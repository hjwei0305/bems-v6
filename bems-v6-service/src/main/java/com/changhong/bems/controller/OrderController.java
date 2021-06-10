package com.changhong.bems.controller;

import com.alibaba.excel.EasyExcel;
import com.changhong.bems.api.OrderApi;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.vo.TemplateHeadVo;
import com.changhong.bems.service.CategoryService;
import com.changhong.bems.service.DimensionComponentService;
import com.changhong.bems.service.OrderDetailService;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.flow.FlowInvokeParams;
import com.changhong.sei.core.dto.flow.FlowStatus;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.mq.MqProducer;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.util.ArithUtils;
import com.changhong.sei.util.EnumUtils;
import com.changhong.sei.utils.AsyncRunUtil;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private DimensionComponentService dimensionComponentService;
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
        OrderStatus status = order.getStatus();
        if (OrderStatus.PREFAB == status || OrderStatus.DRAFT == status) {
            // 更新状态为草稿状态
            order.setStatus(OrderStatus.DRAFT);
            ResultData<Order> resultData = service.saveOrder(order, null);
            if (resultData.successful()) {
                return ResultData.success(dtoModelMapper.map(resultData.getData(), OrderDto.class));
            } else {
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 订单状态为[{0}],不允许操作
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
        Map<String, Double> data = new HashMap<>(7);
        data.put("ADD", 0d);
        data.put("SUB", 0d);
        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
        if (CollectionUtils.isNotEmpty(details)) {
            double sumAdd = details.stream().filter(d -> d.getAmount() > 0).mapToDouble(OrderDetail::getAmount).sum();
            data.put("ADD", ArithUtils.round(sumAdd, 2));
            double sumSub = details.stream().filter(d -> d.getAmount() < 0).mapToDouble(OrderDetail::getAmount).sum();
            data.put("SUB", ArithUtils.round(sumSub, 2));
        }
        return ResultData.success(data);
    }

    /**
     * 分页查询预算分解上级期间预算
     *
     * @param param 查询参数
     * @return 上级期间预算
     */
    @Override
    public ResultData<PageResult<OrderDetailDto>> querySplitGroup(SplitDetailQuickQueryParam param) {
        PageResult<OrderDetail> result = orderDetailService.querySplitGroup(param);
        PageResult<OrderDetailDto> pageResult = new PageResult<>(result);
        List<OrderDetail> details = result.getRows();
        if (CollectionUtils.isNotEmpty(details)) {
            Set<String> code = details.stream().map(OrderDetail::getOriginPoolCode).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            Search search = Search.createSearch();
            search.addFilter(new SearchFilter(OrderDetail.FIELD_ORDER_ID, param.getOrderId()));
            search.addFilter(new SearchFilter(OrderDetail.FIELD_ORIGIN_POOL_CODE, code, SearchFilter.Operator.IN));
            List<OrderDetail> allChildren = orderDetailService.findByFilters(search);
            Map<String, List<OrderDetailDto>> group;
            if (CollectionUtils.isNotEmpty(allChildren)) {
                group = allChildren.stream().map(d -> modelMapper.map(d, OrderDetailDto.class))
                        .collect(Collectors.groupingBy(OrderDetailDto::getOriginPoolCode));
            } else {
                group = new HashMap<>();
            }
            OrderDetailDto dto;
            List<OrderDetailDto> dtoList = new ArrayList<>(details.size());
            for (OrderDetail detail : details) {
                dto = modelMapper.map(detail, OrderDetailDto.class);
                if (StringUtils.isNotBlank(detail.getOriginPoolCode())) {
                    dto.setChildren(group.get(detail.getOriginPoolCode()));
                }
                dtoList.add(dto);
            }
            pageResult.setRows(dtoList);
        }
        return ResultData.success(pageResult);
    }

    /**
     * excel文件数据导入
     *
     * @return 检查结果
     */
    @Override
    public ResultData<String> importBudge(AddOrderDetail order, MultipartFile file) {
        LogUtil.bizLog("上传订单数据 {}", JsonUtils.toJson(order));
        LogUtil.bizLog("上传文件名 {}", file.getOriginalFilename());
        try {
            List<Map<Integer, String>> list = EasyExcel.read(file.getInputStream())
                    // 指定sheet,默认从0开始
                    .sheet(0)
                    // 数据读取起始行.从头开始读,并将第一行数据进行校验
                    .headRowNumber(0)
                    .doReadSync();
            return service.importOrderDetails(order, list);
        } catch (Exception e) {
            return ResultData.fail(ContextUtil.getMessage("order_detail_00013", ExceptionUtils.getRootCause(e)));
        }
    }

    /**
     * 获取预算模版格式数据
     *
     * @param categoryId 预算类型id
     * @return 预算模版格式数据
     */
    @Override
    public ResultData<List<String>> getBudgetTemplate(String categoryId) {
        return ResultData.success(service.getBudgetTemplate(categoryId).stream().map(TemplateHeadVo::getValue).collect(Collectors.toList()));
    }

    /**
     * 获取预算维度主数据
     *
     * @param subjectId 预算主体id
     * @param dimCode   预算维度代码
     * @return 导出预算模版数据
     */
    @Override
    public ResultData<Map<String, Object>> getDimensionValues(String subjectId, String dimCode) {
        return dimensionComponentService.getDimensionValues(subjectId, dimCode);
    }

    /**
     * 确认预算申请单
     * 预算余额检查并预占用
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> confirmOrder(String orderId) {
        return service.confirm(orderId);
    }

    /**
     * 撤销已确认的预算申请单
     * 释放预占用
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> cancelConfirmOrder(String orderId) {
        return service.cancelConfirm(orderId);
    }

    /**
     * 已确认的预算申请单直接生效
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> effectiveOrder(String orderId) {
        return service.effective(orderId);
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
        Map<String, String> map = new HashMap<>(7);
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
        Map<String, Object> map = new HashMap<>(7);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("流程状态变化接口. 订单类型: {}, 单据id: {}, 状态: {}", businessModelCode, id, status);
        }
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
                if (OrderStatus.APPROVAL == order.getStatus()) {
                    service.cancelConfirm(orderId);
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
                }
                break;
            case INPROCESS:
                // 流程启动或流程中
                if (OrderStatus.PREFAB == order.getStatus() || OrderStatus.DRAFT == order.getStatus()) {
                    // 状态更新为流程中
                    service.updateStatus(orderId, OrderStatus.APPROVAL);
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
                }
                break;
            case COMPLETED:
                // 流程正常完成
                // 检查订单状态
                if (OrderStatus.APPROVAL == order.getStatus()) {
                    service.effective(orderId);
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
     * 预算申请单提交审批,流程启动检查事件
     *
     * @param flowInvokeParams 服务、事件输入参数VO
     * @return 操作结果
     */
    @Override
    public ResultData<Boolean> flowBeforeEvent(FlowInvokeParams flowInvokeParams) {
        // 业务id
        String orderId = flowInvokeParams.getId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("流程状态变化接口. 单据id: {}", orderId);
        }
        final Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }
        // 检查订单状态
        if (OrderStatus.CONFIRMED == order.getStatus()) {
            return ResultData.success(Boolean.TRUE);
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

            Map<String, Object> result = new HashMap<>(7);
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