package com.changhong.bems.api;

import com.changhong.bems.dto.*;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.api.DataAuthEntityApi;
import com.changhong.sei.core.api.FindByPageApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 按公司代码获取组织机构树(不包含冻结)
     *
     * @param corpCode 公司代码
     * @return 组织机构树清单
     */
    @GetMapping(path = "findOrgTreeByCorpCode")
    @ApiOperation(value = "按公司代码获取组织机构树(不包含冻结)", notes = "按公司代码获取组织机构树(不包含冻结)")
    ResultData<OrganizationDto> findOrgTreeByCorpCode(@RequestParam("corpCode") String corpCode);

    /**
     * 按组织级主体id获取分配的组织机构
     *
     * @param id 组织级主体id
     * @return 分配的组织机构清单
     */
    @GetMapping(path = "getSubjectOrganizations")
    @ApiOperation(value = "按组织级主体id获取分配的组织机构", notes = "按组织级主体id获取分配的组织机构")
    ResultData<List<SubjectOrganizationDto>> getSubjectOrganizations(@RequestParam("id") String id);
}