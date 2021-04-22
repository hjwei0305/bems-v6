package com.changhong.bems.api;

import com.changhong.bems.dto.ItemDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 预算科目(Item)API
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Valid
@FeignClient(name = "bems-v6", path = ItemApi.PATH)
public interface ItemApi extends BaseEntityApi<ItemDto>, FindByPageApi<ItemDto> {
    String PATH = "item";

}