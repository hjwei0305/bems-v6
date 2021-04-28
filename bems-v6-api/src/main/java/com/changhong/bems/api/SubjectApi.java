package com.changhong.bems.api;

import com.changhong.bems.dto.CorporationDto;
import com.changhong.bems.dto.CurrencyDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.SubjectDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.DataAuthEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算主体(Subject)API
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@Valid
@FeignClient(name = "bems-v6", path = SubjectApi.PATH)
public interface SubjectApi extends BaseEntityApi<SubjectDto>, FindByPageApi<SubjectDto>, DataAuthEntityApi<SubjectDto> {
    String PATH = "subject";

    /**
     * 获取币种数据
     *
     * @return 查询结果
     */
    @GetMapping(path = "findCurrencies")
    @ApiOperation(value = "获取币种数据", notes = "获取币种数据")
    ResultData<List<CurrencyDto>> findCurrencies();

    /**
     * 获取当前用户有权限的公司
     *
     * @return 当前用户有权限的公司
     */
    @GetMapping(path = "findUserAuthorizedCorporations")
    @ApiOperation(value = "获取当前用户有权限的公司", notes = "获取当前用户有权限的公司")
    ResultData<List<CorporationDto>> findUserAuthorizedCorporations();

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    @GetMapping(path = "findOrgTree")
    @ApiOperation(value = "获取组织机构树(不包含冻结)", notes = "获取组织机构树(不包含冻结)")
    ResultData<List<OrganizationDto>> findOrgTree();
}