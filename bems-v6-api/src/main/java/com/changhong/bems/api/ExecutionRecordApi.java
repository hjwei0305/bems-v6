package com.changhong.bems.api;

import com.changhong.bems.dto.ExecutionRecordDto;
import com.changhong.sei.core.api.BaseEntityApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 预算执行记录(ExecutionRecord)API
 *
 * @author sei
 * @since 2021-04-25 15:13:38
 */
@Valid
@FeignClient(name = "bems-v6", path = ExecutionRecordApi.PATH)
public interface ExecutionRecordApi {
    String PATH = "executionRecord";

}