package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：金额类型
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 13:13
 */
public enum AmountType {
    /**
     * 预下达金额
     */
    @Remark("预下达金额")
    PRE_RELEASE,
    /**
     * 下达金额
     */
    @Remark("下达金额")
    RELEASE,
    /**
     * 已使用金额
     */
    @Remark("已使用金额")
    CONSUMED
}
