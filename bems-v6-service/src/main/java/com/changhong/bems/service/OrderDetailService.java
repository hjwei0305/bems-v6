package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.dto.OrderStatistics;
import com.changhong.bems.dto.OrderStatus;
import com.changhong.bems.dto.SplitDetailQuickQueryParam;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.service.BaseEntityService;
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
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

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
    private OrderDao orderDao;
    @Autowired
    private OrderCommonService orderCommonService;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
     * 获取订单总金额
     *
     * @param orderId 订单头id
     * @return 返回总金额
     */
    public double getSumAmount(String orderId) {
        return dao.getSumAmount(orderId);
    }

    /**
     * 获取申请单调整金额
     *
     * @param orderId 申请单号
     * @return 返回调整金额:调增金额,调减金额
     */
    public Map<String, Number> getAdjustData(String orderId) {
        Map<String, Number> data = new HashMap<>(7);
        data.put("ADD", 0d);
        data.put("SUB", 0d);
        Object[] objects = dao.getAdjustData(orderId);
        if (Objects.nonNull(objects) && objects.length == 1) {
            Object[] objArr = (Object[]) objects[0];
            if (Objects.nonNull(objArr)) {
                for (Object obj : objArr) {
                    if (Objects.nonNull(obj) && obj instanceof Number) {
                        Number num = (Number) obj;
                        if (num.doubleValue() > 0) {
                            data.put("ADD", num);
                        } else {
                            data.put("SUB", num);
                        }
                    }
                }
            }
        }
        return data;
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
    public ResultData<OrderDetail> updateDetailAmount(Order order, OrderDetail detail, BigDecimal amount) {
        // 原行项金额
        BigDecimal oldAmount = detail.getAmount();
        // 设置当前修改金额
        detail.setAmount(amount);

        ResultData<Void> resultData;
        // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
        switch (order.getOrderCategory()) {
            case INJECTION:
                resultData = orderCommonService.checkInjectionDetail(order, detail);
                break;
            case ADJUSTMENT:
                resultData = orderCommonService.checkAdjustmentDetail(order, detail);
                break;
            case SPLIT:
                resultData = orderCommonService.checkSplitDetail(order, detail);
                break;
            default:
                // 不支持的订单类型
                return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
        }

        if (resultData.successful()) {
            detail.setHasErr(Boolean.FALSE);
            detail.setErrMsg("");
            if (OrderStatus.APPROVING == order.getStatus()) {
                resultData = orderCommonService.confirmUseBudget(order, detail);
                if (resultData.failed()) {
                    detail.setAmount(oldAmount);
                    detail.setHasErr(Boolean.TRUE);
                    detail.setErrMsg(resultData.getMessage());
                    // 回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                }
            } else {
                // 只对正常数据做保存
                this.save(detail);
            }
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
     * 保存订单行项
     * 被异步调用,故忽略事务一致性
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void addOrderItems(Order order, List<OrderDetail> details) {
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
        int detailSize = details.size();
        OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_detail"), orderId, detailSize);
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
        // 设置默认过期时间:1天
        operations.set(statistics, 10, TimeUnit.HOURS);

        // 通过预算类型获取预算维度组合
        ResultData<String> resultData = dimensionAttributeService.getAttribute(order.getCategoryId());
        if (resultData.failed()) {
            LOG.error(resultData.getMessage());
            return;
        }
        // 预算维度组合
        final String attribute = resultData.getData();

        try {
            SessionUser sessionUser = ContextUtil.getSessionUser();
            LongAdder successes = new LongAdder();
            LongAdder failures = new LongAdder();
            // 记录所有hash值,以便识别出重复的行项
            Set<Long> duplicateHash = new CopyOnWriteArraySet<>();
            Map<Long, OrderDetail> detailMap = new ConcurrentHashMap<>(7);
            List<OrderDetail> orderDetails = this.getOrderItems(orderId);
            if (CollectionUtils.isNotEmpty(orderDetails)) {
                detailMap.putAll(orderDetails.stream().collect(Collectors.toMap(OrderDetail::getAttributeCode, o -> o)));
                orderDetails.clear();
            }

            details.parallelStream().forEach(detail -> {
                // 订单id
                detail.setOrderId(orderId);
                // 维度属性组合
                detail.setAttribute(attribute);

                // 保存订单行项.若存在相同的行项则忽略跳过(除非在导入时需要对金额做覆盖处理)
                orderCommonService.putOrderDetail(Boolean.FALSE, order, detail, detailMap, duplicateHash, successes, failures, sessionUser);

                OrderStatistics orderStatistics = new OrderStatistics(ContextUtil.getMessage("task_name_detail"), orderId, detailSize);
                orderStatistics.setSuccesses(successes.intValue());
                orderStatistics.setFailures(failures.intValue());
                // 更新缓存
                redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), orderStatistics, 1, TimeUnit.HOURS);
            });
        } catch (ServiceException e) {
            LOG.error("异步生成单据行项异常", e);
        } finally {
            orderDao.setProcessStatus(orderId, Boolean.FALSE);
            // 清除缓存
            redisTemplate.expire(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), 3, TimeUnit.SECONDS);
        }
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