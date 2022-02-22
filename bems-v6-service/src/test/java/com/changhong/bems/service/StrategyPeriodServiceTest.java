package com.changhong.bems.service;

import com.changhong.bems.entity.StrategyPeriod;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-02-22 23:01
 */
class StrategyPeriodServiceTest extends BaseUnit5Test {
    @Autowired
    private StrategyPeriodService strategyPeriodService;

    @Test
    void save() {
    }

    @Test
    void delete() {
    }

    @Test
    void initStrategyPeriod() {
    }

    @Test
    void findBySubject() {
        List<StrategyPeriod> list = strategyPeriodService.findBySubject("AC695DAB-532E-11EC-A5BE-0242C0A84425");
        System.out.println(list);
    }

    @Test
    void getSubjectPeriod() {
    }
}