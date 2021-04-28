package com.changhong.bems.api;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}