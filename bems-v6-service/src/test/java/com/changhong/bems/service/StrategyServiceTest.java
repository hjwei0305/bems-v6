package com.changhong.bems.service;

import com.changhong.bems.dto.StrategyDto;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-01-20 15:51
 */
class StrategyServiceTest extends BaseUnit5Test {
    @Autowired
    private StrategyService service;

    @Test
    void getMatchStrategy() {
    }

    @Test
    void getExecutionStrategy() {
    }

    @Test
    void getByCode() {
    }

    @Test
    void getNameByCode() {
    }

    @Test
    void findAll() {
    }

    @Test
    void findByCategory() {
    }

    @Test
    void findByDimensionCode() {
        List<StrategyDto> list = service.findByDimensionCode("org");
        System.out.println(list);
    }

    @Test
    void getStrategy() {
    }
}