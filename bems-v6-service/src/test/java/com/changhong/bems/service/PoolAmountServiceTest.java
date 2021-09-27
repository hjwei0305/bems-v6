package com.changhong.bems.service;

import com.changhong.bems.dto.PoolAmountQuotaDto;
import com.changhong.sei.core.test.BaseUnit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-19 01:44
 */
class PoolAmountServiceTest extends BaseUnit5Test {
    @Autowired
    private PoolAmountService service;

    @Test
    void getPoolBalanceByPoolCode() {
    }

    @Test
    void countAmount() {
    }

    @Test
    void getPoolAmountQuota() {
        PoolAmountQuotaDto quota = service.getPoolAmountQuota("0000015805");
        System.out.println(quota);
    }
}