package com.changhong.bems.dao;

import com.changhong.bems.dto.report.*;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-04 13:49
 */
public interface PoolAttributeAmountExtDao {

    /**
     * 预算概览分析
     *
     * @param request 查询
     * @return 结果
     */
    List<OverviewDataItemVo> overview(OverviewRequest request);

    /**
     * 预算分析报表数据
     *
     * @param request 预算分析报表数据查询
     * @return 预算分析报表数据结果
     */
    List<ExecutionAnalysisVo> executionAnalysis(ExecutionAnalysisRequest request);

    /**
     * 预算使用趋势报表数据
     *
     * @param request 查询
     * @return 预算使用趋势报表数据结果
     */
    List<UsageTrendVo> usageTrend(UsageTrendRequest request);
}
