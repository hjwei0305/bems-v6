package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-22 19:31
 */
public interface BaseStrategy {
    /**
     * 策略类别
     *
     * @return 策略类别
     */
    StrategyCategory category();

    /**
     * 策略名称
     *
     * @return 策略名称
     */
    String name();

    /**
     * 策略描述
     *
     * @return 策略描述
     */
    String remark();
}
