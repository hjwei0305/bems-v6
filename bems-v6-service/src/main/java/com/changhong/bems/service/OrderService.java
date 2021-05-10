package com.changhong.bems.service;

import com.changhong.bems.dao.OrderDao;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.serial.sdk.SerialService;
import com.changhong.sei.util.ArithUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

/**
 * 预算申请单(Order)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@Service
public class OrderService extends BaseEntityService<Order> {
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
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> clearOrderItems(String orderId) {
        orderDetailService.clearOrderItems(orderId);
        return ResultData.success();
    }

    /**
     * 通过单据行项id删除行项
     *
     * @param detailIds 单据Id
     * @return 业务实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> removeOrderItems(String[] detailIds) {
        Set<String> ids = new HashSet<>();
        Collections.addAll(ids, detailIds);
        orderDetailService.delete(ids);
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
        if (StringUtils.isBlank(order.getCode())) {
            order.setCode(serialService.getNumber(Order.class, ContextUtil.getTenantCode()));
        }
        OperateResultWithData<Order> result = this.save(order);
        if (result.successful()) {
            String orderId = order.getId();
            if (CollectionUtils.isNotEmpty(details)) {
                ResultData<Void> resultData = orderDetailService.updateAmount(order, details);
                if (resultData.failed()) {
                    // 回滚事务
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return ResultData.fail(resultData.getMessage());
                }
            }
            return ResultData.success(order);
        } else {
            return ResultData.fail(result.getMessage());
        }
    }

    /**
     * 生效预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> effectiveOrder(String orderId) {
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001", orderId));
        }
        if (OrderStatus.DRAFT != order.getStatus()) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        ResultData<Void> resultData = this.checkAndPutDetailPool(order, details, OperationType.RELEASE);
        if (resultData.successful()) {
            // 更新订单状态为:完成
            order.setStatus(OrderStatus.COMPLETED);
            dao.save(order);
        }
        return resultData;
    }

    /**
     * 提交审批预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> commitOrder(String orderId) {
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001", orderId));
        }

        if (OrderStatus.DRAFT != order.getStatus()) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        ResultData<Void> resultData = this.checkAndPutDetailPool(order, details, OperationType.PRE_RELEASE);
        if (resultData.successful()) {
            // 更新订单状态为:流程中
            order.setStatus(OrderStatus.PROCESSING);
            dao.save(order);
            resultData = ResultData.success();
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
    public ResultData<Void> completeOrder(String orderId) {
        Order order = dao.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001", orderId));
        }

        if (OrderStatus.PROCESSING != order.getStatus()) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        // TODO 流程中 到 完成
        ResultData<Void> resultData = this.checkAndPutDetailPool(order, details, OperationType.RELEASE);
        if (resultData.successful()) {
            // 更新订单状态为:完成
            order.setStatus(OrderStatus.COMPLETED);
            dao.save(order);
            resultData = ResultData.success();
        }
        return resultData;
    }

    /**
     * 检查并设置或创建预算池
     *
     * @param order     预算申请单
     * @param details   预算申请单行项
     * @param operation 操作类型
     * @return 返回处理结果
     */
    private ResultData<Void> checkAndPutDetailPool(Order order, List<OrderDetail> details, OperationType operation) {
        if (CollectionUtils.isNotEmpty(details)) {
            String poolCode;
            ExecutionRecord record;
            ResultData<Void> resultData;
            // 调整时总额不变(调增调减之和等于0)
            double adjustBalance = 0;
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
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), OrderCategory.INJECTION.name());
                        record.setBizOrderId(order.getId());
                        record.setBizOrderCode(order.getCode());
                        record.setBizItemId(detail.getOrderId());
                        record.setBizRemark(order.getRemark());
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
                        record = new ExecutionRecord(poolCode, operation, detail.getAmount(), OrderCategory.ADJUSTMENT.name());
                        record.setBizOrderId(order.getId());
                        record.setBizOrderCode(order.getCode());
                        record.setBizItemId(detail.getOrderId());
                        record.setBizRemark(order.getRemark());
                        poolService.recordLog(record);
                        break;
                    case SPLIT:
                        resultData = orderDetailService.checkSplitDetail(order, detail);
                        if (resultData.successful()) {
                            // 当前预算池
                            poolCode = detail.getPoolCode();
                            // 源预算池
                            String originPoolCode = detail.getOriginPoolCode();
                            // 记录预算池执行日志
                        }
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