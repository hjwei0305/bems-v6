package com.changhong.bems.api;

import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.PeriodDto;
import com.changhong.bems.dto.SubjectItemDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算维度UI组件(Dimension)API
 *
 * @author sei
 * @since 2021-04-22 12:54:24
 */
@Valid
@FeignClient(name = "bems-v6", path = DimensionComponentApi.PATH)
public interface DimensionComponentApi {
    String PATH = "dimensionComponent";

    /**
     * 获取指定预算主体的科目(维度组件专用)
     *
     * @return 子实体清单
     */
    @GetMapping(path = "getBudgetItems")
    @ApiImplicitParam(name = "subjectId", value = "预算主体id", required = true)
    @ApiOperation(value = "获取预算科目", notes = "获取指定预算主体的科目(维度组件专用)")
    ResultData<List<SubjectItemDto>> getBudgetItems(@RequestParam("subjectId") String subjectId);

    /**
     * 按预算主体和期间类型获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    @GetMapping(path = "getBudgetPeriods")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subjectId", value = "预算主体id", required = true),
            @ApiImplicitParam(name = "type", value = "期间分类,可用值:ANNUAL,SEMIANNUAL,QUARTER,MONTHLY,CUSTOMIZE", required = true)
    })
    @ApiOperation(value = "获取期间", notes = "按预算主体和期间类型获取期间(维度组件专用)")
    ResultData<List<PeriodDto>> getPeriods(@RequestParam("subjectId") String subjectId,
                                           @RequestParam(name = "type") String type);

    /**
     * 按预算主体获取组织机构
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    @GetMapping(path = "getOrgTree")
    @ApiImplicitParam(name = "subjectId", value = "预算主体id", required = true)
    @ApiOperation(value = "获取组织机构", notes = "按预算主体获取组织机构(维度组件专用)")
    ResultData<OrganizationDto> getOrgTree(@RequestParam("subjectId") String subjectId);
}