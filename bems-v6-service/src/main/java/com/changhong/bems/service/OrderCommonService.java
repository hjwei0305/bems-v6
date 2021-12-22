package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.dto.OrderStatistics;
import com.changhong.bems.dto.OrderStatus;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.context.mock.MockUser;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.util.EnumUtils;
import com.changhong.sei.util.thread.ThreadLocalHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-22 01:14
 */
@Service
public class OrderCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderCommonService.class);
    @Autowired
    private OrderDao dao;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private EventService eventService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MockUser mockUser;

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderId, OrderStatus status, boolean processing) {
        // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
        dao.updateOrderStatus(orderId, status, processing);
        if (processing) {
            // 按订单id设置所有行项的处理状态为处理中
            orderDetailService.setProcessing4All(orderId);
        } else {
            redisTemplate.expire(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), 3, TimeUnit.SECONDS);
        }
    }

    /**
     * 异步确认
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void asyncConfirm(Order order, List<OrderDetail> details, SessionUser sessionUser) {
        LongAdder successes = new LongAdder();
        LongAdder failures = new LongAdder();
        String orderId = order.getId();
        OrderStatistics statistics = new OrderStatistics(orderId, details.size());
        details.parallelStream().forEach(detail -> {
            ResultData<Void> result = this.confirmUseBudget(order, detail, sessionUser);

            this.pushProcessState(successes, failures, statistics, result);
        });
        // 若处理完成,则更新订单状态为:已确认
        this.updateOrderStatus(orderId, OrderStatus.CONFIRMED, Boolean.FALSE);
    }

    /**
     * 异步撤销确认
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void asyncCancelConfirm(Order order, List<OrderDetail> details, SessionUser sessionUser) {
        LongAdder successes = new LongAdder();
        LongAdder failures = new LongAdder();
        String orderId = order.getId();
        OrderStatistics statistics = new OrderStatistics(orderId, details.size());
        details.parallelStream().forEach(detail -> {
            ResultData<Void> result = this.cancelConfirmUseBudget(order, detail, sessionUser);

            this.pushProcessState(successes, failures, statistics, result);
        });
        // 若处理完成,则更新订单状态为:草稿
        this.updateOrderStatus(orderId, OrderStatus.DRAFT, Boolean.FALSE);
    }

    /**
     * 异步生效预算
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void asyncEffective(Order order, List<OrderDetail> details, SessionUser sessionUser) {
        LongAdder successes = new LongAdder();
        LongAdder failures = new LongAdder();
        String orderId = order.getId();
        OrderStatistics statistics = new OrderStatistics(orderId, details.size());
        details.parallelStream().forEach(detail -> {
            ResultData<Void> result = this.effectiveUseBudget(order, detail, sessionUser);
            this.pushProcessState(successes, failures, statistics, result);
        });
        // 若处理完成,则更新订单状态为:已生效
        this.updateOrderStatus(orderId, OrderStatus.COMPLETED, Boolean.FALSE);
    }

    private void pushProcessState(LongAdder successes, LongAdder failures, OrderStatistics statistics, ResultData<Void> result) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("异步处理结果: {}", result);
        }
        if (result.successful()) {
            successes.increment();
        } else {
            failures.increment();
        }
        statistics.setSuccesses(successes.intValue());
        statistics.setFailures(failures.intValue());
        redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX + statistics.getOrderId(), statistics, 10, TimeUnit.HOURS);
    }

    /**
     * 确认预算申请单
     * 规则:预算池进行预占用
     *
     * @param order  预算申请单
     * @param detail 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> confirmUseBudget(Order order, OrderDetail detail, SessionUser sessionUser) {
        if (detail.getState() >= 0) {
            // 已处理,不用重复再做预占用
            return ResultData.success();
        }

        ResultData<Void> resultData;
        OrderStatus status = order.getStatus();
        // 状态为草稿和确认中的可进行确认操作
        if (OrderStatus.DRAFT == status || OrderStatus.CONFIRMING == status) {
            String poolCode;
            Pool pool = null;
            resultData = ResultData.success();
            String code = order.getCode();
            String remark = order.getRemark();
            try {
                // 本地线程全局变量存储-开始
                ThreadLocalHolder.begin();

                mockUser.mockCurrentUser(sessionUser);

                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            resultData = orderDetailService.checkInjectionDetail(order, detail);
                            if (resultData.failed()) {
                                break;
                            }
                            poolCode = detail.getPoolCode();
                            if (StringUtils.isNotBlank(poolCode)) {
                                pool = poolService.getPool(poolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_INJECTION);
                            }
                            // 记录预算池执行日志.从无到有的新增预算,视为外部注入internal = Boolean.TRUE
                            poolService.poolAmountLog(pool, detail.getId(), code, remark, detail.getAmount(),
                                    Constants.EVENT_BUDGET_INJECTION, Boolean.FALSE, OperationType.RELEASE);
                        }
                        break;
                    case ADJUSTMENT:
                        poolCode = detail.getPoolCode();
                        if (StringUtils.isNotBlank(poolCode)) {
                            pool = poolService.getPool(poolCode);
                        }
                        if (Objects.isNull(pool)) {
                            LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }

                        // 为保证性能仅对调减的预算池做额度检查
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            resultData = orderDetailService.checkAdjustmentDetail(order, detail);
                            if (resultData.failed()) {
                                break;
                            }
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_ADJUSTMENT);
                            }
                            // 记录预算池执行日志
                            poolService.poolAmountLog(pool, detail.getId(), code, remark,
                                    detail.getAmount().negate(), Constants.EVENT_BUDGET_ADJUSTMENT, Boolean.TRUE, OperationType.USE);
                        }
                        break;
                    case SPLIT:
                        // 预算分解
                        resultData = orderDetailService.checkSplitDetail(order, detail);
                        if (resultData.successful()) {
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT);
                            }

                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                                // 当前预算池
                                poolCode = detail.getPoolCode();
                                if (StringUtils.isNotBlank(poolCode)) {
                                    pool = poolService.getPool(poolCode);
                                }
                                if (Objects.isNull(pool)) {
                                    LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                    // 预算池不存在
                                    resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                    break;
                                }
                                // 记录预算池执行日志
                                poolService.poolAmountLog(pool, detail.getId(), code, remark,
                                        detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                            } else {
                                // 源预算池
                                String originPoolCode = detail.getOriginPoolCode();
                                if (StringUtils.isNotBlank(originPoolCode)) {
                                    pool = poolService.getPool(originPoolCode);
                                }
                                if (Objects.isNull(pool)) {
                                    LOG.error("源预算池不存在. - " + JsonUtils.toJson(detail));
                                    // 预算池不存在
                                    resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                    break;
                                }
                                // 记录预算池执行日志
                                poolService.poolAmountLog(pool, detail.getId(), code, remark,
                                        detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                            }
                        }
                        break;
                    default:
                        // 不支持的订单类型
                        resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
                // 标记处理完成
                detail.setProcessing(Boolean.FALSE);
                if (resultData.failed()) {
                    detail.setState((short) -1);
                    detail.setHasErr(Boolean.TRUE);
                    detail.setErrMsg(resultData.getMessage());
                } else {
                    // 预占用成功
                    detail.setState((short) 0);
                }
                // 更新行项
                orderDetailService.save(detail);
            } catch (Exception e) {
                LOG.error("预算确认异常", e);
            } finally {
                // 本地线程全局变量存储-释放
                ThreadLocalHolder.end();
            }
        } else {
            // 可能已发起取消确认动作,故不作任何处理
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
        return resultData;
    }

    /**
     * 取消已确认的预算申请单
     * 规则:释放预占用
     *
     * @param detail 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> cancelConfirmUseBudget(Order order, OrderDetail detail, SessionUser sessionUser) {
        ResultData<Void> resultData;
        try {
            // 本地线程全局变量存储-开始
            ThreadLocalHolder.begin();

            mockUser.mockCurrentUser(sessionUser);
            if (detail.getState() < 0) {
                orderDetailService.setProcessed(detail.getId());
                // 未成功预占用,不用做释放
                return ResultData.success();
            }

            OrderStatus status = order.getStatus();
            // 撤销中的,确认中的,已确认的可进行撤销操作
            if (OrderStatus.CANCELING == status || OrderStatus.CONFIRMING == status || OrderStatus.CONFIRMED == status) {
                Pool pool = null;
                // 当前预算池
                String poolCode = detail.getPoolCode();
                String code = order.getCode();
                String remark = order.getRemark();
                String detailId = detail.getId();
                resultData = ResultData.success();

                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            if (StringUtils.isNotBlank(poolCode)) {
                                pool = poolService.getPool(poolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_INJECTION_CANCEL);
                            }
                            // 记录预算池执行日志
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_INJECTION_CANCEL, Boolean.FALSE, OperationType.FREED);
                        }
                        break;
                    case ADJUSTMENT:
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            if (StringUtils.isNotBlank(poolCode)) {
                                pool = poolService.getPool(poolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_ADJUSTMENT_CANCEL);
                            }
                            // 记录预算池执行日志
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount().negate(), Constants.EVENT_BUDGET_ADJUSTMENT_CANCEL, Boolean.TRUE, OperationType.FREED);
                        }
                        break;
                    case SPLIT:
                        // 预算分解
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT_CANCEL);
                        }
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            if (StringUtils.isNotBlank(poolCode)) {
                                pool = poolService.getPool(poolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT_CANCEL, Boolean.TRUE, OperationType.FREED);
                            break;
                        } else {
                            // 源预算池
                            String originPoolCode = detail.getOriginPoolCode();
                            if (StringUtils.isNotBlank(originPoolCode)) {
                                pool = poolService.getPool(originPoolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("源预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            // 记录预算池执行日志
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT_CANCEL, Boolean.TRUE, OperationType.FREED);
                            break;
                        }
                    default:
                        // 不支持的订单类型
                        resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
                // 标记处理完成
                detail.setProcessing(Boolean.FALSE);
                if (resultData.failed()) {
                    detail.setHasErr(Boolean.TRUE);
                    detail.setErrMsg(resultData.getMessage());
                } else {
                    detail.setState((short) -1);
                }
                // 更新行项
                orderDetailService.save(detail);

            } else {
                // 订单状态为[{0}],不允许操作!
                resultData = ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
            }
        } catch (Exception e) {
            LOG.error("撤销预算确认异常", e);
            resultData = ResultData.fail("撤销预算确认异常" + e.getMessage());
        } finally {
            // 本地线程全局变量存储-释放
            ThreadLocalHolder.end();
        }
        return resultData;
    }

    /**
     * 流程审批完成生效预算处理
     * 规则:释放预占用,更新正式占用或创建预算池
     *
     * @param detail 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> effectiveUseBudget(Order order, OrderDetail detail, SessionUser sessionUser) {
        if (detail.getState() < 0) {
            // 订单行项未被确认,不能生效
            return ResultData.fail(ContextUtil.getMessage("order_detail_00016"));
        }
        if (detail.getState() > 0) {
            // 已生效的
            return ResultData.success();
        }

        ResultData<Void> resultData;
        OrderStatus status = order.getStatus();
        // 已确认的,审批中的,生效中的可进行生效操作
        if (OrderStatus.CONFIRMED == status || OrderStatus.APPROVING == status || OrderStatus.EFFECTING == status) {
            Pool pool = null;
            // 当前预算池
            String poolCode = detail.getPoolCode();
            String code = order.getCode();
            String remark = order.getRemark();
            String detailId = detail.getId();
            resultData = ResultData.success();
            try {
                // 本地线程全局变量存储-开始
                ThreadLocalHolder.begin();

                mockUser.mockCurrentUser(sessionUser);

                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) <= 0) {
                            if (StringUtils.isBlank(poolCode)) {
                                // 预算池不存在,需要创建预算池
                                ResultData<Pool> result =
                                        poolService.createPool(order.getSubjectId(), order.getCategoryId(), order.getCurrencyCode(), order.getCurrencyName(),
                                                order.getManagerOrgCode(), order.getManagerOrgName(), order.getPeriodType(), detail, detail.getAmount(), detail.getAmount());
                                if (result.failed()) {
                                    resultData = ResultData.fail(result.getMessage());
                                    break;
                                }
                                pool = result.getData();
                                poolCode = pool.getCode();
                                detail.setPoolCode(poolCode);
                            } else {
                                pool = poolService.getPool(poolCode);
                                if (Objects.isNull(pool)) {
                                    LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                    // 预算池不存在
                                    resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                    break;
                                }
                            }
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_INJECTION);
                            }
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_INJECTION, Boolean.FALSE, OperationType.RELEASE);
                        }
                        break;
                    case ADJUSTMENT:
                        // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) <= 0) {
                            if (StringUtils.isNotBlank(poolCode)) {
                                pool = poolService.getPool(poolCode);
                            }
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                            if (StringUtils.isBlank(remark)) {
                                remark = eventService.getEventName(Constants.EVENT_BUDGET_ADJUSTMENT);
                            }
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_ADJUSTMENT, Boolean.TRUE, OperationType.RELEASE);
                        }
                        break;
                    case SPLIT:
                        // 预算分解
                        if (StringUtils.isBlank(remark)) {
                            remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT);
                        }
                        if (StringUtils.isBlank(poolCode)) {
                            // 预算池不存在,需要创建预算池
                            ResultData<Pool> result =
                                    poolService.createPool(order.getSubjectId(), order.getCategoryId(), order.getCurrencyCode(), order.getCurrencyName(),
                                            order.getManagerOrgCode(), order.getManagerOrgName(), order.getPeriodType(), detail, BigDecimal.ZERO, detail.getAmount());
                            if (result.failed()) {
                                resultData = ResultData.fail(result.getMessage());
                                break;
                            }
                            pool = result.getData();
                            poolCode = pool.getCode();
                            detail.setPoolCode(poolCode);
                        } else {
                            pool = poolService.getPool(poolCode);
                            if (Objects.isNull(pool)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                                break;
                            }
                        }
                        // 源预算池
                        Pool originPool = null;
                        String originPoolCode = detail.getOriginPoolCode();
                        if (StringUtils.isNotBlank(originPoolCode)) {
                            originPool = poolService.getPool(originPoolCode);
                        }
                        if (Objects.isNull(originPool)) {
                            LOG.error("源预算池不存在. - " + JsonUtils.toJson(detail));
                            // 预算池不存在
                            resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            break;
                        }
                        // 记录预算池执行日志
                        if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                            poolService.poolAmountLog(pool, detailId, code, remark,
                                    detail.getAmount().negate(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.FREED);

                            poolService.poolAmountLog(pool, detailId, code, ContextUtil.getMessage("order_detail_00018", remark, originPoolCode),
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);

                            poolService.poolAmountLog(originPool, detailId, code, ContextUtil.getMessage("order_detail_00019", remark, poolCode),
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                        } else {
                            poolService.poolAmountLog(pool, detailId, code, ContextUtil.getMessage("order_detail_00018", remark, originPoolCode),
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.RELEASE);

                            // 源预算池
                            poolService.poolAmountLog(originPool, detailId, code, remark,
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.FREED);

                            poolService.poolAmountLog(originPool, detailId, code, ContextUtil.getMessage("order_detail_00019", remark, poolCode),
                                    detail.getAmount(), Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                        }
                        break;
                    default:
                        // 不支持的订单类型
                        resultData = ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
                // 标记处理完成
                detail.setProcessing(Boolean.FALSE);
                if (resultData.failed()) {
                    detail.setHasErr(Boolean.TRUE);
                    detail.setErrMsg(resultData.getMessage());
                } else {
                    // 已生效
                    detail.setState((short) 1);
                }
                // 更新行项
                orderDetailService.save(detail);
            } catch (Exception e) {
                LOG.error("生效预算异常", e);
            } finally {
                // 本地线程全局变量存储-释放
                ThreadLocalHolder.end();
            }
        } else {
            // 订单状态为[{0}],不允许操作!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
        return resultData;
    }

}
