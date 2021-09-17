package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 13:09
 */
public enum PeriodType {
    /**
     * 年度
     */
    @Remark("年度")
    ANNUAL,
    /**
     * 半年度
     */
    @Remark("半年度")
    SEMIANNUAL,
    /**
     * 季度
     */
    @Remark("季度")
    QUARTER,
    /**
     * 月度
     */
    @Remark("月度")
    MONTHLY,
    /**
     * 自定义
     */
    @Remark("自定义")
    CUSTOMIZE
}
