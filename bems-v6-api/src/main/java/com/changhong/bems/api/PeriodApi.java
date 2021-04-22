package com.changhong.bems.api;

import com.changhong.bems.dto.CreateCustomizePeriodRequest;
import com.changhong.bems.dto.CreateNormalPeriodRequest;
import com.changhong.bems.dto.PeriodDto;
import com.changhong.sei.core.api.BaseEntityApi;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping(path = "findBySubject")
    @ApiOperation(value = "按预算主体获取期间", notes = "按预算主体获取期间")
    ResultData<List<PeriodDto>> findBySubject(@RequestParam("subjectId") String subjectId,
                                              @RequestParam("type") String type);

    /**
     * 关闭预算期间
     *
     * @param ids 预算期间id
     * @return 期间清单
     */
    @PostMapping(path = "closePeriods", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "关闭预算期间", notes = "关闭预算期间")
    ResultData<Void> closePeriods(@RequestBody List<String> ids);

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
    ResultData<Void> saveCustomizePeriod(@RequestBody @Valid CreateCustomizePeriodRequest request);
}