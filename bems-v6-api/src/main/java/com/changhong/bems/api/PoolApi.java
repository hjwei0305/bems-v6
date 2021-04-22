package com.changhong.bems.api;

import com.changhong.bems.dto.PoolDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 预算池(Pool)API
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Valid
@FeignClient(name = "bems-v6", path = PoolApi.PATH)
public interface PoolApi extends BaseEntityApi<PoolDto>, FindByPageApi<PoolDto> {
    String PATH = "pool";

}