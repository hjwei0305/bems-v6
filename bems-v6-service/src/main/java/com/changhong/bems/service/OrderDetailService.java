package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.dao.OrderDetailErrDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.OrderDetailErr;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    private OrderDetailErrDao orderDetailErrDao;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HANDLE_CACHE_KEY_PREFIX = "bems-v6:order:handle:";

    // 分组大小
    private static final int MAX_NUMBER = 500;

    @Override
    protected BaseEntityDao<OrderDetail> getDao() {
        return dao;
    }

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearOrderItems(String orderId) {
        orderDetailErrDao.clearOrderItems(orderId);
        int count = dao.clearOrderItems(orderId);
        if (LogUtil.isDebugEnabled()) {
            LogUtil.debug("预算申请单[" + orderId + "]清空明细[" + count + "]行.");
        }
    }

    /**
     * 更新行项金额
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
                for (OrderDetail detail : detailList) {
                    detail.setAmount(detailMap.get(detail.getId()));

                    // TODO 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)

                }
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

        try {
            BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(HANDLE_CACHE_KEY_PREFIX.concat(orderId));
            OrderStatistics statistics = (OrderStatistics) operations.get();
            if (Objects.nonNull(statistics)) {
                // 订单[]正在处理过程中,再次提交会影响处理统计
                LOG.warn(ContextUtil.getMessage("order_detail_00005"));
            } else {
                statistics = new OrderStatistics(details.size(), LocalDateTime.now());
                // 设置默认过期时间:1天
                operations.set(statistics, 1, TimeUnit.DAYS);
            }

            // 保存订单行项
            this.addOrderItems(order, details, Boolean.TRUE);
        } catch (ServiceException e) {
            LOG.error("异步生成单据行项异常", e);
        } finally {
            // 清除缓存
            redisTemplate.delete(HANDLE_CACHE_KEY_PREFIX.concat(orderId));
        }

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
            BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(HANDLE_CACHE_KEY_PREFIX.concat(orderId));
            OrderStatistics statistics = (OrderStatistics) operations.get();
            if (Objects.nonNull(statistics)) {
                // 订单[]正在处理过程中,再次提交会影响处理统计
                LOG.warn(ContextUtil.getMessage("order_detail_00005"));
            } else {
                statistics = new OrderStatistics(detailList.size(), LocalDateTime.now());
                // 设置默认过期时间:1天
                operations.set(statistics, 1, TimeUnit.DAYS);
            }

            // 保存订单行项
            this.addOrderItems(order, detailList, Boolean.FALSE);
        } catch (ServiceException e) {
            LOG.error("异步生成单据行项异常", e);
        } finally {
            // 清除缓存
            redisTemplate.delete(HANDLE_CACHE_KEY_PREFIX.concat(orderId));
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
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_ORG, dimensionCode)) {
            detail.setOrg(dimension.getValue());
            detail.setOrgName(dimension.getText());
        } else if (StringUtils.equals(Constants.DIMENSION_CODE_PROJECT, dimensionCode)) {
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
//        makeAttributeHash(detail);
    }

//    /**
//     * 计算维度属性hash值
//     */
//    private static void makeAttributeHash(OrderDetail detail) {
//        if (Objects.nonNull(detail)) {
//            long result = 1;
//            result = 31 * result + detail.getItem().hashCode();
//            result = 31 * result + detail.getPeriod().hashCode();
//            result = 31 * result + detail.getOrg().hashCode();
//            result = 31 * result + detail.getProject().hashCode();
//
//            result = 31 * result + detail.getUdf1().hashCode();
//            result = 31 * result + detail.getUdf2().hashCode();
//            result = 31 * result + detail.getUdf3().hashCode();
//            result = 31 * result + detail.getUdf4().hashCode();
//            result = 31 * result + detail.getUdf5().hashCode();
//            detail.setAttributeHash(result);
//        } else {
//            // 预算维度属性不能为空
//            throw new ServiceException(ContextUtil.getMessage("dimension_attribute_00002"));
//        }
//    }

    /**
     * 保存订单行项
     * 被异步调用,故忽略事务一致性
     *
     * @param isCover 出现重复行项时,是否覆盖原有记录
     */
    private void addOrderItems(Order order, List<OrderDetail> details, boolean isCover) {
        if (Objects.isNull(order)) {
            //添加单据行项时,订单头不能为空.
            LOG.error(ContextUtil.getMessage("order_detail_00001"));
            return;
        }
        // 订单id
        String orderId = order.getId();
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(HANDLE_CACHE_KEY_PREFIX + orderId);

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

        // 记录所有hash值,以便识别出重复的行项
        Set<Long> duplicateHash = new HashSet<>();
        Search search = Search.createSearch();
        // 分组处理
        for (List<OrderDetail> detailList : groups) {
            search.clearAll();

            Set<Long> hashSet = detailList.stream().map(OrderDetail::getAttributeHash).collect(Collectors.toSet());
            search.addFilter(new SearchFilter(OrderDetail.FIELD_ORDER_ID, orderId));
            search.addFilter(new SearchFilter(OrderDetail.FIELD_ATTRIBUTE_HASH, hashSet, SearchFilter.Operator.IN));
            List<OrderDetail> orderDetails = dao.findByFilters(search);
            Map<Long, OrderDetail> detailMap;
            if (CollectionUtils.isNotEmpty(orderDetails)) {
                detailMap = orderDetails.stream().collect(Collectors.toMap(OrderDetail::getAttributeHash, o -> o));
            } else {
                detailMap = new HashMap<>();
            }

            for (OrderDetail detail : detailList) {
                detail.setOrderId(orderId);
                OrderStatistics statistics = (OrderStatistics) operations.get();
                if (Objects.isNull(statistics)) {
                    statistics = new OrderStatistics(size, LocalDateTime.now());
                }

                // 本次提交数据中存在重复项
                if (duplicateHash.contains(detail.getAttributeHash())) {
                    OrderDetailErr err = new OrderDetailErr(detail);
                    // 存在重复项
                    err.setErrMsg(ContextUtil.getMessage("order_detail_00006"));
                    err.setTenantCode(ContextUtil.getTenantCode());
                    orderDetailErrDao.save(err);
                    // 错误数加1
                    statistics.addFailures();
                    // 更新缓存
                    operations.set(statistics);
                    continue;
                } else {
                    // 记录hash值
                    duplicateHash.add(detail.getAttributeHash());
                }
                // 检查持久化数据中是否存在重复项
                OrderDetail orderDetail = detailMap.get(detail.getAttributeHash());
                if (Objects.nonNull(orderDetail)) {
                    // 检查重复行项
                    if (isCover) {
                        // 覆盖原有行项记录(更新金额)
                        orderDetail.setAmount(detail.getAmount());
                        details.add(orderDetail);
                    } else {
                        // 忽略,不做处理
                        continue;
                    }
                } else {
                    details.add(detail);
                }
            }
        }

        OperateResultWithData<OrderDetail> result;
        // 获取预算池及可用额度
        for (OrderDetail detail : details) {
            OrderStatistics statistics = (OrderStatistics) operations.get();

            result = this.save(detail);
            if (result.successful()) {
                statistics.addSuccesses();
            } else {
                statistics.addFailures();
                OrderDetailErr err = new OrderDetailErr(detail);
                err.setTenantCode(ContextUtil.getTenantCode());
                err.setErrMsg(result.getMessage());
                orderDetailErrDao.save(err);
            }
            operations.set(statistics);
        }
//        handlePool(orderId, order.getOrderCategory(), details);
    }

    /**
     * 对行项数据做预算池及可用额度处理
     *
     * @param category 订单类型
     * @param details  订单行项
     */
    private void handlePool(String orderId, OrderCategory category, List<OrderDetail> details) {
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(HANDLE_CACHE_KEY_PREFIX + orderId);
        for (OrderDetail detail : details) {
            OrderStatistics statistics = (OrderStatistics) operations.get();


        }
    }
}