package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;

/**
 * 实现功能：强控(不允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:18
 */
public interface LimitExecutionStrategy extends BudgetExecutionStrategy {
    /**
     * 策略类别
     *
     * @return 策略类别
     */
    @Override
    default StrategyCategory category() {
        return StrategyCategory.EXECUTION;
    }

    /**
     * 策略名称
     *
     * @return 策略名称
     */
    @Override
    default String name() {
        return "强控";
    }

    /**
     * 策略描述
     *
     * @return 策略描述
     */
    @Override
    default String remark() {
        return "必须在预算额度范围内使用";
    }
}
