package com.changhong.bems.api;

import com.changhong.bems.dto.AddOrderDetail;
import com.changhong.bems.dto.OrderDetailDto;
import com.changhong.bems.dto.OrderDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算申请单(Order)API
 *
 * @author sei
 * @since 2021-04-25 15:13:58
 */
@Valid
@FeignClient(name = "bems-v6", path = OrderApi.PATH)
public interface OrderApi extends BaseEntityApi<OrderDto> {
    String PATH = "order";

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    @GetMapping(path = "findOrgTree")
    @ApiOperation(value = "获取组织机构树(不包含冻结)", notes = "获取组织机构树(不包含冻结)")
    ResultData<List<OrganizationDto>> findOrgTree();

    /**
     * 分页查询下达注入订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findInjectionByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询下达注入订单", notes = "分页查询下达注入订单")
    ResultData<PageResult<OrderDto>> findInjectionByPage(@RequestBody Search search);

    /**
     * 分页查询下达调整订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findAdjustmentByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询下达调整订单", notes = "分页查询下达调整订单")
    ResultData<PageResult<OrderDto>> findAdjustmentByPage(@RequestBody Search search);

    /**
     * 分页查询分解调整订单
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    @PostMapping(path = "findSplitByPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "分页查询分解调整订单", notes = "分页查询分解调整订单")
    ResultData<PageResult<OrderDto>> findSplitByPage(@RequestBody Search search);

    /**
     * 通过单据Id获取单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @PostMapping(path = "getOrderItems/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "通过单据Id获取单据行项", notes = "通过单据Id分页获取单据行项")
    ResultData<PageResult<OrderDetailDto>> getOrderItems(@PathVariable("orderId") String orderId, @RequestBody Search search);

    /**
     * 通过单据Id清空单据行项
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @PostMapping(path = "clearOrderItems")
    @ApiOperation(value = "通过单据Id清空单据行项", notes = "通过单据Id清空单据行项")
    ResultData<Void> clearOrderItems(@RequestParam("orderId") String orderId);

    /**
     * 通过单据行项id删除行项
     *
     * @param detailIds 单据Id
     * @return 业务实体
     */
    @DeleteMapping(path = "removeOrderItems", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "通过单据行项id删除行项", notes = "通过单据行项id删除行项")
    ResultData<Void> removeOrderItems(@RequestBody String[] detailIds);

    /**
     * 通过单据Id检查预算主体和类型是否被修改
     *
     * @param orderId 单据Id
     * @return 业务实体
     */
    @GetMapping(path = "checkDimension")
    @ApiOperation(value = "检查预算主体和类型是否修改", notes = "通过单据Id检查预算主体和类型是否被修改")
    ResultData<String> checkDimension(@RequestParam("orderId") String orderId,
                                      @RequestParam("subjectId") String subjectId,
                                      @RequestParam("categoryId") String categoryId);

    /**
     * 添加预算申请单行项明细
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @PostMapping(path = "addOrderDetails", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "添加预算申请单行项明细", notes = "批量添加一个预算申请单行项明细")
    ResultData<String> addOrderDetails(@RequestBody @Valid AddOrderDetail order);

    /**
     * 保存预算申请单
     *
     * @param order 业务实体DTO
     * @return 返回订单头id
     */
    @PostMapping(path = "saveOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "保存预算申请单", notes = "保存一个预算申请单")
    ResultData<String> saveOrder(@RequestBody @Valid OrderDto order);

    /**
     * 生效预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @PostMapping(path = "effectiveOrder")
    @ApiOperation(value = "生效预算申请单", notes = "直接生效预算申请单")
    ResultData<Void> effectiveOrder(@RequestParam("orderId") String orderId);

    /**
     * 提交审批预算申请单
     *
     * @param orderId 申请单id
     * @return 返回处理结果
     */
    @PostMapping(path = "commitOrder")
    @ApiOperation(value = "提交审批预算申请单", notes = "提交审批预算申请单")
    ResultData<Void> commitOrder(@RequestParam("orderId") String orderId);
}