package com.changhong.bems.api;

import com.changhong.bems.dto.SubjectDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import org.springframework.cloud.openfeign.FeignClient;

import javax.validation.Valid;

/**
 * 预算主体(Subject)API
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@Valid
@FeignClient(name = "bems-v6", path = SubjectApi.PATH)
public interface SubjectApi extends BaseEntityApi<SubjectDto>, FindByPageApi<SubjectDto> {
    String PATH = "subject";

}