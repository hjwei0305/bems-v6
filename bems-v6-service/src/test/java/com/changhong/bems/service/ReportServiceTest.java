package com.changhong.bems.service;

import com.changhong.bems.dto.report.ExecutionAnalysisRequest;
import com.changhong.bems.dto.report.ExecutionAnalysisVo;
import com.changhong.bems.dto.report.UsageTrendRequest;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-09 15:32
 */
class ReportServiceTest extends BaseUnit5Test {

    @Autowired
    private ReportService service;

    @Test
    void usageTrend() {
        UsageTrendRequest request = new UsageTrendRequest();
        request.setSubjectId("C81A4E58-BBD4-11EB-A896-0242C0A84429");
        request.setYear(new Integer[]{2021});
        request.setItem("00001");
        Map<Integer, BigDecimal[]> map = service.usageTrend(request);
        System.out.println(map);
    }

    @Test
    void annualBudgetAnalysis() {
        ExecutionAnalysisRequest request = new ExecutionAnalysisRequest();
        request.setSubjectId("C81A4E58-BBD4-11EB-A896-0242C0A84429");
        request.setYear(2021);
        List<ExecutionAnalysisVo> list = service.executionAnalysis(request);
        System.out.println(list);
    }
}