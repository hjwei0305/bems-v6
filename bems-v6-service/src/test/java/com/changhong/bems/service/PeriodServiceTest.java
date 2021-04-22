package com.changhong.bems.service;

import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-23 00:56
 */
class PeriodServiceTest extends BaseUnit5Test {
    @Autowired
    private PeriodService service;

    @Test
    void findBySubject() {
    }

    @Test
    void closePeriods() {
    }

    @Test
    void createNormalPeriod() {
        String subjectId = "1111";
        int year = 2021;
        PeriodType[] periodTypes = new PeriodType[]{PeriodType.ANNUAL, PeriodType.QUARTER, PeriodType.MONTHLY};
        ResultData<Void> resultData = service.createNormalPeriod(subjectId, year, periodTypes);
        System.out.println(resultData);
    }

    @Test
    void saveCustomizePeriod() {
    }
}