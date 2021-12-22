package com.changhong.bems.controller;

import com.alibaba.excel.EasyExcel;
import com.changhong.bems.api.OrderApi;
import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.entity.vo.TemplateHeadVo;
import com.changhong.bems.service.CategoryService;
import com.changhong.bems.service.OrderCommonService;
import com.changhong.bems.service.OrderDetailService;
import com.changhong.bems.service.OrderService;
import com.changhong.bems.service.cust.BudgetDimensionCustManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.flow.FlowInvokeParams;
import com.changhong.sei.core.dto.flow.FlowStatus;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private OrderCommonService orderCommonService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BudgetDimensionCustManager budgetDimensionCustManager;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
        // 只允许查看本人单据
        search.addFilter(new SearchFilter(Order.FIELD_CREATOR_ID, ContextUtil.getUserId()));
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
        // 只允许查看本人单据
        search.addFilter(new SearchFilter(Order.FIELD_CREATOR_ID, ContextUtil.getUserId()));
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
        // 只允许查看本人单据
        search.addFilter(new SearchFilter(Order.FIELD_CREATOR_ID, ContextUtil.getUserId()));
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
        OrderStatus status = order.getStatus();
        // 允许流程中修改金额
        if (OrderStatus.PREFAB == status || OrderStatus.DRAFT == status || OrderStatus.APPROVING == status) {
            ResultData<OrderDetail> resultData = orderDetailService.updateDetailAmount(order, detail, new BigDecimal(Double.toString(amount)));
            if (resultData.successful()) {
                return ResultData.success(dtoModelMapper.map(resultData.getData(), OrderDetailDto.class));
            } else {
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 订单状态为[{0}],不允许操作
            return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
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
        ResultData<Void> resultData = service.checkDetailHasErr(orderId);
        if (resultData.failed()) {
            dto.setHasErr(Boolean.TRUE);
        }
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
            ResultData<Order> resultData = service.saveOrder(order);
            if (resultData.successful()) {
                return ResultData.success(dtoModelMapper.map(resultData.getData(), OrderDto.class));
            } else {
                return ResultData.fail(resultData.getMessage());
            }
        } else {
            // 订单状态为[{0}],不允许操作
            return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
        }
    }

    /**
     * 检查是否存在注入类型预制单
     *
     * @return 返回检查结果
     */
    @Override
    public ResultData<List<OrderDto>> checkInjectPrefab() {
        List<OrderDto> dtoList = null;
        List<Order> orders = service.getPrefabExist(OrderCategory.INJECTION);
        if (CollectionUtils.isNotEmpty(orders)) {
            dtoList = orders.stream().map(this::convertToDto).collect(Collectors.toList());
        }
        return ResultData.success(dtoList);
    }

    /**
     * 检查是否存在调整类型预制单
     *
     * @return 返回检查结果
     */
    @Override
    public ResultData<List<OrderDto>> checkAdjustPrefab() {
        List<OrderDto> dtoList = null;
        List<Order> orders = service.getPrefabExist(OrderCategory.ADJUSTMENT);
        if (CollectionUtils.isNotEmpty(orders)) {
            dtoList = orders.stream().map(this::convertToDto).collect(Collectors.toList());
        }
        return ResultData.success(dtoList);
    }

    /**
     * 检查是否存在分解类型预制单
     *
     * @return 返回检查结果
     */
    @Override
    public ResultData<List<OrderDto>> checkSplitPrefab() {
        List<OrderDto> dtoList = null;
        List<Order> orders = service.getPrefabExist(OrderCategory.SPLIT);
        if (CollectionUtils.isNotEmpty(orders)) {
            dtoList = orders.stream().map(this::convertToDto).collect(Collectors.toList());
        }
        return ResultData.success(dtoList);
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
            BigDecimal sumAdd = BigDecimal.ZERO;
            BigDecimal sumSub = BigDecimal.ZERO;
            for (OrderDetail detail : details) {
                if (detail.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    sumAdd = sumAdd.add(detail.getAmount());
                } else {
                    sumSub = sumSub.add(detail.getAmount());
                }
            }
            data.put("ADD", ArithUtils.round(sumAdd.doubleValue(), 2));
            data.put("SUB", ArithUtils.round(sumSub.doubleValue(), 2));
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
                        .collect(Collectors.groupingBy(OrderDetailDto::getId));
            } else {
                group = new HashMap<>();
            }
            OrderDetailDto dto;
            List<OrderDetailDto> dtoList = new ArrayList<>(details.size());
            for (OrderDetail detail : details) {
                dto = modelMapper.map(detail, OrderDetailDto.class);
                dto.setChildren(group.get(detail.getId()));
                if (StringUtils.equals(Constants.NONE, detail.getOriginPoolCode())) {
                    dto.setOriginPoolCode(null);
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
    public ResultData<String> importBudge(AddOrderDetail orderDto, MultipartFile file) {
        StopWatch stopWatch = new StopWatch("导入");
        String categoryId = orderDto.getCategoryId();
        if (StringUtils.isBlank(categoryId)) {
            //添加单据行项时,预算类型不能为空.
            return ResultData.fail(ContextUtil.getMessage("order_detail_00003"));
        }
        stopWatch.start("检查维度");
        // 通过单据Id检查预算主体和类型是否被修改
        ResultData<String> resultData = service.checkDimension(orderDto.getId(), orderDto.getSubjectId(), categoryId);
        if (resultData.failed()) {
            return ResultData.fail(resultData.getMessage());
        }
        stopWatch.stop();
        try {
            stopWatch.start("读取excel");
            List<Map<Integer, String>> list = EasyExcel.read(file.getInputStream())
                    // 指定sheet,默认从0开始
                    .sheet(0)
                    // 数据读取起始行.从头开始读,并将第一行数据进行校验
                    .headRowNumber(0)
                    .doReadSync();
            stopWatch.stop();
            if (CollectionUtils.isEmpty(list)) {
                //导入的订单行项数据不能为空
                return ResultData.fail(ContextUtil.getMessage("order_detail_00012"));
            }
            stopWatch.start("获取维度");
            List<DimensionDto> dimensions = categoryService.getAssigned(orderDto.getCategoryId());
            if (CollectionUtils.isEmpty(dimensions)) {
                // 预算类型[{0}]下未找到预算维度
                return ResultData.fail(ContextUtil.getMessage("category_00007"));
            }
            stopWatch.stop();
            stopWatch.start("检查模版");
            // 预算类型获取模版
            List<TemplateHeadVo> templateHead = new ArrayList<>();
            // 模版检查
            {
                // 取出抬头并从列表中移除
                Map<Integer, String> head = list.remove(0);
                if (Objects.nonNull(head)) {
                    String dimName;
                    TemplateHeadVo headVo;
                    // 解析导入模版抬头
                    for (DimensionDto dim : dimensions) {
                        headVo = null;
                        dimName = ContextUtil.getMessage("default_dimension_".concat(dim.getCode()));
                        for (Map.Entry<Integer, String> entry : head.entrySet()) {
                            if (org.apache.commons.lang.StringUtils.equals(dimName, entry.getValue())) {
                                headVo = new TemplateHeadVo(entry.getKey(), dim.getCode(), dimName);
                                templateHead.add(headVo);
                                break;
                            }
                        }
                        if (Objects.isNull(headVo)) {
                            // 预算数据导入模版不正确
                            return ResultData.fail(ContextUtil.getMessage("order_detail_00014"));
                        }
                    }

                    dimName = ContextUtil.getMessage("budget_template_amount");
                    for (Map.Entry<Integer, String> entry : head.entrySet()) {
                        if (org.apache.commons.lang.StringUtils.equals(dimName, entry.getValue())) {
                            headVo = new TemplateHeadVo(entry.getKey(), OrderDetail.FIELD_AMOUNT, dimName);
                            templateHead.add(headVo);
                            break;
                        }
                    }
                } else {
                    // 预算数据导入模版不正确
                    return ResultData.fail(ContextUtil.getMessage("order_detail_00014"));
                }
            }
            stopWatch.stop();
            stopWatch.start("保存订单头");
            Order order = modelMapper.map(orderDto, Order.class);
            // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
            order.setProcessing(Boolean.TRUE);
            // 保存订单头
            ResultData<Order> orderResult = service.saveOrder(order);
            stopWatch.stop();
            if (orderResult.successful()) {
                orderCommonService.importOrderDetails(order, templateHead, list);
                LOG.info("预算导入总耗时:\n{}", stopWatch.prettyPrint());
                return ResultData.success(order.getId());
            } else {
                return ResultData.fail(orderResult.getMessage());
            }
        } catch (Exception e) {
            return ResultData.fail(ContextUtil.getMessage("order_detail_00013", ExceptionUtils.getRootCause(e)));
        }
    }

    /**
     * 导出预算订单明细数据
     *
     * @param orderId 订单id
     * @return 导出的明细数据
     */
    @Override
    public ResultData<Map<String, Object>> exportBudgeDetails(String orderId) {
        return service.exportBudgeDetails(orderId);
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
        return budgetDimensionCustManager.getDimensionValues(subjectId, dimCode);
    }

    /**
     * 确认预算申请单
     * 预算余额检查并预占用
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<OrderDto> confirmOrder(String orderId) {
        ResultData<Order> resultData = service.confirm(orderId);
        if (resultData.successful()) {
            return ResultData.success(modelMapper.map(resultData.getData(), OrderDto.class));
        } else {
            return ResultData.fail(resultData.getMessage());
        }
    }

    /**
     * 撤销已确认的预算申请单
     * 释放预占用
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<OrderDto> cancelConfirmOrder(String orderId) {
        ResultData<Order> resultData = service.cancelConfirm(orderId);
        if (resultData.successful()) {
            return ResultData.success(modelMapper.map(resultData.getData(), OrderDto.class));
        } else {
            return ResultData.fail(resultData.getMessage());
        }
    }

    /**
     * 已确认的预算申请单直接生效
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<OrderDto> effectiveOrder(String orderId) {
        ResultData<Order> resultData = service.effective(orderId);
        if (resultData.successful()) {
            return ResultData.success(modelMapper.map(resultData.getData(), OrderDto.class));
        } else {
            // 预算生效失败
            return ResultData.fail(ContextUtil.getMessage("order_00008", resultData.getMessage()));
        }
    }

    /**
     * 获取订单处理状态
     *
     * @param orderId 订单id
     * @return 处理状态
     */
    @Override
    public ResultData<OrderStatistics> getProcessingStatus(String orderId) {
        OrderStatistics statistics = (OrderStatistics) redisTemplate.opsForValue().get(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
        if (Objects.nonNull(statistics)) {
            if (statistics.getFinish()) {
                // // 获取处理中的订单行项数.等于0表示处理完订单所有行项
                // long processingCount = orderDetailService.getProcessingCount(orderId);
                // if (processingCount == 0) {
                // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
                service.setProcessStatus(orderId, Boolean.FALSE);
                // }
            }
        } else {
            statistics = new OrderStatistics();
            // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
            service.setProcessStatus(orderId, Boolean.FALSE);
        }
        return ResultData.success(statistics);
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
        if (LOG.isInfoEnabled()) {
            LOG.info("流程状态变化接口. 订单类型: {}, 单据id: {}, 状态: {}", businessModelCode, id, status);
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
                if (OrderStatus.APPROVING == order.getStatus()) {
                    service.cancelConfirm(orderId);
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
                }
                break;
            case INPROCESS:
                // 流程启动或流程中
                if (OrderStatus.CONFIRMED == order.getStatus()) {
                    // 检查是否存在错误行项
                    ResultData<Void> resultData = service.checkDetailHasErr(orderId);
                    if (resultData.failed()) {
                        return ResultData.fail(resultData.getMessage());
                    }
                    // 调整时总额不变(调增调减之和等于0)
                    if (OrderCategory.ADJUSTMENT.equals(order.getOrderCategory())) {
                        List<OrderDetail> details = orderDetailService.getOrderItems(orderId);
                        if (CollectionUtils.isEmpty(details)) {
                            // 订单[{0}]生效失败: 无订单行项
                            return ResultData.fail(ContextUtil.getMessage("order_00007", order.getCode()));
                        }
                        // 计算调整余额
                        double adjustBalance = details.parallelStream().mapToDouble(detail -> detail.getAmount().doubleValue()).sum();
                        // 检查调整余额是否等于0
                        if (0 != adjustBalance) {
                            // 还有剩余调整余额[{0}]
                            return ResultData.fail(ContextUtil.getMessage("order_00006", adjustBalance));
                        }
                    }
                    // 状态更新为流程中
                    service.updateStatus(orderId, OrderStatus.APPROVING);
                } else {
                    // 订单状态为[{0}],不允许操作!
                    return ResultData.fail(ContextUtil.getMessage("order_00004", ContextUtil.getMessage(EnumUtils.getEnumItemRemark(OrderStatus.class, order.getStatus()))));
                }
                break;
            case COMPLETED:
                // 流程正常完成

                break;
            default:

        }
        return ResultData.success(Boolean.TRUE);
    }

    /**
     * 流程结束事件,生效预算申请单
     *
     * @param flowInvokeParams 服务、事件输入参数VO
     * @return 操作结果
     */
    @Override
    public ResultData<Boolean> flowEndEvent(FlowInvokeParams flowInvokeParams) {
        // 业务id
        String orderId = flowInvokeParams.getId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("流程状态变化接口. 单据id: {}", orderId);
        }

        //终止时
        Map<String, String> otherParam = flowInvokeParams.getParams();
        if (otherParam != null) {
            String endSign = otherParam.get("endSign");
            // 等于0：表示根据流程图走到了流程的结束节点
            if ("0".equals(endSign)) {
                ResultData<Order> resultData = service.effective(orderId);
                // 检查订单状态
                if (resultData.failed()) {
                    return ResultData.fail(resultData.getMessage());
                }
            } else {
                // 状态更新为提交流程前的已确认状态
                service.updateStatus(orderId, OrderStatus.CONFIRMED);
            }
            return ResultData.success(Boolean.TRUE);
        } else {
            return ResultData.fail("流程结束标识符不存在");
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