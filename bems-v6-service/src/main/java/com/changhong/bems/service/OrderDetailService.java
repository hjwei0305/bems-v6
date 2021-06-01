package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 预算维度属性(OrderDetail)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:59
 */
@Service
public class OrderDetailService extends BaseEntityService<OrderDetail> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderDetailService.class);

    @Autowired
    private OrderDetailDao dao;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static final String HANDLE_CACHE_KEY_PREFIX = "bems-v6:order:handle:";

    // 分组大小
    private static final int MAX_NUMBER = 500;

    @Override
    protected BaseEntityDao<OrderDetail> getDao() {
        return dao;
    }

    /**
     * 通过单据Id获取单据行项
     *
     * @param orderId 单据Id
     */
    public List<OrderDetail> getOrderItems(String orderId) {
        return dao.findListByProperty(OrderDetail.FIELD_ORDER_ID, orderId);
    }

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearOrderItems(String orderId) {
        int count = dao.clearOrderItems(orderId);
        if (LogUtil.isDebugEnabled()) {
            LogUtil.debug("预算申请单[" + orderId + "]清空明细[" + count + "]行.");
        }
    }

    /**
     * 通过单据行项id删除行项
     *
     * @param ids 单据行项Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeOrderItems(Set<String> ids) {
        dao.delete(ids);
    }

    /**
     * 更新预算申请单行项金额
     *
     * @param order  申请单
     * @param detail 申请单行项
     * @param amount 金额
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<OrderDetail> updateDetailAmount(Order order, OrderDetail detail, double amount) {
        // 原行项金额
        double oldAmount = detail.getAmount();
        // 设置当前修改金额
        detail.setAmount(amount);

        ResultData<Void> resultData;
        // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
        switch (order.getOrderCategory()) {
            case INJECTION:
                resultData = this.checkInjectionDetail(order, detail);
                break;
            case ADJUSTMENT:
                resultData = this.checkAdjustmentDetail(order, detail);
                break;
            case SPLIT:
                resultData = this.checkSplitDetail(order, detail);
                break;
            default:
                // 不支持的订单类型
                return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }
        if (resultData.successful()) {
            detail.setHasErr(Boolean.FALSE);
            detail.setErrMsg("");
            // 只对正常数据做保存
            this.save(detail);
        } else {
            detail.setAmount(oldAmount);
            detail.setHasErr(Boolean.TRUE);
            detail.setErrMsg(resultData.getMessage());
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return ResultData.success(detail);
    }

    /**
     * 更新行项金额
     *
     * @return 返回订单总金额
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> updateAmount(Order order, List<OrderDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            Map<String, Double> detailMap = details.stream().collect(Collectors.toMap(OrderDetail::getId, OrderDetail::getAmount));
            Search search = Search.createSearch();
            search.addFilter(new SearchFilter(OrderDetail.FIELD_ORDER_ID, order.getId()));
            search.addFilter(new SearchFilter(OrderDetail.ID, detailMap.keySet(), SearchFilter.Operator.IN));
            List<OrderDetail> detailList = dao.findByFilters(search);
            if (CollectionUtils.isNotEmpty(detailList)) {
                ResultData<Void> resultData;
                for (OrderDetail detail : detailList) {
                    detail.setAmount(detailMap.get(detail.getId()));

                    // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                    switch (order.getOrderCategory()) {
                        case INJECTION:
                            resultData = this.checkInjectionDetail(order, detail);
                            break;
                        case ADJUSTMENT:
                            resultData = this.checkAdjustmentDetail(order, detail);
                            break;
                        case SPLIT:
                            resultData = this.checkSplitDetail(order, detail);
                            break;
                        default:
                            // 不支持的订单类型
                            return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                    }
                    if (resultData.failed()) {
                        return resultData;
                    }
                }
                this.save(detailList);
            }
        }
        return ResultData.success();
    }

    /**
     * 保存订单行项(导入使用)
     */
    @Async
    public void batchAddOrderItems(Order order, List<OrderDetail> details) {
        if (Objects.isNull(order)) {
            // 添加单据行项时,订单头不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00001"));
            return;
        }
        String orderId = order.getId();
        if (StringUtils.isBlank(orderId)) {
            //添加单据行项时,订单id不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00002"));
            return;
        }
        if (CollectionUtils.isEmpty(details)) {
            // 添加单据行项时,行项数据不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00004"));
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("生成行项: " + details.size());
        }

        // 保存订单行项
        this.addOrderItems(order, details, Boolean.TRUE);
    }

    /**
     * 异步生成单据行项(手工添加使用)
     * 若存在相同的行项则忽略跳过(除非在导入时需要覆盖处理)
     *
     * @param order 单据头
     */
    @Async
    public void batchAddOrderItems(Order order, AddOrderDetail addOrderDetail) {
        if (Objects.isNull(order)) {
            //添加单据行项时,订单头不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00001"));
            return;
        }
        String orderId = order.getId();
        if (StringUtils.isBlank(orderId)) {
            //添加单据行项时,订单id不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00002"));
            return;
        }
        String categoryId = order.getCategoryId();
        if (StringUtils.isBlank(categoryId)) {
            //添加单据行项时,预算类型不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00003"));
            return;
        }
        if (Objects.isNull(addOrderDetail)) {
            //添加单据行项时,行项数据不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00004"));
            return;
        }
        List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
        if (CollectionUtils.isEmpty(dimensions)) {
            // 预算类型[{0}]下未找到预算维度
            LOG.error(ContextUtil.getMessage("category_00007"));
            return;
        }
        if (CollectionUtils.isEmpty(dimensions)) {
            // 预算类型[{0}]下未找到预算维度
            LOG.error(ContextUtil.getMessage("category_00007"));
            return;
        }

        List<String> keyList = new ArrayList<>();
        // 维度映射
        Map<String, Set<OrderDimension>> dimensionMap = new HashMap<>();
        for (DimensionDto dimension : dimensions) {
            String dimensionCode = dimension.getCode();
            keyList.add(dimensionCode);
            // 期间维度
            if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_PERIOD, addOrderDetail.getPeriod());
            }
            // 科目维度
            else if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_ITEM, addOrderDetail.getItem());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_ORG, addOrderDetail.getOrg());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_PROJECT, addOrderDetail.getProject());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF1, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF1, addOrderDetail.getUdf1());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF2, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF2, addOrderDetail.getUdf2());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF3, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF3, addOrderDetail.getUdf3());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF4, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF4, addOrderDetail.getUdf4());
            } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF5, dimensionCode)) {
                dimensionMap.put(Constants.DIMENSION_CODE_UDF5, addOrderDetail.getUdf5());
            }
        }

        try {
            List<OrderDetail> detailList = new ArrayList<>();
            OrderDetail detail = new OrderDetail();
            // 订单id
            detail.setOrderId(orderId);

            // 通过笛卡尔方式生成行项
            descartes(keyList, dimensionMap, detailList, 0, detail);
            if (LOG.isDebugEnabled()) {
                LOG.debug("生成行项: " + detailList.size());
            }

            // 保存订单行项
            this.addOrderItems(order, detailList, Boolean.FALSE);
        } catch (ServiceException e) {
            LOG.error("异步生成单据行项异常", e);
        }
    }

    /**
     * 笛卡尔方式生成行项
     */
    private static void descartes(List<String> keyList, Map<String, Set<OrderDimension>> dimensionMap,
                                  List<OrderDetail> detailList, int layer, OrderDetail detail) {
        // 维度代码
        String dimensionCode = keyList.get(layer);
        // 当前维度所选择的要素清单
        Set<OrderDimension> orderDimensionSet = dimensionMap.get(dimensionCode);
        // 如果不是最后一个子集合时
        if (layer < keyList.size() - 1) {
            // 如果当前子集合元素个数为空，则抛出异常中止
            if (CollectionUtils.isEmpty(orderDimensionSet)) {
                throw new ServiceException("维度[" + dimensionCode + "]未选择要素值.");
            } else {
                OrderDetail od;
                //如果当前子集合元素不为空，则循环当前子集合元素，累加到临时变量。并且继续递归调用，直到达到父集合的最后一个子集合。
                int i = 0;
                for (OrderDimension dimension : orderDimensionSet) {
                    if (i == 0) {
                        od = detail;
                    } else {
                        od = detail.clone();
                    }
                    // 设置维度值
                    setDimension(dimensionCode, dimension, od);
                    descartes(keyList, dimensionMap, detailList, layer + 1, od);
                    i++;
                }
            }
        }
        //递归调用到最后一个子集合时
        else if (layer == keyList.size() - 1) {
            // 如果当前子集合元素为空，则抛出异常中止
            if (CollectionUtils.isEmpty(orderDimensionSet)) {
                throw new ServiceException("维度[" + dimensionCode + "]未选择要素值.");
            } else {
                OrderDetail od;
                //如果当前子集合元素不为空，则循环当前子集合所有元素，累加到临时变量，然后将临时变量加入到结果集中。
                int i = 0;
                for (OrderDimension dimension : orderDimensionSet) {
                    if (i == 0) {
                        od = detail;
                    } else {
                        od = detail.clone();
                    }
                    // 设置维度值
                    setDimension(dimensionCode, dimension, od);
                    detailList.add(od);
                    i++;
                }
            }
        }
    }

    /**
     * 设置维度要素值
     *
     * @param dimensionCode 维度代码
     * @param dimension     选择的维度要素值
     * @param detail        订单行项对象
     */
    private static void setDimension(String dimensionCode, OrderDimension dimension, OrderDetail detail) {
        // 期间维度
        if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, dimensionCode)) {
            detail.setPeriod(dimension.getValue());
            detail.setPeriodName(dimension.getText());
        }
        // 科目维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, dimensionCode)) {
            detail.setItem(dimension.getValue());
            detail.setItemName(dimension.getText());
        }
        // 组织机构维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
            detail.setOrg(dimension.getValue());
            detail.setOrgName(dimension.getText());
        }
        // 项目维度
        else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
            detail.setProject(dimension.getValue());
            detail.setProjectName(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF1, dimensionCode)) {
            detail.setUdf1(dimension.getValue());
            detail.setUdf1Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF2, dimensionCode)) {
            detail.setUdf2(dimension.getValue());
            detail.setUdf2Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF3, dimensionCode)) {
            detail.setUdf3(dimension.getValue());
            detail.setUdf3Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF4, dimensionCode)) {
            detail.setUdf4(dimension.getValue());
            detail.setUdf4Name(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_UDF5, dimensionCode)) {
            detail.setUdf5(dimension.getValue());
            detail.setUdf5Name(dimension.getText());
        }
    }

    /**
     * 保存订单行项
     * 被异步调用,故忽略事务一致性
     *
     * @param isCover 出现重复行项时,是否覆盖原有记录
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnnecessaryLocalVariable"})
    private void addOrderItems(Order order, List<OrderDetail> details, boolean isCover) {
        if (Objects.isNull(order)) {
            //添加单据行项时,订单头不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00001"));
            return;
        }
        // 订单id
        String orderId = order.getId();
        if (StringUtils.isBlank(orderId)) {
            //添加单据行项时,订单id不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00002"));
            return;
        }
        // 创建一个单线程执行器,保证任务按顺序执行(FIFO)
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            // 分组处理,防止数据太多导致异常(in查询限制)
            int size = details.size();
            // 计算组数
            int limit = (details.size() + MAX_NUMBER - 1) / MAX_NUMBER;
            // 使用流遍历操作
            List<List<OrderDetail>> groups = new ArrayList<>();
            Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
                groups.add(details.stream().skip(i * MAX_NUMBER).limit(MAX_NUMBER).collect(Collectors.toList()));
            });
            details.clear();

            OrderStatistics statistics = new OrderStatistics(size, LocalDateTime.now());
            BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(HANDLE_CACHE_KEY_PREFIX.concat(orderId));
            // 设置默认过期时间:1天
            operations.set(statistics, 1, TimeUnit.DAYS);

            // 记录所有hash值,以便识别出重复的行项
            Set<Long> duplicateHash = new HashSet<>();
            Search search = Search.createSearch();
            ResultData<Void> result;
            // 分组处理
            for (List<OrderDetail> detailList : groups) {
                search.clearAll();

                Set<Long> hashSet = detailList.stream().map(OrderDetail::getAttributeCode).collect(Collectors.toSet());
                search.addFilter(new SearchFilter(OrderDetail.FIELD_ORDER_ID, orderId));
                search.addFilter(new SearchFilter(OrderDetail.FIELD_ATTRIBUTE_CODE, hashSet, SearchFilter.Operator.IN));
                List<OrderDetail> orderDetails = dao.findByFilters(search);
                Map<Long, OrderDetail> detailMap;
                if (CollectionUtils.isNotEmpty(orderDetails)) {
                    detailMap = orderDetails.stream().collect(Collectors.toMap(OrderDetail::getAttributeCode, o -> o));
                } else {
                    detailMap = new HashMap<>();
                }

                for (OrderDetail detail : detailList) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("正在处理行项: " + JsonUtils.toJson(detail));
                    }

                    detail.setOrderId(orderId);

                    // 本次提交数据中存在重复项
                    if (duplicateHash.contains(detail.getAttributeCode())) {
                        // 有错误的
                        detail.setHasErr(Boolean.TRUE);
                        // 存在重复项
                        detail.setErrMsg(ContextUtil.getMessage("order_detail_00006"));
                        this.save(detail);
                        // 错误数加1
                        statistics.addFailures();
                        // 更新缓存
                        OrderStatistics finalStatistics = statistics;
                        CompletableFuture.runAsync(() -> operations.set(finalStatistics), executorService);
                        continue;
                    } else {
                        // 记录hash值
                        duplicateHash.add(detail.getAttributeCode());
                    }
                    // 检查持久化数据中是否存在重复项
                    OrderDetail orderDetail = detailMap.get(detail.getAttributeCode());
                    if (Objects.nonNull(orderDetail)) {
                        // 检查重复行项
                        if (isCover) {
                            // 覆盖原有行项记录(更新金额)
                            orderDetail.setAmount(detail.getAmount());
                            detail = orderDetail;
                        } else {
                            // 忽略,不做处理
                            continue;
                        }
                    }

                    try {
                        // 设置行项数据的预算池及当前可用额度
                        result = this.createDetail(order, detail);
                    } catch (Exception e) {
                        result = ResultData.fail(ExceptionUtils.getRootCauseMessage(e));
                    }
                    if (result.successful()) {
                        statistics.addSuccesses();
                    } else {
                        statistics.addFailures();
                        // 有错误的
                        detail.setHasErr(Boolean.TRUE);
                        detail.setErrMsg(result.getMessage());
                    }
                    this.save(detail);
                    OrderStatistics finalStatistics = statistics;
                    CompletableFuture.runAsync(() -> operations.set(finalStatistics), executorService);
                }
            }
        } catch (ServiceException e) {
            LOG.error("异步生成单据行项异常", e);
        } finally {
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
            // 清除缓存
            redisTemplate.delete(HANDLE_CACHE_KEY_PREFIX.concat(orderId));
        }
    }

    /**
     * 设置行项数据的预算池及可用额度
     *
     * @param order  订单头
     * @param detail 订单行项
     */
    private ResultData<Void> createDetail(Order order, OrderDetail detail) throws Exception {
        // 预算主体id
        String subjectId = order.getSubjectId();
        ResultData<Pool> resultData;
        switch (order.getOrderCategory()) {
            case INJECTION:
                // 注入下达(对总额的增减,预算池可以不存在)
                /*
                    1.通过主体和维度属性hash检查是否存在预算池
                    2.若存在,则设置预算池编码和当前余额到行项上
                    3.若不存在,则跳过.在预算生效或申请完成时,创建预算池(创建时再检查是否存在预算池)
                 */
                resultData = poolService.getPool(subjectId, detail.getAttributeCode());
                if (resultData.successful()) {
                    Pool pool = resultData.getData();
                    detail.setPoolCode(pool.getCode());
                    detail.setPoolAmount(pool.getBalance());
                }
                break;
            case ADJUSTMENT:
                // 调整(跨纬度调整,总额不变,且预算池必须存在)
                /*
                    1.通过主体和维度属性hash检查是否存在预算池
                    2.若存在,则设置预算池编码和当前余额到行项上
                    3.若不存在,则返回错误:预算池未找到
                 */
                resultData = poolService.getPool(subjectId, detail.getAttributeCode());
                if (resultData.successful()) {
                    Pool pool = resultData.getData();
                    detail.setPoolCode(pool.getCode());
                    detail.setPoolAmount(pool.getBalance());
                } else {
                    return ResultData.fail(resultData.getMessage());
                }
                break;
            case SPLIT:
                // 分解(年度到月度,总额不变.目标预算池可以不存在,但源预算池必须存在)
                /* TODO
                    1.通过主体和维度属性,按对应的自动溯源规则获取上级预算池;
                    2.若找到上级预算池,则更新源预算池及源预算池余额;
                    2-1.通过主体和维度属性hash检查是否存在预算池
                    2-2.若存在,则设置预算池编码和当前余额到行项上
                    2-3.若不存在,则跳过.在预算生效或申请完成时,创建预算池(创建时再检查是否存在预算池)
                    3.若未找到,则返回错误:预算池未找到
                 */
                break;
            default:
                // 不支持的订单类型
                return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }
        ResultData<DimensionAttribute> result = dimensionAttributeService.createAttribute(subjectId, order.getCategoryId(), detail);
        if (result.successful()) {
            if (!Objects.equals(detail.getAttributeCode(), result.getData().getAttributeCode())) {
                LOG.error("预算维度策略hash计算错误: {}", JsonUtils.toJson(detail));
                throw new ServiceException("预算维度策略hash计算错误.");
            }
        } else {
            return ResultData.fail(result.getMessage());
        }
        return ResultData.success();
    }

    /**
     * 按下达注入的规则检查行项明细
     * 下达注入规则:
     *
     * @param order  订单头
     * @param detail 订单行项
     */
    public ResultData<Void> checkInjectionDetail(Order order, OrderDetail detail) {
        // 预算主体id
        String subjectId = order.getSubjectId();
        Pool pool = null;
        // 当前预算池余额
        double balance;
        if (OrderCategory.INJECTION == order.getOrderCategory()) {
            // 注入下达(对总额的增减,允许预算池不存在)
            ResultData<Pool> result = poolService.getPool(subjectId, detail.getAttributeCode());
            if (result.successful()) {
                pool = result.getData();
                // 预算池编码
                detail.setPoolCode(pool.getCode());
            }
            if (Objects.nonNull(pool)) {
                // 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
                balance = poolService.getPoolBalance(pool);
                // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                if (balance + detail.getAmount() < 0) {
                    // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                    return ResultData.fail(ContextUtil.getMessage("pool_00002", pool.getCode(), balance, detail.getAmount()));
                }
                detail.setPoolAmount(pool.getBalance());
            } else {
                // 当预算池不存在时,发生金额不能小于0(不能将预算池值为负数)
                if (detail.getAmount() < 0) {
                    // 预算池金额不能值为负数[{0}]
                    return ResultData.fail(ContextUtil.getMessage("pool_00004", detail.getAmount()));
                }
            }
        } else {
            // 不支持的订单类型
            return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }
        return ResultData.success();
    }

    /**
     * 对行项数据做预算池及可用额度处理
     *
     * @param order  订单头
     * @param detail 订单行项
     */
    public ResultData<Void> checkAdjustmentDetail(Order order, OrderDetail detail) {
        // 预算主体id
        String subjectId = order.getSubjectId();
        // 当前预算池余额
        double balance;
        if (OrderCategory.ADJUSTMENT == order.getOrderCategory()) {
            // 调整(跨纬度调整,总额不变.不允许预算池不存在)
            // 预算池编码
            ResultData<Pool> resultData = poolService.getPool(subjectId, detail.getAttributeCode());
            if (resultData.successful()) {
                Pool pool = resultData.getData();
                // 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
                balance = poolService.getPoolBalance(pool);
                // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                if (balance + detail.getAmount() < 0) {
                    // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                    return ResultData.fail(ContextUtil.getMessage("pool_00002", pool.getCode(), balance, detail.getAmount()));
                }
                detail.setPoolAmount(pool.getBalance());
            } else {
                // 预算池未找到
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 不支持的订单类型
            return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }
        return ResultData.success();
    }

    /**
     * 对行项数据做预算池及可用额度处理
     *
     * @param order  订单头
     * @param detail 订单行项
     */
    public ResultData<Void> checkSplitDetail(Order order, OrderDetail detail) {
        // 预算主体id
        String subjectId = order.getSubjectId();
        Pool pool = null;
        // 预算池编码
        String poolCode;
        // 当前预算池余额
        double balance;
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            // 分解(年度到月度,总额不变.允许目标预算池不存在,源预算池必须存在)
            /* TODO
                1.通过主体和维度属性,按对应的自动溯源规则获取上级预算池;
                2.若找到上级预算池,则更新源预算池及源预算池余额;
                2-1.通过主体和维度属性hash检查是否存在预算池
                2-2.若存在,则设置预算池编码和当前余额到行项上
                2-3.若不存在,则跳过.在预算生效或申请完成时,创建预算池(创建时再检查是否存在预算池)
                3.若未找到,则返回错误:预算池未找到
             */
        } else {
            // 不支持的订单类型
            return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }
        return ResultData.success();
    }
}