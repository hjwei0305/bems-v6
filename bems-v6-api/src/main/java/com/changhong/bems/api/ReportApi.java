package com.changhong.bems.api;

import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.dto.KeyValueDto;
import com.changhong.sei.core.dto.ResultData;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 预算分析报表(BudgetReport)API
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Valid
@FeignClient(name = "bems-v6", path = ReportApi.PATH)
public interface ReportApi {
    String PATH = "report";

    /**
     * 通过预算主体获取其使用的维度清单
     *
     * @param subjectId 预算主体id
     * @return 使用预算结果
     */
    @GetMapping(path = "getDimensionsBySubjectId")
    @ApiOperation(value = "预算主体获取在使用的维度", notes = "通过预算主体获取其使用的维度清单")
    ResultData<List<DimensionDto>> getDimensionsBySubjectId(@RequestParam String subjectId);

}