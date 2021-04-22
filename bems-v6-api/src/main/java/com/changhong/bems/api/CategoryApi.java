package com.changhong.bems.api;

import com.changhong.bems.dto.CategoryDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

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

}