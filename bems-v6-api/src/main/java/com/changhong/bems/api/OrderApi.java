package com.changhong.bems.api;

import com.changhong.bems.dto.OrderDto;
import com.changhong.sei.core.api.BaseEntityApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

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

}