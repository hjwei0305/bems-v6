package com.changhong.bems.api;

import com.changhong.bems.dto.CategoryDto;
import com.changhong.bems.dto.CreateCategoryDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * 预算类型(Category)API
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@Valid
@FeignClient(name = "bems-v6", path = CategoryApi.PATH)
public interface CategoryApi extends BaseEntityApi<CategoryDto>, FindByPageApi<CategoryDto> {
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
}