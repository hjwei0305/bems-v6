package com.changhong.bems.controller;

import com.changhong.bems.api.ReportApi;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.dto.PoolLogDto;
import com.changhong.bems.dto.report.*;
import com.changhong.bems.service.CategoryService;
import com.changhong.bems.service.PoolLogService;
import com.changhong.bems.service.ReportService;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实现功能：预算分析报表服务
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-29 08:41
 */
@RestController
@Api(value = "ReportApi", tags = "预算分析报表服务")
@RequestMapping(path = ReportApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportController implements ReportApi {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PoolLogService poolLogService;
    @Autowired
    private ReportService reportService;

    /**
     * 通过预算主体获取其使用的维度清单
     *
     * @param subjectId 预算主体id
     * @return 使用预算结果
     */
    @Override
    public ResultData<List<DimensionDto>> getDimensionsBySubjectId(String subjectId) {
        return ResultData.success(categoryService.findDimensionBySubject(subjectId));
    }

    /**
     * 获取预算年度
     *
     * @param subjectId 预算主体
     * @return 返回预算年度清单
     */
    @Override
    public ResultData<List<Integer>> getYears(String subjectId) {
        List<Integer> result = new ArrayList<>(7);
        int year = LocalDate.now().getYear();
        int i = 0;
        while (i < 3) {
            result.add(year - i++);
        }
        return ResultData.success(result);
    }

    @Override
    public ResultData<PageResult<PoolLogDto>> getLogRecords(Search search) {
        PageResult<PoolLogDto> pageResult = poolLogService.findByPage(search);
        return ResultData.success(pageResult);
    }

    /**
     * 预算概览报表数据
     *
     * @param request 预算概览报表数据查询
     * @return 预算概览报表数据结果
     */
    @Override
    public ResultData<List<OverviewVo>> overview(OverviewRequest request) {
        return ResultData.success(reportService.overview(request));
    }

    /**
     * 预算分析报表数据
     *
     * @param request 预算分析报表数据查询
     * @return 预算分析报表数据结果
     */
    @Override
    public ResultData<List<ExecutionAnalysisVo>> executionAnalysis(ExecutionAnalysisRequest request) {
        return ResultData.success(reportService.executionAnalysis(request));
    }

    /**
     * 预算使用趋势报表数据
     *
     * @param request 查询
     * @return 预算使用趋势报表数据结果
     */
    @Override
    public ResultData<Map<Integer, BigDecimal[]>> usageTrend(UsageTrendRequest request) {
        return ResultData.success(reportService.usageTrend(request));
    }
}
