package com.changhong.bems.controller;

import com.changhong.bems.api.OrderApi;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.OrderDetailService;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.limiter.support.lock.SeiLockHelper;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.utils.AsyncRunUtil;
import io.swagger.annotations.Api;
import org.modelmapper.ModelMapper;
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
    /**
     * 预算申请单服务对象
     */
    @Autowired
    private OrderService service;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AsyncRunUtil asyncRunUtil;

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
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.INJECTION));
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
    public ResultData<String> checkDimension(String orderId, String subjectId, String categoryId) {
        return service.checkDimension(orderId, subjectId, categoryId);
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
            if (!SeiLockHelper.checkLocked("bems-v6:effective:" + orderId)) {
                asyncRunUtil.runAsync(() -> service.effective(order));
                return ResultData.success();
            } else {
                // 订单[{0}]正在生效处理过程中,请稍后.
                return ResultData.fail(ContextUtil.getMessage("order_00007", order.getCode()));
            }
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
    }

    /**
     * 提交审批预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> submitProcess(String orderId) {
        final Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        // 检查订单状态
        if (OrderStatus.PREFAB == order.getStatus() || OrderStatus.DRAFT == order.getStatus()) {
            if (!SeiLockHelper.checkLocked("bems-v6:submit:" + orderId)) {
                asyncRunUtil.runAsync(() -> service.submitProcess(order));
                return ResultData.success();
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
     * 预算申请单取消流程审批
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> cancelProcess(String orderId) {
        final Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        // 检查订单状态
        if (OrderStatus.PROCESSING == order.getStatus()) {
            if (!SeiLockHelper.checkLocked("bems-v6:cancel:" + orderId)) {
                asyncRunUtil.runAsync(() -> service.cancelProcess(order));
                return ResultData.success();
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
     * 预算申请单审批完成
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @Override
    public ResultData<Void> completeProcess(String orderId) {
        final Order order = service.findOne(orderId);
        if (Objects.isNull(order)) {
            // 订单[{0}]不存在!
            return ResultData.fail(ContextUtil.getMessage("order_00001"));
        }

        // 检查订单状态
        if (OrderStatus.PROCESSING == order.getStatus()) {
            if (!SeiLockHelper.checkLocked("bems-v6:complete:" + orderId)) {
                asyncRunUtil.runAsync(() -> service.completeProcess(order));
                return ResultData.success();
            } else {
                // 订单[{0}]正在提交流程处理过程中,请稍后.
                return ResultData.fail(ContextUtil.getMessage("order_00008", order.getCode()));
            }
        } else {
            // 订单状态为[{0}],不允许操作!
            return ResultData.fail(ContextUtil.getMessage("order_00004", order.getStatus()));
        }
    }
}