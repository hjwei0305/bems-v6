package com.changhong.bems.dto.report;

import java.math.BigDecimal;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-04 18:00
 */
public class OverviewDataItemVo {
    private final Integer year;
    private final Integer month;
    private final BigDecimal balance;
    private final BigDecimal used;

    public OverviewDataItemVo(Integer year, Integer month, BigDecimal injected, BigDecimal used) {
        this.year = year;
        this.month = month;
        this.balance = injected.subtract(used);
        this.used = used;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getUsed() {
        return used;
    }
}
