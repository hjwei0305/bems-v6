package com.changhong.bems.service;

import com.changhong.bems.dto.report.AnnualBudgetResponse;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void annualBudgetAnalysis() {
        String subjectId = "C81A4E58-BBD4-11EB-A896-0242C0A84429";
        int year = 2021;
        List<AnnualBudgetResponse> list = service.annualBudgetAnalysis(subjectId, year, null);
        System.out.println(list);
    }
}