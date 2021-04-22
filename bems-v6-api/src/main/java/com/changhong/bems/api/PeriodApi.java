package com.changhong.bems.api;

import com.changhong.bems.dto.PeriodDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 预算期间(Period)API
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Valid
@FeignClient(name = "bems-v6", path = PeriodApi.PATH)
public interface PeriodApi extends BaseEntityApi<PeriodDto>, FindByPageApi<PeriodDto> {
    String PATH = "period";

}