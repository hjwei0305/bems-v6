package com.changhong.bems.dao;

import com.changhong.bems.dto.report.AnnualBudgetResponse;

import java.util.List;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-03 18:23
 */
public interface PoolReportViewExtDao {

    /**
     * 获取年度预算分析报表数据
     *
     * @param subjectId 预算主体
     * @param year      年度
     * @return 年度预算分析报表数据结果
     */
    List<AnnualBudgetResponse> annualBudgetAnalysis(String subjectId, Integer year, Set<String> itemCodes);
}
