package com.changhong.bems.controller;

import com.changhong.bems.api.OrderApi;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
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
    private ModelMapper modelMapper;

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
        return service.clearOrderItems(orderId);
    }

    /**
     * 通过单据行项id删除行项
     *
     * @param detailIds 单据Id
     * @return 业务实体
     */
    @Override
    public ResultData<Void> removeOrderItems(String[] detailIds) {
        return service.removeOrderItems(detailIds);
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
     * 保存预算申请单
     *
     * @param request 业务实体DTO
     * @return 返回订单头id
     */
    @Override
    public ResultData<String> saveOrder(OrderDto request) {
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
        List<OrderDetailDto> detailDtoList = request.getOrderDetails();
        List<OrderDetail> details = null;
        if (CollectionUtils.isNotEmpty(detailDtoList)) {
            details = detailDtoList.stream().map(dto -> entityModelMapper.map(dto, OrderDetail.class)).collect(Collectors.toList());
        }
        return service.saveOrder(order, details);
    }
}