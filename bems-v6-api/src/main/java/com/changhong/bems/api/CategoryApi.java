package com.changhong.bems.api;

import com.changhong.bems.dto.CategoryDto;
import com.changhong.bems.dto.CreateCategoryDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
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
@FeignClient(name = "bems-v6", path = CategoryApi.PATH)
public interface CategoryApi extends BaseEntityApi<CategoryDto> {
    String PATH = "category";

    /**
     * 创建预算类型
     *
     * @param dto 业务实体DTO
     * @return 操作结果
     */
    @PostMapping(path = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建预算类型", notes = "创建预算类型")
    ResultData<Void> create(@RequestBody @Valid CreateCategoryDto dto);

    /**
     * 查询通用预算类型
     *
     * @return 查询结果
     */
    @GetMapping(path = "findByGeneral")
    @ApiOperation(value = "查询通用预算类型", notes = "查询通用预算类型")
    ResultData<List<CategoryDto>> findByGeneral();

    /**
     * 根据预算主体查询私有预算类型
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @GetMapping(path = "findBySubject")
    @ApiOperation(value = "根据预算主体查询私有预算类型", notes = "根据预算主体查询私有预算类型")
    ResultData<List<CategoryDto>> findBySubject(@RequestParam("subjectId") String subjectId);

    /**
     * 创建预算类型
     *
     * @param subjectId 预算主体id
     * @param id        通用预算类型id
     * @return 操作结果
     */
    @PostMapping(path = "reference/{subjectId}/{id}")
    @ApiOperation(value = "创建预算类型", notes = "创建预算类型")
    ResultData<Void> reference(@PathVariable("subjectId") String subjectId, @PathVariable("id") String id);

}