package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.context.ContextUtil;

/**
 * 实现功能：同期间总额执行策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:19
 */
public interface SamePeriodTotalLimitExecutionStrategy extends BudgetExecutionStrategy {
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
     * @return 同期间总额控
     */
    @Override
    default String name() {
        return ContextUtil.getMessage("strategy_execution_same_period");
    }

    /**
     * 策略描述
     *
     * @return 在同期间类型范围内, 预算总额控制
     */
    @Override
    default String remark() {
        return ContextUtil.getMessage("strategy_execution_same_period_remark");
    }
}
