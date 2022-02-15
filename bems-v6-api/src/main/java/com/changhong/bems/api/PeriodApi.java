package com.changhong.bems.api;

import com.changhong.bems.dto.CreateCustomizePeriodRequest;
import com.changhong.bems.dto.CreateNormalPeriodRequest;
import com.changhong.bems.dto.PeriodDto;
import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算期间(Period)API
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Valid
@FeignClient(name = "bems-v6", path = PeriodApi.PATH)
public interface PeriodApi extends BaseEntityApi<PeriodDto> {
    String PATH = "period";

    /**
     * 按预算主体获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subjectId", value = "预算主体id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "type", value = "期间分类,可用值:ANNUAL,SEMIANNUAL,QUARTER,MONTHLY,CUSTOMIZE", dataTypeClass = String.class)
    })
    @GetMapping(path = "findBySubject")
    @ApiOperation(value = "按预算主体获取期间", notes = "按预算主体获取期间")
    ResultData<List<PeriodDto>> findBySubject(@RequestParam("subjectId") String subjectId,
                                              @RequestParam(name = "type", required = false) String type);

    /**
     * 按预算主体获取期间
     *
     * @param subjectId 预算主体id
     * @param year      年度
     * @param type      预算期间类型
     * @return 期间清单
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subjectId", value = "预算主体id", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "year", value = "年度", dataTypeClass = Integer.class, required = true),
            @ApiImplicitParam(name = "type", value = "期间类型", dataTypeClass = String.class)
    })
    @GetMapping(path = "getBySubject")
    @ApiOperation(value = "按预算主体获取期间", notes = "按预算主体获取期间")
    ResultData<List<PeriodDto>> getBySubject(@RequestParam("subjectId") String subjectId,
                                             @RequestParam("year") Integer year,
                                             @RequestParam(name = "type", required = false) PeriodType type);

    /**
     * 设置预算期间状态
     *
     * @param id     预算期间id
     * @param status 预算期间状态
     * @return 期间清单
     */
    @PostMapping(path = "setPeriodStatus/{id}/{status}")
    @ApiOperation(value = "设置预算期间状态", notes = "设置预算期间状态, status为true:启用;为false:停用")
    ResultData<Void> setPeriodStatus(@PathVariable("id") String id, @PathVariable("status") boolean status);

    /**
     * 关闭过期预算期间(调度定时任务)
     * 定时任务执行，关闭过期预算期间
     *
     * @return 操作结果
     */
    @PostMapping(path = "closingOverduePeriod")
    @ApiOperation(value = "定时任务执行，关闭过期预算期间", notes = "关闭过期预算期间(调度定时任务)")
    ResultData<Void> closingOverduePeriod();

    /**
     * 创建标准期间
     *
     * @param request 预算主体id
     * @return 期间清单
     */
    @PostMapping(path = "createNormalPeriod", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建标准期间", notes = "创建标准期间")
    ResultData<Void> createNormalPeriod(@RequestBody @Valid CreateNormalPeriodRequest request);

    /**
     * 创建/编辑自定义期间
     *
     * @param request 预算主体id
     * @return 期间清单
     */
    @PostMapping(path = "saveCustomizePeriod", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "创建/编辑自定义期间", notes = "创建/编辑自定义期间")
    ResultData<PeriodDto> saveCustomizePeriod(@RequestBody @Valid CreateCustomizePeriodRequest request);

}