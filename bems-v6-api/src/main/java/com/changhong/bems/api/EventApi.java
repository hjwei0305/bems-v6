package com.changhong.bems.api;

import com.changhong.bems.dto.EventDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindAllApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-11 23:07
 */
@Valid
@FeignClient(name = "bems-v6", path = EventApi.PATH)
public interface EventApi extends BaseEntityApi<EventDto>, FindByPageApi<EventDto>, FindAllApi<EventDto> {
    String PATH = "event";
}
