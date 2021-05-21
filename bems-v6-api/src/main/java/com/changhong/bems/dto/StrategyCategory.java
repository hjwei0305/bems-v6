package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：策略类别
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 13:04
 */
public enum StrategyCategory {
    /**
     * 维度策略
     */
    @Remark("维度策略")
    DIMENSION,
    /**
     * 执行策略
     */
    @Remark("执行策略")
    EXECUTION
}
