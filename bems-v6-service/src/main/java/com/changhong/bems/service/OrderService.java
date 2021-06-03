package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.service.client.FlowClient;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.limiter.support.lock.SeiLock;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.serial.sdk.SerialService;
import com.changhong.sei.util.ArithUtils;
import com.changhong.sei.utils.AsyncRunUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 预算申请单(Order)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Service
public class OrderService extends BaseEntityService<Order> {
    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);
    @Autowired
    private OrderDao dao;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private OrganizationManager organizationManager;
    @Autowired(required = false)
    private SerialService serialService;
    @Autowired
    private PoolService poolService;
    @Autowired
    private FlowClient flowClient;

    @Override
    protected BaseEntityDao<Order> getDao() {
        return dao;
    }

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    public ResultData<List<OrganizationDto>> findOrgTree() {
        return organizationManager.findOrgTreeWithoutFrozen();
    }

    /**
     * 通过单据Id获取单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    public PageResult<OrderDetail> getOrderItems(String orderId, Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        search.addFilter(new SearchFilter(OrderDetail.FIELD_ORDER_ID, orderId));
        return orderDetailService.findByPage(search);
    }

    /**
     * 检查是否存在指定类型的预制单
     *
     * @return 返回检查结果
     */
    public Order getPrefabExist(OrderCategory category) {
        Search search = Search.createSearch();
        // 创建人
        search.addFilter(new SearchFilter(Order.FIELD_CREATOR_ID, ContextUtil.getUserId()));
        // 类型
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, category));
        // 预制状态
        search.addFilter(new SearchFilter(Order.FIELD_STATUS, OrderStatus.PREFAB));
        return dao.findFirstByFilters(search);
    }

    /**
     * 通过单据Id检查预算主体和类型是否被修改
     *
     * @param orderId    单据Id
     * @param subjectId  主体id
     * @param categoryId 类型id
     * @return 业务实体
     */
    public ResultData<Void> checkAndGetDimension(String orderId, String subjectId, String categoryId) {
        if (StringUtils.isNotBlank(orderId)) {
            // 通过orderId查询单据
            Order order = dao.findOne(orderId);
            if (Objects.nonNull(order)) {
                OrderDetail detail = orderDetailService.findFirstByProperty(OrderDetail.FIELD_ORDER_ID, orderId);
                if (Objects.nonNull(detail)) {
                    //通过单据保存的主体和类型进行比较,是否一致
                    if (!StringUtils.equals(subjectId, order.getSubjectId())) {
                        // 预算主体不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00002", order.getSubjectName()));
                    }
                    if (!StringUtils.equals(categoryId, order.getCategoryId())) {
                        // 预算类型不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00003", order.getCategoryName()));
                    }
                }
            }
        }
        return ResultData.success();
    }

    /**
     * 通过单据Id检查预算主体和类型是否被修改
     *
     * @param orderId    单据Id
     * @param subjectId  主体id
     * @param categoryId 类型id
     * @return 业务实体
     */
    public ResultData<String> checkDimension(String orderId, String subjectId, String categoryId) {
        if (StringUtils.isNotBlank(orderId)) {
            // 通过orderId查询单据
            Order order = dao.findOne(orderId);
            if (Objects.nonNull(order)) {
                OrderDetail detail = orderDetailService.findFirstByProperty(OrderDetail.FIELD_ORDER_ID, orderId);
                if (Objects.nonNull(detail)) {
                    //通过单据保存的主体和类型进行比较,是否一致
                    if (!StringUtils.equals(subjectId, order.getSubjectId())) {
                        // 预算主体不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00002", order.getSubjectName()));
                    }
                    if (!StringUtils.equals(categoryId, order.getCategoryId())) {
                        // 预算类型不是[{0}]
                        return ResultData.fail(ContextUtil.getMessage("order_00003", order.getCategoryName()));
                    }
                }
            }
        }
        return ResultData.success(orderId);
    }

    /**
     * 添加预算申请单行项明细
     *
     * @param orderDto 业务实体DTO
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<String> addOrderDetails(AddOrderDetail orderDto) {
        ResultData<String> resultData = this.checkDimension(orderDto.getId(), orderDto.getSubjectId(), orderDto.getCategoryId());
        if (resultData.failed()) {
            return resultData;
        }
        Order order = modelMapper.map(orderDto, Order.class);
        // 保存订单头
        ResultData<Order> orderResult = this.saveOrder(order, null);
        if (orderResult.successful()) {
            // 异步生成订单行项
            orderDetailService.batchAddOrderItems(order, orderDto);
            resultData = ResultData.success(order.getId());
        } else {
            resultData = ResultData.fail(orderResult.getMessage());
        }
        return resultData;
    }

    /**
     * 保存预算申请单
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Order> saveOrder(Order order, List<OrderDetail> details) {
        if (StringUtils.isBlank(order.getId())) {
            order.setCode(serialService.getNumber(Order.class, ContextUtil.getTenantCode()));
        } else {
            Order entity = dao.findOne(order.getId());
            if (Objects.nonNull(entity)) {
                order.setCode(entity.getCode());
                order.setCreatorId(entity.getCreatorId());
                order.setCreatorAccount(entity.getCreatorAccount());
                order.setCreatorName(entity.getCreatorName());
                order.setCreatedDate(entity.getCreatedDate());
                order.setApplyAmount(entity.getApplyAmount());
            }
        }
        OperateResultWithData<Order> result = this.save(order);
        if (result.successful()) {
            if (CollectionUtils.isNotEmpty(details)) {
                ResultData<Void> resultData = orderDetailService.updateAmount(order, details);
                if (resultData.failed()) {
                    // 回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResultData.fail(resultData.getMessage());
                }
            }
            // 订单总金额
            dao.updateAmount(order.getId());

            return ResultData.success(order);
        } else {
            return ResultData.fail(result.getMessage());
        }
    }

    /**
     * 更新订单状态
     *
     * @param id     订单id
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, OrderStatus status) {
        dao.updateStatus(id, status);
    }

    /**
     * 更新订单总金额
     *
     * @param id 订单id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAmount(String id) {
        dao.updateAmount(id);
    }

    /**
     * 生效预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> effective(String orderId) {
        ResultData<Void> resultData = ResultData.success();
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        if (resultData.successful()) {
            // 检查订单状态
            if (OrderStatus.EFFECTING == order.getStatus()) {
                List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
                resultData = this.effectiveUseBudget(order, details);
                if (resultData.successful()) {
                    // 更新订单状态为:完成
                    dao.updateStatus(orderId, OrderStatus.COMPLETED);
                    // 更新订单为手动生效标示
                    dao.manuallyEffective(orderId, Boolean.TRUE);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("预算申请单[" + order.getCode() + "]生效成功!");
                    }
                } else {
                    // 回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    LOG.error("预算申请单[" + order.getCode() + "]生效错误: " + resultData.getMessage());
                }
            } else {
                // 订单状态为[{0}],不允许操作!
                resultData = ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
            }
        }
        return resultData;
    }

    /**
     * 提交审批预算申请单
     * 对不存在预算池的行项,在此时不创建预算池,仅对金额为负数的按一般占用处理(预占用)
     *
     * @param order        申请单
     * @param details      申请单行项
     * @param taskActDefId flow接收任务回调id
     * @return 返回处理结果
     */
    @SeiLock(key = "'bems-v6:submit:' + #order.id")
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> submitProcess(Order order, List<OrderDetail> details, String taskActDefId) {
        ResultData<Void> resultData;
        if (Objects.nonNull(order)) {
            // 检查订单状态
            if (OrderStatus.PROCESSING == order.getStatus()) {
                resultData = this.submitProcessUseBudget(order, details);
            } else {
                // 订单状态为[{0}],不允许操作!
                resultData = ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
            }
        } else {
            // 订单不存在!
            resultData = ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("[{}]提交流程异步处理结果: {}", order.getCode(), resultData.getMessage());
        }
        try {
            if (resultData.failed()) {
                // 回调flow通知接收任务退出流程
                resultData = flowClient.endByBusinessId(order.getId());
            } else {
                // 回调flow通知接收任务继续执行
                resultData = flowClient.signalByBusinessId(order.getId(), taskActDefId, new HashMap<>(7));
            }
        } catch (Exception e) {
            try {
                // 回调flow通知接收任务退出流程
                flowClient.endByBusinessId(order.getId());
            } catch (Exception ignored) {
            }
            LOG.error("回调flow通知接收任务异常", e);
            resultData = ResultData.fail("回调flow通知接收任务异常.");
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("[{}]回调flow通知接收任务结果: {}", order.getCode(), resultData.getMessage());
        }
        if (resultData.failed()) {
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return resultData;
    }

    /**
     * 预算申请单取消流程审批
     *
     * @param order 申请单
     * @return 返回处理结果
     */
    @SeiLock(key = "'bems-v6:cancel:' + #order.id")
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> cancelProcess(Order order) {
        if (Objects.isNull(order)) {
            // 订单不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        if (OrderStatus.PROCESSING != order.getStatus()) {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());

        ResultData<Void> resultData = this.cancelProcessUseBudget(order, details);
        if (resultData.successful()) {
            // 更新订单状态为:草稿
            order.setStatus(OrderStatus.DRAFT);
            dao.save(order);
        }
        return resultData;
    }

    /**
     * 预算申请单审批完成
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> completeProcess(String orderId) {
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单不存在
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        if (OrderStatus.PROCESSING != order.getStatus()) {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());
        // 检查是否存在错误行项
        ResultData<Void> resultData = this.completeProcessUseBudget(order, details);
        if (resultData.successful()) {
            // 更新订单状态为:完成
            order.setStatus(OrderStatus.COMPLETED);
            dao.save(order);
            resultData = ResultData.success();
        }
        return resultData;
    }

    /**
     * 检查行项是否有错误未处理
     *
     * @param details 预算申请单行项
     * @return 返回处理结果
     */
    public ResultData<Void> checkDetailHasErr(List<OrderDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            if (details.parallelStream().anyMatch(OrderDetail::getHasErr)) {
                // 存在错误行项未处理
                return ResultData.fail(ContextUtil.getMessage("order_detail_00008"));
            }
        }
        return ResultData.success();
    }

    /**
     * 直接生效预算处理
     * 规则:更新或创建预算池
     *
     * @param order   预算申请单
     * @param details 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> effectiveUseBudget(Order order, List<OrderDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            String remark;
            String poolCode;
            ExecutionRecord record;
            ResultData<Void> resultData;
            // 调整时总额不变(调增调减之和等于0)
            double adjustBalance = 0;
            OperationType operation = OperationType.RELEASE;
            for (OrderDetail detail : details) {
                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        if (detail.getAmount() < 0) {
                            resultData = orderDetailService.checkInjectionDetail(order, detail);
                            if (resultData.failed()) {
                                return resultData;
                            }
                        }
                        poolCode = detail.getPoolCode();
                        if (StringUtils.isBlank(poolCode)) {
                            // 预算池不存在,需要创建预算池
                            ResultData<Pool> result = poolService.createPool(order, detail);
                            if (result.failed()) {
                                return ResultData.fail(result.getMessage());
                            }
                            Pool pool = result.getData();
                            poolCode = pool.getCode();
                            detail.setPoolCode(poolCode);
                            detail.setPoolAmount(pool.getBalance());
                        }
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_INJECTION_EFFECTIVE);
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case ADJUSTMENT:
                        // 计算调整余额
                        adjustBalance = ArithUtils.add(adjustBalance, detail.getAmount());

                        resultData = orderDetailService.checkAdjustmentDetail(order, detail);
                        if (resultData.failed()) {
                            return resultData;
                        }
                        poolCode = detail.getPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_ADJUSTMENT_EFFECTIVE);
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case SPLIT:
                        // 预算分解
                        resultData = orderDetailService.checkSplitDetail(order, detail);
                        if (resultData.successful()) {
                            // 当前预算池
                            poolCode = detail.getPoolCode();
                            if (StringUtils.isBlank(poolCode)) {
                                // 预算池不存在,需要创建预算池
                                ResultData<Pool> result = poolService.createPool(order, detail);
                                if (result.failed()) {
                                    return ResultData.fail(result.getMessage());
                                }
                                Pool pool = result.getData();
                                poolCode = pool.getCode();
                                detail.setPoolCode(poolCode);
                                detail.setPoolAmount(pool.getBalance());
                            }
                            // 记录预算池执行日志
                            record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_EFFECTIVE);
                            record.setSubjectId(order.getSubjectId());
                            record.setAttributeCode(detail.getAttributeCode());
                            record.setBizCode(order.getCode());
                            record.setBizId(detail.getId());
                            remark = order.getRemark();
                            record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
                            poolService.recordLog(record);
                            // 源预算池
                            String originPoolCode = detail.getOriginPoolCode();
                            // 记录预算池执行日志
                            record = new ExecutionRecord(originPoolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_EFFECTIVE);
                            record.setSubjectId(order.getSubjectId());
                            record.setAttributeCode(detail.getAttributeCode());
                            record.setBizCode(order.getCode());
                            record.setBizId(detail.getId());
                            remark = order.getRemark();
                            record.setBizRemark("直接生效" + (StringUtils.isBlank(remark) ? "" : remark));
                            poolService.recordLog(record);
                            break;
                        } else {
                            return resultData;
                        }
                    default:
                        // 不支持的订单类型
                        return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
            }
            // 检查调整余额是否等于0
            if (0 != adjustBalance) {
                // 还有剩余调整余额[{0}]
                return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
            }
        }
        return ResultData.success();
    }

    /**
     * 提交流程审批预算处理
     * 规则:预算池进行预占用
     *
     * @param order   预算申请单
     * @param details 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> submitProcessUseBudget(Order order, List<OrderDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            String remark;
            String poolCode;
            ExecutionRecord record;
            ResultData<Void> resultData;
            // 调整时总额不变(调增调减之和等于0)
            double adjustBalance = 0;
            OperationType operation = OperationType.RELEASE;
            for (OrderDetail detail : details) {
                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        if (detail.getAmount() < 0) {
                            resultData = orderDetailService.checkInjectionDetail(order, detail);
                            if (resultData.failed()) {
                                return resultData;
                            }
                            poolCode = detail.getPoolCode();
                            if (StringUtils.isBlank(poolCode)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池金额不能值为负数[{0}]
                                return ResultData.fail(ContextUtil.getMessage("pool_00004", detail.getAmount()));
                            }
                        } else {
                            poolCode = detail.getPoolCode();
                        }

                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_INJECTION_SUBMIT);
                        if (detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("提交审核" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case ADJUSTMENT:
                        // 计算调整余额
                        adjustBalance = ArithUtils.add(adjustBalance, detail.getAmount());
                        // 为保证性能仅对调减的预算池做额度检查
                        if (detail.getAmount() < 0) {
                            resultData = orderDetailService.checkAdjustmentDetail(order, detail);
                            if (resultData.failed()) {
                                return resultData;
                            }
                            poolCode = detail.getPoolCode();
                            if (StringUtils.isBlank(poolCode)) {
                                LOG.error("预算池不存在. - " + JsonUtils.toJson(detail));
                                // 预算池金额不能值为负数[{0}]
                                return ResultData.fail(ContextUtil.getMessage("pool_00004", detail.getAmount()));
                            }
                        } else {
                            poolCode = detail.getPoolCode();
                        }

                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_ADJUSTMENT_SUBMIT);
                        if (detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("提交审核" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case SPLIT:
                        // 预算分解
                        resultData = orderDetailService.checkSplitDetail(order, detail);
                        if (resultData.successful()) {
                            // 当前预算池
                            poolCode = detail.getPoolCode();
                            // 记录预算池执行日志
                            record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_SUBMIT);
                            if (detail.getAmount() >= 0) {
                                // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                                record.setIsPoolAmount(Boolean.FALSE);
                            }
                            record.setSubjectId(order.getSubjectId());
                            record.setAttributeCode(detail.getAttributeCode());
                            record.setBizCode(order.getCode());
                            record.setBizId(detail.getId());
                            remark = order.getRemark();
                            record.setBizRemark("提交审核" + (StringUtils.isBlank(remark) ? "" : remark));
                            poolService.recordLog(record);

                            // 源预算池
                            String originPoolCode = detail.getOriginPoolCode();
                            // 记录预算池执行日志
                            record = new ExecutionRecord(originPoolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_SUBMIT);
                            if (-detail.getAmount() >= 0) {
                                // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                                record.setIsPoolAmount(Boolean.FALSE);
                            }
                            record.setSubjectId(order.getSubjectId());
                            record.setAttributeCode(detail.getAttributeCode());
                            record.setBizCode(order.getCode());
                            record.setBizId(detail.getId());
                            remark = order.getRemark();
                            record.setBizRemark("提交审核" + (StringUtils.isBlank(remark) ? "" : remark));
                            poolService.recordLog(record);
                            break;
                        } else {
                            return resultData;
                        }
                    default:
                        // 不支持的订单类型
                        return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
            }
            // 检查调整余额是否等于0
            if (0 != adjustBalance) {
                // 还有剩余调整余额[{0}]
                return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
            }
        }
        return ResultData.success();
    }

    /**
     * 取消流程预算处理
     * 规则:释放预占用
     *
     * @param order   预算申请单
     * @param details 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> cancelProcessUseBudget(Order order, List<OrderDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            String remark;
            String poolCode;
            ExecutionRecord record;
            ResultData<Void> resultData;
            OperationType operation = OperationType.RELEASE;
            for (OrderDetail detail : details) {
                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        poolCode = detail.getPoolCode();

                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, -detail.getAmount(), Constants.EVENT_INJECTION_CANCEL);
                        if (detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("退出流程" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case ADJUSTMENT:
                        poolCode = detail.getPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, -detail.getAmount(), Constants.EVENT_ADJUSTMENT_CANCEL);
                        if (detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("退出流程" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case SPLIT:
                        // 预算分解
                        // 当前预算池
                        poolCode = detail.getPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_CANCEL);
                        if (detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("退出流程" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);

                        // 源预算池
                        String originPoolCode = detail.getOriginPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(originPoolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_CANCEL);
                        if (-detail.getAmount() >= 0) {
                            // 订单状态为流程中,且金额大于等于0的金额,不影响预算池余额;而小于0的金额需要进行预占用处理
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("退出流程" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    default:
                        // 不支持的订单类型
                        return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
            }
        }
        return ResultData.success();
    }

    /**
     * 流程审批完成生效预算处理
     * 规则:释放预占用,更新正式占用或创建预算池
     *
     * @param order   预算申请单
     * @param details 预算申请单行项
     * @return 返回处理结果
     */
    private ResultData<Void> completeProcessUseBudget(Order order, List<OrderDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            String remark;
            String poolCode;
            ExecutionRecord record;
            // 调整时总额不变(调增调减之和等于0)
            double adjustBalance = 0;
            OperationType operation = OperationType.RELEASE;
            for (OrderDetail detail : details) {
                // 按订单类型,检查预算池额度(为保证性能仅对调减的预算池做额度检查)
                switch (order.getOrderCategory()) {
                    case INJECTION:
                        poolCode = detail.getPoolCode();
                        if (StringUtils.isBlank(poolCode)) {
                            // 预算池不存在,需要创建预算池
                            ResultData<Pool> result = poolService.createPool(order, detail);
                            if (result.failed()) {
                                return ResultData.fail(result.getMessage());
                            }
                            Pool pool = result.getData();
                            poolCode = pool.getCode();
                        }
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_INJECTION_COMPLETE);
                        if (detail.getAmount() <= 0) {
                            // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("审批完成" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case ADJUSTMENT:
                        // 计算调整余额
                        adjustBalance = ArithUtils.add(adjustBalance, detail.getAmount());

                        poolCode = detail.getPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_ADJUSTMENT_COMPLETE);
                        if (detail.getAmount() <= 0) {
                            // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("审批完成" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    case SPLIT:
                        // 预算分解
                        // 当前预算池
                        poolCode = detail.getPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), Constants.EVENT_SPLIT_COMPLETE);
                        if (detail.getAmount() <= 0) {
                            // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("审批完成" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);

                        // 源预算池
                        String originPoolCode = detail.getOriginPoolCode();
                        // 记录预算池执行日志
                        record = new ExecutionRecord(originPoolCode, operation, -detail.getAmount(), Constants.EVENT_SPLIT_COMPLETE);
                        if (-detail.getAmount() <= 0) {
                            // 订单状态由流程中变为已完成,金额小于等于0在提交流程时已提前占用,故此时不再重复占用,只记录日志
                            record.setIsPoolAmount(Boolean.FALSE);
                        }
                        record.setSubjectId(order.getSubjectId());
                        record.setAttributeCode(detail.getAttributeCode());
                        record.setBizCode(order.getCode());
                        record.setBizId(detail.getId());
                        remark = order.getRemark();
                        record.setBizRemark("审批完成" + (StringUtils.isBlank(remark) ? "" : remark));
                        poolService.recordLog(record);
                        break;
                    default:
                        // 不支持的订单类型
                        return ResultData.fail(ContextUtil.getMessage("order_detail_00007"));
                }
            }
            // 检查调整余额是否等于0
            if (0 != adjustBalance) {
                // 还有剩余调整余额[{0}]
                return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
            }
        }
        return ResultData.success();
    }
}