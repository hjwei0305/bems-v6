package com.changhong.bems.api;

import com.changhong.bems.dto.StrategyDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * (Strategy)API
 *
 * @author sei
 * @since 2021-04-22 11:12:09
 */
@Valid
@FeignClient(name = "bems-v6", path = StrategyApi.PATH)
public interface StrategyApi extends BaseEntityApi<StrategyDto>, FindAllApi<StrategyDto> {
    String PATH = "strategy";

}