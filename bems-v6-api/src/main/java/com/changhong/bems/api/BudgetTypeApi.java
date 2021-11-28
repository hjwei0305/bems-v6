package com.changhong.bems.api;

import com.changhong.bems.dto.AssigneDimensionRequest;
import com.changhong.bems.dto.BudgetTypeDto;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算类型(Category)API
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@Valid
@FeignClient(name = "bems-v6", path = BudgetTypeApi.PATH)
public interface BudgetTypeApi extends BaseEntityApi<BudgetTypeDto> {
    String PATH = "category";

    /**
     * 查询通用预算类型
     *
     * @return 查询结果
     */
    @GetMapping(path = "findByGeneral")
    @ApiOperation(value = "查询通用预算类型", notes = "查询通用预算类型")
    ResultData<List<BudgetTypeDto>> findByGeneral();

    /**
     * 根据预算主体查询私有预算类型
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @GetMapping(path = "findBySubject")
    @ApiOperation(value = "根据预算主体查询私有预算类型", notes = "根据预算主体查询私有预算类型")
    ResultData<List<BudgetTypeDto>> findBySubject(@RequestParam("subjectId") String subjectId);

    /**
     * 引用通用预算类型
     *
     * @param subjectId 预算主体id
     * @param id        通用预算类型id
     * @return 操作结果
     */
    @PostMapping(path = "reference/{subjectId}/{id}")
    @ApiOperation(value = "引用通用预算类型", notes = "预算主体引用通用预算类型")
    ResultData<Void> reference(@PathVariable("subjectId") String subjectId, @PathVariable("id") String id);

    /**
     * 冻结预算类型
     *
     * @param id 预算类型id
     * @return 操作结果
     */
    @PostMapping(path = "frozen/{id}")
    @ApiOperation(value = "冻结预算类型", notes = "冻结预算类型")
    ResultData<Void> frozen(@PathVariable("id") String id);

    /**
     * 解冻预算类型
     *
     * @param id 预算类型id
     * @return 操作结果
     */
    @PostMapping(path = "unfrozen/{id}")
    @ApiOperation(value = "冻结预算类型", notes = "冻结预算类型")
    ResultData<Void> unfrozen(@PathVariable("id") String id);

    /**
     * 获取未分配的预算维度
     *
     * @param categoryId 预算类型
     * @return 子实体清单
     */
    @GetMapping(path = "getUnassigned")
    @ApiOperation(value = "获取未分配的预算维度", notes = "获取未分配的预算维度")
    ResultData<List<DimensionDto>> getUnassigned(@RequestParam("categoryId") String categoryId);

    /**
     * 获取已分配的预算维度
     *
     * @param categoryId 预算类型
     * @return 子实体清单
     */
    @GetMapping(path = "getAssigned")
    @ApiOperation(value = "获取已分配的预算维度", notes = "获取已分配的预算维度")
    ResultData<List<DimensionDto>> getAssigned(@RequestParam("categoryId") String categoryId);

    /**
     * 为指定预算类型分配预算维度
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping(path = "assigne")
    @ApiOperation(value = "为指定预算类型分配预算维度", notes = "为指定预算类型分配预算维度")
    ResultData<Void> assigne(@RequestBody @Valid AssigneDimensionRequest request);

    /**
     * 解除预算类型与维度分配关系
     *
     * @param request 分配请求
     * @return 分配结果
     */
    @PostMapping(path = "unassigne")
    @ApiOperation(value = "解除预算类型与维度分配关系", notes = "解除预算类型与维度分配关系")
    ResultData<Void> unassigne(@RequestBody @Valid AssigneDimensionRequest request);

    /**
     * 通过订单类型获取预算类型
     *
     * @param category 订单类型
     * @return 业务实体
     */
    @GetMapping(path = "getByCategory")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subjectId", value = "预算主体", dataTypeClass = String.class),
            @ApiImplicitParam(name = "category", value = "管理类型(订单类型),可用值:INJECTION,ADJUSTMENT,SPLIT", dataTypeClass = String.class)
    })
    @ApiOperation(value = "通过订单类型获取预算类型", notes = "通过订单类型获取预算类型")
    ResultData<List<BudgetTypeDto>> getByCategory(@RequestParam("subjectId") String subjectId, @RequestParam("category") String category);
}