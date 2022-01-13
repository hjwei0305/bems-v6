package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dao.OrderDetailDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderStatistics;
import com.changhong.bems.dto.OrderStatus;
import com.changhong.bems.entity.*;
import com.changhong.bems.entity.vo.TemplateHeadVo;
import com.changhong.bems.service.cust.BudgetDimensionCustManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.context.mock.MockUser;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.util.EnumUtils;
import com.changhong.sei.util.thread.ThreadLocalHolder;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-22 01:14
 */
@Service
public class OrderCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderCommonService.class);

    private static final String NUM_REGEX = "-?[0-9]+.?[0-9]*";

    @Autowired
    private OrderDao dao;
    @Autowired
    private OrderDetailDao orderDetailDao;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private DimensionAttributeService dimensionAttributeService;
    @Autowired
    private EventService eventService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private BudgetDimensionCustManager budgetDimensionCustManager;
    @Autowired
    private MockUser mockUser;

    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderId, OrderStatus status, boolean processing) {
        // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
        dao.updateOrderStatus(orderId, status, processing);
        if (processing) {
            // 按订单id设置所有行项的处理状态为处理中
            orderDetailDao.setProcessing4All(orderId);
        } else {
            redisTemplate.expire(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId), 3, TimeUnit.SECONDS);
        }
    }

    /**
     * 添加预算申请单行项明细(导入使用)
     *
     * @param order        业务实体
     * @param templateHead 模版
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void importOrderDetails(Order order, List<TemplateHeadVo> templateHead, List<Map<Integer, String>> details) {
        if (Objects.isNull(order)) {
            //导入的订单头数据不能为空
            LOG.error(ContextUtil.getMessage("order_detail_00011"));
            return;
        }

        // 订单id
        final String orderId = order.getId();
        int detailSize = details.size();
        OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_import"), orderId, detailSize);
        BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX + orderId);
        // 设置默认过期时间:1天
        operations.set(statistics, 1, TimeUnit.HOURS);

        if (LOG.isDebugEnabled()) {
            LOG.debug("导入行项数: " + details.size());
        }

        // 通过预算类型获取预算维度组合
        ResultData<String> resultData = dimensionAttributeService.getAttribute(order.getCategoryId());
        if (resultData.failed()) {
            LOG.error(resultData.getMessage());
            return;
        }
        // 预算维度组合
        final String attribute = resultData.getData();

        Subject subject = subjectService.getSubject(order.getSubjectId());
        if (Objects.isNull(subject)) {
            LOG.error(ContextUtil.getMessage("subject_00003", order.getSubjectName()));
            return;
        }

        StopWatch stopWatch = new StopWatch("导入处理");
        stopWatch.start("导入数据预处理");
        SessionUser sessionUser = ContextUtil.getSessionUser();
        try {
            Map<String, String> periodMap = new HashMap<>(), subjectItemMap = new HashMap<>(), orgMap = new HashMap<>(),
                    projectMap = new HashMap<>(), costCenterMap = new HashMap<>(),
                    udf1Map = new HashMap<>(), udf2Map = new HashMap<>(), udf3Map = new HashMap<>(), udf4Map = new HashMap<>(), udf5Map = new HashMap<>();
            for (TemplateHeadVo headVo : templateHead) {
                // 期间
                if (Constants.DIMENSION_CODE_PERIOD.equals(headVo.getFiled())) {
                    periodMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_PERIOD));
                }
                // 科目
                else if (Constants.DIMENSION_CODE_ITEM.equals(headVo.getFiled())) {
                    subjectItemMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_ITEM));
                }
                // 组织
                else if (Constants.DIMENSION_CODE_ORG.equals(headVo.getFiled())) {
                    orgMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_ORG));
                }
                // 项目
                else if (Constants.DIMENSION_CODE_PROJECT.equals(headVo.getFiled())) {
                    projectMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_PROJECT));
                }
                // 成本中心
                else if (Constants.DIMENSION_CODE_COST_CENTER.equals(headVo.getFiled())) {
                    costCenterMap.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_COST_CENTER));
                }
                // 扩展1
                else if (Constants.DIMENSION_CODE_UDF1.equals(headVo.getFiled())) {
                    udf1Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF1));
                } else if (Constants.DIMENSION_CODE_UDF2.equals(headVo.getFiled())) {
                    udf2Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF2));
                } else if (Constants.DIMENSION_CODE_UDF3.equals(headVo.getFiled())) {
                    udf3Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF3));
                } else if (Constants.DIMENSION_CODE_UDF4.equals(headVo.getFiled())) {
                    udf4Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF4));
                } else if (Constants.DIMENSION_CODE_UDF5.equals(headVo.getFiled())) {
                    udf5Map.putAll(budgetDimensionCustManager.getDimensionNameValueMap(subject, Constants.DIMENSION_CODE_UDF5));
                }
            }

            LongAdder successes = new LongAdder();
            LongAdder failures = new LongAdder();
            // 记录所有hash值,以便识别出重复的行项
            Set<Long> duplicateHash = new CopyOnWriteArraySet<>();
            Map<Long, OrderDetail> detailMap = new ConcurrentHashMap<>(7);
            List<OrderDetail> orderDetails = orderDetailDao.findListByProperty(OrderDetail.FIELD_ORDER_ID, orderId);
            if (CollectionUtils.isNotEmpty(orderDetails)) {
                detailMap.putAll(orderDetails.stream().collect(Collectors.toMap(OrderDetail::getAttributeCode, o -> o)));
                orderDetails.clear();
            }

            details.parallelStream().forEach(data -> {
                OrderDetail detail = new OrderDetail();
                String temp;
                for (TemplateHeadVo headVo : templateHead) {
                    temp = data.get(headVo.getIndex());
                    if (StringUtils.isBlank(temp)) {
                        if (StringUtils.isBlank(detail.getPeriodName())) {
                            detail.setPeriodName("");
                        } else if (StringUtils.isBlank(detail.getItemName())) {
                            detail.setItemName("");
                        } else if (StringUtils.isBlank(detail.getOrgName())) {
                            detail.setOrgName("");
                        } else if (StringUtils.isBlank(detail.getProjectName())) {
                            detail.setProjectName("");
                        } else if (StringUtils.isBlank(detail.getCostCenterName())) {
                            detail.setCostCenterName("");
                        } else if (StringUtils.isBlank(detail.getUdf1Name())) {
                            detail.setUdf1Name("");
                        } else if (StringUtils.isBlank(detail.getUdf2Name())) {
                            detail.setUdf2Name("");
                        } else if (StringUtils.isBlank(detail.getUdf3Name())) {
                            detail.setUdf3Name("");
                        } else if (StringUtils.isBlank(detail.getUdf4Name())) {
                            detail.setUdf4Name("");
                        } else if (StringUtils.isBlank(detail.getUdf5Name())) {
                            detail.setUdf5Name("");
                        }
                        detail.setHasErr(Boolean.TRUE);
                        // 存在错误的导入数据
                        detail.setErrMsg(ContextUtil.getMessage("order_detail_00023"));
                    } else {
                        // 期间
                        if (Constants.DIMENSION_CODE_PERIOD.equals(headVo.getFiled())) {
                            detail.setPeriodName(temp);
                            String periodId = periodMap.get(temp);
                            if (StringUtils.isNotBlank(periodId)) {
                                detail.setPeriod(periodId);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的预算期间数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_period")));
                            }
                        }
                        // 科目
                        else if (Constants.DIMENSION_CODE_ITEM.equals(headVo.getFiled())) {
                            detail.setItemName(temp);
                            String itemCode = subjectItemMap.get(temp);
                            if (StringUtils.isNotBlank(itemCode)) {
                                detail.setItem(itemCode);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的预算科目数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_item")));
                            }
                        }
                        // 组织
                        else if (Constants.DIMENSION_CODE_ORG.equals(headVo.getFiled())) {
                            detail.setOrgName(temp);
                            String orgId = orgMap.get(temp);
                            if (StringUtils.isNotBlank(orgId)) {
                                detail.setOrg(orgId);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的组织数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_org")));
                            }
                        }
                        // 项目
                        else if (Constants.DIMENSION_CODE_PROJECT.equals(headVo.getFiled())) {
                            detail.setProjectName(temp);
                            String projectId = projectMap.get(temp);
                            if (StringUtils.isNotBlank(projectId)) {
                                detail.setProject(projectId);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的公司项目数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_project")));
                            }
                        }
                        // 成本中心
                        else if (Constants.DIMENSION_CODE_COST_CENTER.equals(headVo.getFiled())) {
                            detail.setCostCenterName(temp);
                            String costCenter = costCenterMap.get(temp);
                            if (StringUtils.isNotBlank(costCenter)) {
                                detail.setCostCenter(costCenter);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的成本中心数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_cost_center")));
                            }
                        }
                        // 扩展1
                        else if (Constants.DIMENSION_CODE_UDF1.equals(headVo.getFiled())) {
                            detail.setUdf1Name(temp);
                            String udf1 = udf1Map.get(temp);
                            if (StringUtils.isNotBlank(udf1)) {
                                detail.setUdf1(udf1);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的扩展维度1数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf1")));
                            }
                        } else if (Constants.DIMENSION_CODE_UDF2.equals(headVo.getFiled())) {
                            detail.setUdf2Name(temp);
                            String udf2 = udf2Map.get(temp);
                            if (StringUtils.isNotBlank(udf2)) {
                                detail.setUdf2(udf2);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的扩展维度2数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf2")));
                            }
                        } else if (Constants.DIMENSION_CODE_UDF3.equals(headVo.getFiled())) {
                            detail.setUdf3Name(temp);
                            String udf3 = udf3Map.get(temp);
                            if (StringUtils.isNotBlank(udf3)) {
                                detail.setUdf3(udf3);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的扩展维度3数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf3")));
                            }
                        } else if (Constants.DIMENSION_CODE_UDF4.equals(headVo.getFiled())) {
                            detail.setUdf4Name(temp);
                            String udf4 = udf4Map.get(temp);
                            if (StringUtils.isNotBlank(udf4)) {
                                detail.setUdf4(udf4);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的扩展维度4数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf4")));
                            }
                        } else if (Constants.DIMENSION_CODE_UDF5.equals(headVo.getFiled())) {
                            detail.setUdf5Name(temp);
                            String udf5 = udf5Map.get(temp);
                            if (StringUtils.isNotBlank(udf5)) {
                                detail.setUdf5(udf5);
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 错误的扩展维度5数据
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00017", ContextUtil.getMessage("default_dimension_udf5")));
                            }
                        } else if (OrderDetail.FIELD_AMOUNT.equals(headVo.getFiled())) {
                            if (temp.matches(NUM_REGEX)) {
                                detail.setAmount(new BigDecimal(temp));
                            } else {
                                detail.setHasErr(Boolean.TRUE);
                                // 导入的金额不是数字
                                detail.setErrMsg(ContextUtil.getMessage("order_detail_00015"));
                            }
                        }
                    }
                }
                // 订单id
                detail.setOrderId(orderId);
                // 维度属性组合
                detail.setAttribute(attribute);

                this.putOrderDetail(Boolean.TRUE, order, detail, detailMap, duplicateHash, successes, failures, sessionUser);

                OrderStatistics orderStatistics = new OrderStatistics(ContextUtil.getMessage("task_name_import"), orderId, detailSize);
                orderStatistics.setSuccesses(successes.intValue());
                orderStatistics.setFailures(failures.intValue());
                // 更新缓存
                redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX + orderId, orderStatistics, 1, TimeUnit.HOURS);
            });
        } catch (ServiceException e) {
            LOG.error("异步导入单据行项异常", e);
        } finally {
            dao.setProcessStatus(orderId, Boolean.FALSE);
            // 清除缓存
            redisTemplate.expire(Constants.HANDLE_CACHE_KEY_PREFIX + orderId, 3, TimeUnit.SECONDS);
        }
    }

    /**
     * 异步写入行项数据
     * 调用方是在多线程环境下,因此需要在方法体中模拟用户会话以传递当前用户
     *
     * @param isCover       当存在相同维度值时,是否对金额做覆盖处理
     * @param order         订单头
     * @param detail        订单行项
     * @param detailMap     当前订单已存在的行项.用于与原订单数据的操作,结合isCover对金额进行控制
     * @param duplicateHash 本次添加过的行项.用于本次重复检查
     * @param successes     本次成功数
     * @param failures      本次失败数
     * @param sessionUser   当前会话用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void putOrderDetail(boolean isCover, Order order, OrderDetail detail, Map<Long, OrderDetail> detailMap,
                               Set<Long> duplicateHash, LongAdder successes, LongAdder failures, SessionUser sessionUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("正在处理行项: " + JsonUtils.toJson(detail));
        }
        // 本地线程全局变量存储-开始
        ThreadLocalHolder.begin();
        try {
            mockUser.mockCurrentUser(sessionUser);

            detail.setTenantCode(order.getTenantCode());
            // 创建时间
            detail.setCreatedDate(LocalDateTime.now());
            if (detail.getHasErr()) {
                // 错误数加1
                failures.increment();
            }
            // 本次提交数据中存在重复项
            else if (duplicateHash.contains(detail.getAttributeCode())) {
                // 有错误的
                detail.setHasErr(Boolean.TRUE);
                // 存在重复项
                detail.setErrMsg(ContextUtil.getMessage("order_detail_00006"));
                // 错误数加1
                failures.increment();
            } else {
                // 记录hash值
                duplicateHash.add(detail.getAttributeCode());
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
                        detail = null;
                    }
                }

                if (Objects.nonNull(detail)) {
                    ResultData<Void> result;
                    try {
                        // 设置行项数据的预算池及当前可用额度
                        result = this.createDetail(order, detail);
                    } catch (Exception e) {
                        result = ResultData.fail(ExceptionUtils.getRootCauseMessage(e));
                    }
                    if (result.successful()) {
                        successes.increment();
                    } else {
                        failures.increment();
                        // 有错误的
                        detail.setHasErr(Boolean.TRUE);
                        detail.setErrMsg(result.getMessage());
                    }
                }
            }
            if (Objects.nonNull(detail)) {
                orderDetailDao.save(detail);
            }
        } catch (Exception e) {
            LOG.error("创建预算明细异常", e);
        } finally {
            // 本地线程全局变量存储-释放
            ThreadLocalHolder.end();
        }
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
        int detailSize = details.size();
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            this.cancelSplitConfirmUseBudget(order, details, sessionUser, successes, failures);
        } else {
            details.parallelStream().forEach(detail -> {
                OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_cancel"), orderId, detailSize);
                ResultData<Void> result = ResultData.fail("Unknown error");
                try {
                    // 本地线程全局变量存储-开始
                    ThreadLocalHolder.begin();
                    mockUser.mockCurrentUser(sessionUser);

                    result = this.cancelConfirmUseBudget(order, detail);
                } catch (Exception e) {
                    result = ResultData.fail(e.getMessage());
                } finally {
                    // 本地线程全局变量存储-释放
                    ThreadLocalHolder.end();
                    this.pushProcessState(successes, failures, statistics, result);
                }
            });
        }
        // 若处理完成,则更新订单状态为:草稿
        this.updateOrderStatus(orderId, OrderStatus.DRAFT, Boolean.FALSE);
    }

    /**
     * 异步直接生效预算
     */
    @Async
    // @Transactional(rollbackFor = Exception.class)
    public void asyncDirectlyEffective(Order order, List<OrderDetail> details, SessionUser sessionUser) {
        LongAdder successes = new LongAdder();
        LongAdder failures = new LongAdder();
        String orderId = order.getId();
        int detailSize = details.size();

        OrderCommonService service = ContextUtil.getBean(OrderCommonService.class);

        StopWatch stopWatch = new StopWatch(order.getCode());
        stopWatch.start("预算确认");
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            // 分解确认
            service.confirmSplitUseBudget(order, details, sessionUser, successes, failures);
            stopWatch.stop();

            if (failures.intValue() > 0) {
                // 若处理完成,则更新订单状态为:已生效
                service.updateOrderStatus(orderId, OrderStatus.DRAFT, Boolean.FALSE);
            } else {
                stopWatch.start("生效预算");
                successes.reset();
                failures.reset();
                service.effectiveSplitUseBudget(order, details, sessionUser, successes, failures);
                // 若处理完成,则更新订单状态为:已生效
                service.updateOrderStatus(orderId, OrderStatus.COMPLETED, Boolean.FALSE);
                stopWatch.stop();
            }
        } else {
            int cupNum = Runtime.getRuntime().availableProcessors();
            LogUtil.bizLog("CPU num:{}", cupNum);
            details.parallelStream().forEach(detail -> {
                // OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_confirm"), orderId, detailSize);
                // ResultData<Void> result = ResultData.fail("Unknown error");
                try {
                    // 本地线程全局变量存储-开始
                    ThreadLocalHolder.begin();
                    mockUser.mockCurrentUser(sessionUser);

                    service.confirmUseBudget(order, detail);
                } catch (Exception e) {
                    LogUtil.error("订单[" + order.getCode() + "]行项[" + JsonUtils.toJson(detail) + "]直接生效异常", e);
                    // result = ResultData.fail(e.getMessage());
                } finally {
                    // 本地线程全局变量存储-释放
                    ThreadLocalHolder.end();
                    // this.pushProcessState(successes, failures, statistics, result);
                }
            });
            stopWatch.stop();

            if (failures.intValue() > 0) {
                orderDetailDao.save(details);
                // 若处理完成,则更新订单状态为:已生效
                service.updateOrderStatus(orderId, OrderStatus.DRAFT, Boolean.FALSE);
            } else {
                stopWatch.start("生效预算");
                successes.reset();
                failures.reset();
                ForkJoinPool customThreadPool = new ForkJoinPool(cupNum - 2);
                try {
                    customThreadPool.submit(
                            () -> details.parallelStream().forEach(detail -> {
                                ResultData<Void> result = service.effectiveUseBudget(order, detail, sessionUser);
                                OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_effective"), orderId, detailSize);
                                this.pushProcessState(successes, failures, statistics, result);
                            })).get();
                } catch (Exception e) {
                    LOG.error("并发异常", e);
                } finally {
                    customThreadPool.shutdown();
                }
                // 若处理完成,则更新订单状态为:已生效
                service.updateOrderStatus(orderId, OrderStatus.COMPLETED, Boolean.FALSE);
                stopWatch.stop();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("生效预算总记录数:{},总耗时: {}", detailSize, stopWatch.prettyPrint());
        }
    }

    /**
     * 异步生效预算
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void asyncApprovedEffective(Order order, List<OrderDetail> details, SessionUser sessionUser) {
        LongAdder successes = new LongAdder();
        LongAdder failures = new LongAdder();
        String orderId = order.getId();
        int detailSize = details.size();
        details.parallelStream().forEach(detail -> {
            ResultData<Void> result = this.effectiveUseBudget(order, detail, sessionUser);
            OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_effective"), orderId, detailSize);
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
        redisTemplate.opsForValue().set(Constants.HANDLE_CACHE_KEY_PREFIX + statistics.getOrderId(), statistics, 1, TimeUnit.HOURS);
    }

    /**
     * 确认预算申请单
     * 规则:预算池进行预占用
     *
     * @param order  预算申请单
     * @param detail 预算申请单行项
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultData<Void> confirmUseBudget(Order order, OrderDetail detail) {
        String poolCode;
        Pool pool = null;
        String code = order.getCode();
        String remark = order.getRemark();
        ResultData<Void> resultData = ResultData.success();
        try {
            // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
            switch (order.getOrderCategory()) {
                case INJECTION:
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        resultData = this.checkInjectionDetail(order, detail);
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
                        resultData = this.checkAdjustmentDetail(order, detail);
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
                    resultData = this.checkSplitDetail(order, detail);
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
                detail.setHasErr(Boolean.FALSE);
                detail.setErrMsg("");
            }
        } catch (Exception e) {
            LOG.error("预算确认异常", e);
            detail.setHasErr(Boolean.TRUE);
            detail.setErrMsg(e.getMessage());
            resultData = ResultData.fail(e.getMessage());
        }
        return resultData;
    }

    /**
     * 确认预算申请单
     * 规则:预算池进行预占用
     * 为解决分解性能和并行事务问题:按源预算池分组,并发分组处理,组内串行执行
     *
     * @param order   预算申请单
     * @param details 预算申请单行项
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void confirmSplitUseBudget(Order order, List<OrderDetail> details, SessionUser sessionUser,
                                      LongAdder successes, LongAdder failures) {
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            String orderId = order.getId();
            int detailSize = details.size();
            OrderCommonService service = ContextUtil.getBean(OrderCommonService.class);

            // 按分解源预算池分组
            Map<String, List<OrderDetail>> groupMap = details.stream()
                    // 过滤无效的源预算池
                    .filter(od -> StringUtils.isNotBlank(od.getOriginPoolCode()) && !StringUtils.equals(Constants.NONE, od.getOriginPoolCode()))
                    .collect(Collectors.groupingBy(OrderDetail::getOriginPoolCode));
            Collection<List<OrderDetail>> groupList = groupMap.values();
            groupList.parallelStream().forEach(detailList -> {
                OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_confirm"), orderId, detailSize);
                // 预算分解
                ResultData<Void> resultData = ResultData.success();
                try {
                    // 本地线程全局变量存储-开始
                    ThreadLocalHolder.begin();
                    mockUser.mockCurrentUser(sessionUser);

                    // 分解(年度到月度,总额不变.允许目标预算池不存在,源预算池必须存在)
                    // 源预算池代码
                    String originPoolCode = null;
                    BigDecimal sumAmount = BigDecimal.ZERO;
                    for (OrderDetail orderDetail : detailList) {
                        originPoolCode = orderDetail.getOriginPoolCode();
                        sumAmount = sumAmount.add(orderDetail.getAmount());
                    }
                    // 当前预算池余额. 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
                    BigDecimal originBalance = poolService.getPoolBalanceByCode(originPoolCode);
                    // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                    if (BigDecimal.ZERO.compareTo(originBalance.subtract(sumAmount)) > 0) {
                        for (OrderDetail orderDetail : detailList) {
                            // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                            orderDetail.setErrMsg(ContextUtil.getMessage("pool_00002", originPoolCode, originBalance, orderDetail.getAmount()));
                            orderDetail.setState((short) -1);
                            orderDetail.setHasErr(Boolean.TRUE);
                        }
                        orderDetailDao.save(detailList);
                    } else {
                        // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                        if (BigDecimal.ZERO.compareTo(sumAmount) < 0) {
                            // 源预算池
                            Pool pool = poolService.getPool(originPoolCode);
                            if (Objects.isNull(pool)) {
                                LogUtil.error("源预算池不存在: {}", originPoolCode);
                                // 预算池不存在
                                resultData = ResultData.fail(ContextUtil.getMessage("pool_00005"));
                            } else {
                                String remark = order.getRemark();
                                if (StringUtils.isBlank(remark)) {
                                    remark = eventService.getEventName(Constants.EVENT_BUDGET_SPLIT);
                                }
                                // 记录预算池执行日志
                                poolService.poolAmountLog(pool, order.getCode(), order.getCode(), remark,
                                        sumAmount, Constants.EVENT_BUDGET_SPLIT, Boolean.TRUE, OperationType.USE);
                            }

                            if (resultData.successful()) {
                                for (OrderDetail orderDetail : detailList) {
                                    // 标记处理完成
                                    orderDetail.setProcessing(Boolean.FALSE);
                                    if (resultData.failed()) {
                                        orderDetail.setState((short) -1);
                                        orderDetail.setHasErr(Boolean.TRUE);
                                        orderDetail.setErrMsg(resultData.getMessage());
                                    } else {
                                        // 预占用成功
                                        orderDetail.setState((short) 0);
                                        orderDetail.setHasErr(Boolean.FALSE);
                                        orderDetail.setErrMsg("");
                                    }
                                }
                                orderDetailDao.save(detailList);
                            }
                        } else {
                            // 同一个源预算池的分解,串行执行,避免并发事务问题
                            for (OrderDetail detail : detailList) {
                                resultData = service.confirmUseBudget(order, detail);
                                this.pushProcessState(successes, failures, statistics, resultData);
                            }
                            orderDetailDao.save(detailList);
                        }
                    }
                } catch (Exception e) {
                    resultData = ResultData.fail(e.getMessage());
                } finally {
                    // 本地线程全局变量存储-释放
                    ThreadLocalHolder.end();
                    this.pushProcessState(successes, failures, statistics, resultData);
                }
            });
        }
    }

    /**
     * 取消已确认的预算申请单
     * 规则:释放预占用
     *
     * @param detail 预算申请单行项
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> cancelConfirmUseBudget(Order order, OrderDetail detail) {
        if (detail.getState() < 0) {
            // 未成功预占用,不用做释放
            return ResultData.success();
        }
        ResultData<Void> resultData;
        try {
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
        } catch (Exception e) {
            LOG.error("撤销预算确认异常", e);
            detail.setHasErr(Boolean.TRUE);
            detail.setErrMsg(e.getMessage());
            resultData = ResultData.fail(e.getMessage());
        }
        return resultData;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelSplitConfirmUseBudget(Order order, List<OrderDetail> details, SessionUser sessionUser, LongAdder successes, LongAdder failures) {
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            String orderId = order.getId();
            int detailSize = details.size();
            OrderCommonService service = ContextUtil.getBean(OrderCommonService.class);

            Map<String, List<OrderDetail>> groupMap = details.stream().collect(Collectors.groupingBy(OrderDetail::getOriginPoolCode));
            Collection<List<OrderDetail>> groupList = groupMap.values();
            groupList.parallelStream().forEach(detailList -> {
                OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_cancel"), orderId, detailSize);
                ResultData<Void> result = ResultData.fail("Unknown error");
                try {
                    // 本地线程全局变量存储-开始
                    ThreadLocalHolder.begin();
                    mockUser.mockCurrentUser(sessionUser);

                    for (OrderDetail detail : detailList) {
                        result = service.cancelConfirmUseBudget(order, detail);

                        this.pushProcessState(successes, failures, statistics, result);
                    }
                } catch (Exception e) {
                    result = ResultData.fail(e.getMessage());
                } finally {
                    // 本地线程全局变量存储-释放
                    ThreadLocalHolder.end();
                    this.pushProcessState(successes, failures, statistics, result);
                }
            });
        }
    }

    /**
     * 流程审批完成生效预算处理
     * 规则:释放预占用,更新正式占用或创建预算池
     *
     * @param detail 预算申请单行项
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultData<Void> effectiveUseBudget(Order order, OrderDetail detail, SessionUser sessionUser) {
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
        if (OrderStatus.DRAFT == status || OrderStatus.APPROVING == status || OrderStatus.EFFECTING == status) {
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
                orderDetailDao.save(detail);
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

    @Transactional(rollbackFor = Exception.class)
    public void effectiveSplitUseBudget(Order order, List<OrderDetail> details, SessionUser sessionUser, LongAdder successes, LongAdder failures) {
        if (OrderCategory.SPLIT == order.getOrderCategory()) {
            String orderId = order.getId();
            int detailSize = details.size();
            OrderCommonService service = ContextUtil.getBean(OrderCommonService.class);

            Map<String, List<OrderDetail>> groupMap = details.stream().collect(Collectors.groupingBy(OrderDetail::getOriginPoolCode));
            Collection<List<OrderDetail>> groupList = groupMap.values();
            groupList.parallelStream().forEach(detailList -> {
                OrderStatistics statistics = new OrderStatistics(ContextUtil.getMessage("task_name_effective"), orderId, detailSize);
                ResultData<Void> result;
                for (OrderDetail detail : detailList) {
                    result = service.effectiveUseBudget(order, detail, sessionUser);
                    this.pushProcessState(successes, failures, statistics, result);
                }
            });
        }
    }

    /**
     * 设置行项数据的预算池及可用额度
     *
     * @param order  订单头
     * @param detail 订单行项
     */
    private ResultData<Void> createDetail(Order order, OrderDetail detail) {
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
                Pool parentPeriodPool = poolService.getParentPeriodBudgetPool(subjectId, detail);
                if (Objects.isNull(parentPeriodPool)) {
                    // 添加单据行项时,上级期间预算池未找到.
                    return ResultData.fail(ContextUtil.getMessage("order_detail_00005"));
                }
                // 获取上级期间源预算池
                detail.setOriginPoolCode(parentPeriodPool.getCode());
                detail.setOriginPoolAmount(parentPeriodPool.getBalance());
                // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                if (BigDecimal.ZERO.compareTo(detail.getOriginPoolAmount().subtract(detail.getAmount())) > 0) {
                    // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                    return ResultData.fail(ContextUtil.getMessage("pool_00002", detail.getOriginPoolCode(), detail.getOriginPoolAmount(), detail.getAmount()));
                }

                resultData = poolService.getPool(subjectId, detail.getAttributeCode());
                if (resultData.successful()) {
                    Pool pool = resultData.getData();
                    detail.setPoolCode(pool.getCode());
                    detail.setPoolAmount(pool.getBalance());
                    // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                    if (BigDecimal.ZERO.compareTo(detail.getPoolAmount().add(detail.getAmount())) > 0) {
                        // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                        return ResultData.fail(ContextUtil.getMessage("pool_00002", detail.getPoolCode(), detail.getPoolAmount(), detail.getAmount()));
                    }
                } else {
                    // 当预算池不存在时,发生金额不能小于0(不能将预算池值为负数)
                    if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
                        // 预算池金额不能值为负数[{0}]
                        return ResultData.fail(ContextUtil.getMessage("pool_00004", detail.getAmount()));
                    }
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
            // 发生金额小于0,需检查控制预算池不能为负数
            if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
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
                    BigDecimal balance = poolService.getPoolBalanceByCode(poolCode);
                    // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                    if (BigDecimal.ZERO.compareTo(balance.add(detail.getAmount())) > 0) {
                        // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                        return ResultData.fail(ContextUtil.getMessage("pool_00002", poolCode, balance, detail.getAmount()));
                    }
                    detail.setPoolAmount(balance);
                } else {
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
            BigDecimal balance = poolService.getPoolBalanceByCode(poolCode);
            // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
            if (BigDecimal.ZERO.compareTo(balance.add(detail.getAmount())) > 0) {
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
            if (StringUtils.isBlank(originPoolCode) || StringUtils.equals(Constants.NONE, originPoolCode)) {
                // 分解源预算池不存在.
                return ResultData.fail(ContextUtil.getMessage("order_detail_00010"));
            }
            // 当前预算池余额. 检查预算池可用余额是否满足本次发生金额(主要存在注入负数调减的金额)
            BigDecimal originBalance = poolService.getPoolBalanceByCode(originPoolCode);
            // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
            if (BigDecimal.ZERO.compareTo(originBalance.subtract(detail.getAmount())) > 0) {
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
                BigDecimal balance = poolService.getPoolBalanceByCode(poolCode);
                // 当前预算池余额 + 发生金额 >= 0  不能小于0,使预算池变为负数
                if (BigDecimal.ZERO.compareTo(balance.add(detail.getAmount())) > 0) {
                    // 当前预算池[{0}]余额[{1}]不满足本次发生金额[{2}].
                    return ResultData.fail(ContextUtil.getMessage("pool_00002", poolCode, balance, detail.getAmount()));
                }
                detail.setPoolAmount(balance);
            } else {
                // 当预算池不存在时,发生金额不能小于0(不能将预算池值为负数)
                if (BigDecimal.ZERO.compareTo(detail.getAmount()) > 0) {
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

}
