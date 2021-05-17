package com.changhong.bems.controller;

import com.changhong.bems.api.OrderApi;
import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.CategoryService;
import com.changhong.bems.service.OrderDetailService;
import com.changhong.bems.service.OrderService;
import com.changhong.bems.service.mq.EffectiveOrderMessage;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.flow.FlowInvokeParams;
import com.changhong.sei.core.dto.flow.FlowStatus;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.limiter.support.lock.SeiLockHelper;
import com.changhong.sei.core.mq.MqProducer;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.util.EnumUtils;
import com.changhong.sei.utils.AsyncRunUtil;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 预算申请单(Order)控制类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@RestController
@Api(value = "OrderApi", tags = "预算申请单服务")
@RequestMapping(path = OrderApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController extends BaseEntityController<Order, OrderDto> implements OrderApi {
    private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
    /**
     * 预算申请单服务对象
     */
    @Autowired
    private OrderService service;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AsyncRunUtil asyncRunUtil;
    @Autowired
    private MqProducer producer;

    @Override
    public BaseEntityService<Order> getService() {
        return service;
    }

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    @Override
    public ResultData<List<OrganizationDto>> findOrgTree() {
        return service.findOrgTree();
    }

    /**
     * 分页查询下达注入订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<OrderDto>> findInjectionByPage(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 排除预制状态单据
        search.addFilter(new SearchFilter(Order.FIELD_STATUS, OrderStatus.PREFAB, SearchFilter.Operator.NE));
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.INJECTION));
        return convertToDtoPageResult(service.findByPage(search));
    }

    /**
     * 分页查询下达调整订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<OrderDto>> findAdjustmentByPage(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 排除预制状态单据
        search.addFilter(new SearchFilter(Order.FIELD_STATUS, OrderStatus.PREFAB, SearchFilter.Operator.NE));
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.ADJUSTMENT));
        return convertToDtoPageResult(service.findByPage(search));
    }

    /**
     * 分页查询分解调整订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @Override
    public ResultData<PageResult<OrderDto>> findSplitByPage(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 排除预制状态单据
        search.addFilter(new SearchFilter(Order.FIELD_STATUS, OrderStatus.PREFAB, SearchFilter.Operator.NE));
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.SPLIT));
        return convertToDtoPageResult(service.findByPage(search));
    }

    /**
     * 通过单据Id获取单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @Override
    public ResultData<PageResult<OrderDetailDto>> getOrderItems(String orderId, Search search) {
        PageResult<OrderDetail> pageResult = service.getOrderItems(orderId, search);
        PageResult<OrderDetailDto> result = new PageResult<>(pageResult);
        List<OrderDetail> list = pageResult.getRows();
        result.setRows(list.stream().map(d -> modelMapper.map(d, OrderDetailDto.class)).collect(Collectors.toList()));
        return ResultData.success(result);
    }

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @Override
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
    @Override
    public ResultData<Void> removeOrderItems(String[] detailIds) {
        Set<String> ids = new HashSet<>();
        Collections.addAll(ids, detailIds);
        orderDetailService.removeOrderItems(ids);
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
    @Override
    public ResultData<Void> checkDimension(String orderId, String subjectId, String categoryId) {
        return service.checkAndGetDimension(orderId, subjectId, categoryId);
    }

    /**
     * 添加预算申请单行项明细
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @Override
    public ResultData<String> addOrderDetails(AddOrderDetail order) {
        return service.addOrderDetails(order);
    }

    /**
     * 更新预算申请单行项金额
     *
     * @param detailId 申请单行项id
     * @param amount   金额
     * @return 返回订单头id
     */
    @Override
    public ResultData<OrderDetailDto> updateDetailAmount(String detailId, double amount) {
        OrderDetail detail = orderDetailService.findOne(detailId);
        if (Objects.isNull(detail)) {
            // 行项不存在
            return ResultData.fail(ContextUtil.getMessage("order_detail_00009"));
        }
        Order order = service.findOne(detail.getOrderId());
        if (Objects.isNull(order)) {
            // 订单不存在
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        if (OrderStatus.PREFAB == order.getStatus() || OrderStatus.DRAFT == order.getStatus()) {
            ResultData<OrderDetail> resultData = orderDetailService.updateDetailAmount(order, detail, amount);
            if (resultData.successful()) {
                return ResultData.success(dtoModelMapper.map(resultData.getData(), OrderDetailDto.class));
            } else {
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 订单状态为[{0}],不允许操作
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
    }

    /**
     * 获取一个预算申请单
     *
     * @param orderId 申请单id
     * @return 返回订单头
     */
    @Override
    public ResultData<OrderDto> getOrderHead(String orderId) {
        Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单不存在
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        OrderDto dto = dtoModelMapper.map(order, OrderDto.class);
        List<DimensionDto> dimensions = categoryService.getAssigned(order.getCategoryId());
        dto.setDimensions(dimensions);
        return ResultData.success(dto);
    }

    /**
     * 保存预算申请单
     *
     * @param request 业务实体DTO
     * @return 返回订单头id
     */
    @Override
    public ResultData<OrderDto> saveOrder(OrderDto request) {
        Order order = convertToEntity(request);
        switch (order.getStatus()) {
            case PREFAB:
            case DRAFT:
                // 更新状态为草稿状态
                order.setStatus(OrderStatus.DRAFT);
                break;
            case PROCESSING:
            case COMPLETED:
                // 订单状态为[{0}],不允许操作
                return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
            default:
                // 不支持的订单状态
                return ResultData.fail(ContextUtil.getMessage("order_00005", order.getStatus()));
        }
        ResultData<Order> resultData = service.saveOrder(order, null);
        if (resultData.successful()) {
            return ResultData.success(dtoModelMapper.map(resultData.getData(), OrderDto.class));
        } else {
            return ResultData.fail(resultData.getMessage());
        }
    }

    /**
     * 生效预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> effectiveOrder(String orderId) {
        final Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        // 检查订单状态
        if (OrderStatus.PREFAB == order.getStatus() || OrderStatus.DRAFT == order.getStatus()) {
            List<OrderDetail> details = orderDetailService.getOrderItems(order.getId());
            // 更新状态为生效中
            service.updateStatus(orderId, OrderStatus.EFFECTING);
            // 更新订单总金额
            service.updateAmount(orderId);
            // 检查是否存在错误行项
            ResultData<Void> resultData = service.checkDetailHasErr(details);
            if (resultData.successful()) {
                asyncRunUtil.runAsync(() -> {
                    // 以线性队列方式,避免预算池并发问题
                    try {
                        // 休眠1s,防止状态事务还未更新
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    // 发送队列消息
                    EffectiveOrderMessage message = new EffectiveOrderMessage();
                    message.setOrderId(orderId);
                    message.setOperation(Constants.ORDER_OPERATION_EFFECTIVE);
                    SessionUser sessionUser = ContextUtil.getSessionUser();
                    message.setUserId(sessionUser.getUserId());
                    message.setAccount(sessionUser.getAccount());
                    message.setUserName(sessionUser.getUserName());
                    message.setTenantCode(sessionUser.getTenantCode());
                    producer.send(JsonUtils.toJson(message));
                    if (LOG.isInfoEnabled()) {
                        LOG.info("预算申请单[{}]-直接生效消息发送队列成功.", message);
                    }
                    //service.effective(orderId);
                });
            }
            return resultData;
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
    }

    /**
     * 检查是否存在注入类型预制单
     *
     * @return 返回检查结果
     */
    @Override
    public ResultData<OrderDto> checkInjectPrefab() {
        OrderDto dto = null;
        Order order = service.getPrefabExist(OrderCategory.INJECTION);
        if (Objects.nonNull(order)) {
            dto = convertToDto(order);
        }
        return ResultData.success(dto);
    }

    /**
     * 检查是否存在调整类型预制单
     *
     * @return 返回检查结果
     */
    @Override
    public ResultData<OrderDto> checkAdjustPrefab() {
        OrderDto dto = null;
        Order order = service.getPrefabExist(OrderCategory.ADJUSTMENT);
        if (Objects.nonNull(order)) {
            dto = convertToDto(order);
        }
        return ResultData.success(dto);
    }

    /**
     * 检查是否存在分解类型预制单
     *
     * @return 返回检查结果
     */
    @Override
    public ResultData<OrderDto> checkSplitPrefab() {
        OrderDto dto = null;
        Order order = service.getPrefabExist(OrderCategory.SPLIT);
        if (Objects.nonNull(order)) {
            dto = convertToDto(order);
        }
        return ResultData.success(dto);
    }

    /**
     * 获取申请单调整数据
     *
     * @param orderId 申请单号
     * @return 返回调整数据
     */
    @Override
    public ResultData<Map<String, Double>> getAdjustData(String orderId) {
        Map<String, Double> data = new HashMap<>();
        data.put("ADD", 0d);
        data.put("SUB", 0d);
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        if (CollectionUtils.isNotEmpty(details)) {
            double countAdd = details.stream().filter(d -> d.getAmount() > 0).count();
            data.put("ADD", countAdd);
            double countSub = details.stream().filter(d -> d.getAmount() < 0).count();
            data.put("SUB", countSub);
        }
        return ResultData.success(data);
    }

    ///////////////////////流程集成 start//////////////////////////////

    /**
     * 获取条件POJO属性说明
     *
     * @param businessModelCode 订单类型 {@link OrderCategory}
     * @param all               是否查询全部
     * @return POJO属性说明Map
     */
    @Override
    public ResultData<Map<String, String>> properties(String businessModelCode, Boolean all) {
        Map<String, String> map = new HashMap<>();
        return ResultData.success(map);
    }

    /**
     * 获取条件POJO属性键值对
     *
     * @param businessModelCode 订单类型 {@link OrderCategory}
     * @param id                单据id
     * @return POJO属性说明Map
     */
    @Override
    public ResultData<Map<String, Object>> propertiesAndValues(String businessModelCode, String id, Boolean all) {
        Order order = service.findOne(id);
        if (Objects.isNull(order)) {
            return ResultData.fail("订单不存在.");
        }

        Map<String, Object> map = JsonUtils.object2Map(order);
        map.put("orgId", order.getApplyOrgId());
        map.put("tenantCode", order.getTenantCode());
        map.put("workCaption", order.getRemark());
        map.put("businessCode", order.getCode());
        map.put("id", order.getId());
        return ResultData.success(map);
    }

    /**
     * 获取条件POJO属性初始化值键值对
     *
     * @param businessModelCode 订单类型 {@link OrderCategory}
     * @return POJO属性说明Map
     */
    @Override
    public ResultData<Map<String, Object>> initPropertiesAndValues(String businessModelCode) {
        Map<String, Object> map = new HashMap<>();
        return ResultData.success(map);
    }

    /**
     * 重置单据状态
     *
     * @param businessModelCode 订单类型 {@link OrderCategory}
     * @param id                单据id
     * @param status            状态
     * @return 返回结果
     */
    @Override
    public ResultData<Boolean> resetState(String businessModelCode, String id, String status) {
//        LogUtil.bizLog("流程状态变化接口. 订单类型: {}, 单据id: {}, 状态: {}", businessModelCode, id, status);
        Order order = service.findOne(id);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        String orderId = order.getId();
        FlowStatus flowStatus = EnumUtils.getEnum(FlowStatus.class, status);
        switch (flowStatus) {
            case INIT:
                // 流程终止或退出
                // 检查订单状态
                if (OrderStatus.PROCESSING == order.getStatus()) {
                    if (!SeiLockHelper.checkLocked("bems-v6:cancel:" + orderId)) {
                        asyncRunUtil.runAsync(() -> service.cancelProcess(order));
                    } else {
                        // 订单[{0}]正在提交流程处理过程中,请稍后.
                        return ResultData.fail(ContextUtil.getMessage("order_00008", order.getCode()));
                    }
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
                }
                break;
            case INPROCESS:
                // 流程启动或流程中
                if (OrderStatus.PREFAB == order.getStatus() || OrderStatus.DRAFT == order.getStatus()) {
                    // 状态更新为流程中
                    service.updateStatus(orderId, OrderStatus.PROCESSING);
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
                }
                break;
            case COMPLETED:
                // 流程正常完成
                // 检查订单状态
                if (OrderStatus.PROCESSING == order.getStatus()) {
//                    asyncRunUtil.runAsync(() -> {
                    // 以线性队列方式,避免预算池并发问题
                    // 发送队列消息
                    EffectiveOrderMessage message = new EffectiveOrderMessage();
                    message.setOrderId(orderId);
                    message.setOperation(Constants.ORDER_OPERATION_COMPLETE);
                    SessionUser sessionUser = ContextUtil.getSessionUser();
                    message.setUserId(sessionUser.getUserId());
                    message.setAccount(sessionUser.getAccount());
                    message.setUserName(sessionUser.getUserName());
                    message.setTenantCode(sessionUser.getTenantCode());
                    producer.send(JsonUtils.toJson(message));
                    if (LOG.isInfoEnabled()) {
                        LOG.info("预算申请单[{}]-流程审批完成消息发送队列成功.", message);
                    }
                    // service.completeProcess(order);
//                    });
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
                }
                break;
            default:

        }
        return ResultData.success(Boolean.TRUE);
    }

    /**
     * 预算申请单提交流程占用预算事件
     *
     * @param flowInvokeParams 服务、事件输入参数VO
     * @return 操作结果
     */
    @Override
    public ResultData<Boolean> submitProcessEvent(FlowInvokeParams flowInvokeParams) {
        // 业务id
        String orderId = flowInvokeParams.getId();
        // 流程接收任务回调id
        final String taskActDefId = flowInvokeParams.getTaskActDefId();
//        LogUtil.bizLog("流程状态变化接口. 单据id: {}, 回调id: {}", orderId, taskActDefId);
        final Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        if (CollectionUtils.isEmpty(details)) {
            // 订单行项不存在!
            return ResultData.fail(ContextUtil.getMessage("order_detail_00009"));
        }

        // 检查订单状态
        if (OrderStatus.PROCESSING == order.getStatus()) {
            if (!SeiLockHelper.checkLocked("bems-v6:submit:" + orderId)) {
                // 检查是否存在错误行项
                ResultData<Void> resultData = service.checkDetailHasErr(details);
                if (resultData.successful()) {
                    asyncRunUtil.runAsync(() -> service.submitProcess(order, details, taskActDefId));
                    return ResultData.success(Boolean.TRUE);
                } else {
                    return ResultData.fail(resultData.getMessage());
                }
            } else {
                // 订单[{0}]正在提交流程处理过程中,请稍后.
                return ResultData.fail(ContextUtil.getMessage("order_00008", order.getCode()));
            }
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
    }

    /**
     * 移动端页面属性
     *
     * @param businessModelCode 订单类型 {@link OrderCategory}
     * @param id                单据id
     */
    @Override
    public ResultData<Map<String, Object>> formPropertiesAndValues(String businessModelCode, String id) {
        Order order = service.findOne(id);
        if (Objects.nonNull(order)) {
            Map<String, Object> map = JsonUtils.object2Map(order);
            map.put("orgId", order.getApplyOrgId());
            map.put("tenantCode", order.getTenantCode());
            map.put("workCaption", order.getRemark());
            map.put("businessCode", order.getCode());
            map.put("id", order.getId());

            Map<String, Object> result = new HashMap<>();
            //移动端类型标识 每一中业务的 唯一标识。移动端具体确认是何种业务
            result.put("mobileBusinessType", businessModelCode);
            result.put("data", map);
            return ResultData.success(result);
        } else {
            return ResultData.fail("订单不存在.");
        }
    }
    ///////////////////////流程集成 end//////////////////////////////
}