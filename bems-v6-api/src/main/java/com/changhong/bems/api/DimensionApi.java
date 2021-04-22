package com.changhong.bems.api;

import com.changhong.bems.dto.DimensionDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 预算维度(Dimension)API
 *
 * @author sei
 * @since 2021-04-22 12:54:24
 */
@Valid
@FeignClient(name = "bems-v6", path = DimensionApi.PATH)
public interface DimensionApi extends BaseEntityApi<DimensionDto>, FindAllApi<DimensionDto> {
    String PATH = "dimension";

}