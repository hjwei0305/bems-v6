package com.changhong.bems.service;

import com.changhong.bems.dao.PoolReportViewDao;
import com.changhong.bems.dto.report.AnnualBudgetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-09 15:01
 */
@Service
public class ReportService {
    @Autowired
    private PoolReportViewDao poolReportViewDao;

    /**
     * 获取年度预算分析报表数据
     *
     * @param subjectId 预算主体
     * @param year      年度
     * @return 年度预算分析报表数据结果
     */
    public List<AnnualBudgetResponse> annualBudgetAnalysis(String subjectId, Integer year) {
        return poolReportViewDao.annualBudgetAnalysis(subjectId, year);
    }
}
