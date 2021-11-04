package com.changhong.bems.api;

import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.dto.LogRecordViewDto;
import com.changhong.bems.dto.PoolLogDto;
import com.changhong.bems.dto.report.ExecutionAnalysisRequest;
import com.changhong.bems.dto.report.ExecutionAnalysisResponse;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 获取预算年度
     *
     * @param subjectId 预算主体
     * @return 返回预算年度清单
     */
    @GetMapping(path = "getYears/{subjectId}")
    @ApiOperation(value = "获取预算年度清单", notes = "获取预算年度清单")
    ResultData<List<Integer>> getYears(@PathVariable("subjectId") String subjectId);

    /**
     * 查询预算执行日志
     *
     * @param search 查询条件
     * @return 返回查询结果
     */
    @PostMapping(path = "getLogRecords", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "查询预算执行日志", notes = "查询预算执行日志")
    ResultData<PageResult<PoolLogDto>> getLogRecords(@RequestBody Search search);

    /**
     * 预算分析报表数据
     *
     * @param request 预算分析报表数据查询
     * @return 预算分析报表数据结果
     */
    @PostMapping(path = "executionAnalysis", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "预算分析报表数据", notes = "预算分析报表数据")
    ResultData<List<ExecutionAnalysisResponse>> executionAnalysis(@RequestBody ExecutionAnalysisRequest request);

    /**
     * 获取年度预算使用趋势报表数据
     *
     * @param subjectId 预算主体
     * @param itemCode  预算科目
     * @param years     查询年度
     * @return 年度预算分析报表数据结果
     */
    @PostMapping(path = "annualUsageTrend/{subjectId}/{itemCode}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "获取年度预算使用趋势报表数据", notes = "获取年度预算使用趋势报表数据")
    ResultData<Map<Integer, BigDecimal[]>> annualUsageTrend(@PathVariable("subjectId") String subjectId,
                                                            @PathVariable("itemCode") String itemCode,
                                                            @RequestBody @NotEmpty Set<Integer> years);
}