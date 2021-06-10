package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderStatistics;
import com.changhong.bems.dto.SplitDetailQuickQueryParam;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
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
    private PoolService poolService;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分组大小
     */
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
     * 检查行项是否有错误未处理
     *
     * @param orderId 订单头id
     * @return 错误数
     */
    public long getHasErrCount(String orderId) {
        return dao.getHasErrCount(orderId);
    }

    /**
     * 按订单id设置所有行项的处理状态为处理中
     *
     * @param orderId 订单头id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void setProcessing4All(String orderId) {
        dao.setProcessing4All(orderId);
    }

    /**
     * 更新行项的处理状态为处理完成
     *
     * @param detailId 订单行项id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void setProcessed(String detailId) {
        dao.setProcessed(detailId);
    }


    /**
     * 检查行项是否有处理中的行项
     *
     * @param orderId 订单头id
     * @return 处理中的行项数
     */
    public long getProcessingCount(String orderId) {
        return dao.getProcessingCount(orderId);
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
     * 保存订单行项
     * 被异步调用,故忽略事务一致性
     *
     * @param isCover 出现重复行项时,是否覆盖原有记录
     */
    @Async
    @SuppressWarnings({"UnnecessaryLocalVariable"})
    public void addOrderItems(Order order, List<OrderDetail> details, boolean isCover) {
        if (Objects.isNull(order)) {
            //添加单据行项时,订单头不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00001"));
            return;
        }
        // 订单id
        final String orderId = order.getId();
        if (StringUtils.isBlank(orderId)) {
            //添加单据行项时,订单id不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00002"));
            return;
        }

        OrderStatistics statistics = new OrderStatistics(details.size(), LocalDateTime.now());
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
        // 设置默认过期时间:1天
        operations.set(statistics, 1, TimeUnit.DAYS);

        // 通过预算类型获取预算维度组合
        ResultData<String> resultData = dimensionAttributeService.getAttribute(order.getCategoryId());
        if (resultData.failed()) {
            LOG.error(resultData.getMessage());
            return;
        }
        // 预算维度组合
        final String attribute = resultData.getData();

        // 创建一个单线程执行器,保证任务按顺序执行(FIFO)
        //noinspection AlibabaThreadPoolCreation
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            ////////////// 分组处理,防止数据太多导致异常(in查询限制)  //////////////
            // 计算组数
            int limit = (details.size() + MAX_NUMBER - 1) / MAX_NUMBER;
            // 使用流遍历操作
            List<List<OrderDetail>> groups = new ArrayList<>();
            Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
                groups.add(details.stream().skip(i * MAX_NUMBER).limit(MAX_NUMBER).collect(Collectors.toList()));
            });
            details.clear();
            ////////////// end 分组处理 /////////////

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
                    detailMap = new HashMap<>(7);
                }

                for (OrderDetail detail : detailList) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("正在处理行项: " + JsonUtils.toJson(detail));
                    }
                    // 订单id
                    detail.setOrderId(orderId);
                    // 维度属性组合
                    detail.setAttribute(attribute);
                    if (detail.getHasErr()) {
                        // 对导入时数据校验结果持久化处理
                        this.save(detail);
                        // 错误数加1
                        statistics.addFailures();
                        // 更新缓存
                        OrderStatistics finalStatistics = statistics;
                        CompletableFuture.runAsync(() -> operations.set(finalStatistics), executorService);
                        continue;
                    }

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
            executorService.shutdown();
            // 清除缓存
            redisTemplate.delete(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
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
                /*
                    1.通过主体和维度属性,按对应的自动溯源规则获取上级预算池;
                    2.若找到上级预算池,则更新源预算池及源预算池余额;
                    2-1.通过主体和维度属性hash检查是否存在预算池
                    2-2.若存在,则设置预算池编码和当前余额到行项上
                    2-3.若不存在,则跳过.在预算生效或申请完成时,创建预算池(创建时再检查是否存在预算池)
                    3.若未找到,则返回错误:上级源预算池未找到
                 */
                PoolAttributeView poolAttr = poolService.getParentPeriodBudgetPool(subjectId, detail);
                if (Objects.isNull(poolAttr)) {
                    // 添加单据行项时,上级期间预算池未找到.
                    return ResultData.fail(ContextUtil.getMessage("order_detail_00005"));
                }
                // 获取上级期间源预算池
                detail.setOriginPoolCode(poolAttr.getCode());
                detail.setOriginPoolAmount(poolAttr.getBalance());

                resultData = poolService.getPool(subjectId, detail.getAttributeCode());
                if (resultData.successful()) {
                    Pool pool = resultData.getData();
                    detail.setPoolCode(pool.getCode());
                    detail.setPoolAmount(pool.getBalance());
                }
                break;
            default:
                // 不支持的订单类型
                return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }
        ResultData<DimensionAttribute> result = dimensionAttributeService.createAttribute(subjectId, detail);
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
        if (OrderCategory.INJECTION == order.getOrderCategory()) {
            // 注入下达(对总额的增减,允许预算池不存在)
            String poolCode = detail.getPoolCode();
            if (StringUtils.isBlank(poolCode)) {
                ResultData<Pool> result = poolService.getPool(subjectId, detail.getAttributeCode());
                if (result.successful()) {
                    // 预算池编码
                    poolCode = result.getData().getCode();
                    detail.setPoolCode(poolCode);
                }
            }
            if (StringUtils.isNotBlank(poolCode)) {
                // 当前预算池余额. 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
                double balance = poolService.getPoolBalanceByCode(poolCode);
                // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                if (balance + detail.getAmount() < 0) {
                    // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                    return ResultData.fail(ContextUtil.getMessage("pool_00002", poolCode, balance, detail.getAmount()));
                }
                detail.setPoolAmount(balance);
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
        if (OrderCategory.ADJUSTMENT == order.getOrderCategory()) {
            // 调整(跨纬度调整,总额不变.不允许预算池不存在)
            // 预算池编码
            String poolCode = detail.getPoolCode();
            // 当前预算池余额. 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
            double balance = poolService.getPoolBalanceByCode(poolCode);
            // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
            if (balance + detail.getAmount() < 0) {
                // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                return ResultData.fail(ContextUtil.getMessage("pool_00002", poolCode, balance, detail.getAmount()));
            }
            detail.setPoolAmount(balance);
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
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            // 分解(年度到月度,总额不变.允许目标预算池不存在,源预算池必须存在)
            // 源预算池代码
            String originPoolCode = detail.getOriginPoolCode();
            if (StringUtils.isBlank(originPoolCode)) {
                // 上级期间预算池不存在.
                return ResultData.fail(ContextUtil.getMessage("order_detail_00010"));
            }
            // 当前预算池余额. 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
            double originBalance = poolService.getPoolBalanceByCode(originPoolCode);
            // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
            if (originBalance - detail.getAmount() < 0) {
                // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                return ResultData.fail(ContextUtil.getMessage("pool_00002", originPoolCode, originBalance, detail.getAmount()));
            }
            detail.setOriginPoolAmount(originBalance);

            // 当前(目标)预算池代码
            String poolCode = detail.getPoolCode();
            if (StringUtils.isBlank(poolCode)) {
                ResultData<Pool> result = poolService.getPool(subjectId, detail.getAttributeCode());
                if (result.successful()) {
                    // 预算池编码
                    poolCode = result.getData().getCode();
                    detail.setPoolCode(poolCode);
                }
            }
            if (StringUtils.isNotBlank(poolCode)) {
                // 当前预算池余额. 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
                double balance = poolService.getPoolBalanceByCode(poolCode);
                // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                if (balance + detail.getAmount() < 0) {
                    // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                    return ResultData.fail(ContextUtil.getMessage("pool_00002", poolCode, balance, detail.getAmount()));
                }
                detail.setPoolAmount(balance);
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
     * 分页查询预算分解上级期间预算
     *
     * @param param 查询参数
     * @return 上级期间预算
     */
    public PageResult<OrderDetail> querySplitGroup(SplitDetailQuickQueryParam param) {
        return dao.querySplitGroup(param);
    }
}