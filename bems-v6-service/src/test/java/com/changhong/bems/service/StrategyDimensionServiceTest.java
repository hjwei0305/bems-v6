package com.changhong.bems.service;

import com.changhong.bems.dto.DimensionDto;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-02 13:17
 */
class StrategyDimensionServiceTest extends BaseUnit5Test {
    @Autowired
    private StrategyDimensionService service;

    @Test
    void getDimensionsByClassification() {
    }

    @Test
    void getDimensions() {
        String subjectId = "AC695DAB-532E-11EC-A5BE-0242C0A84425";
        List<DimensionDto> list = service.getDimensions(subjectId);
        System.out.println(list);
    }

    @Test
    void getDimension() {
    }

    @Test
    void setSubjectDimension() {
    }

    @Test
    void setDimensionStrategy() {
    }
}