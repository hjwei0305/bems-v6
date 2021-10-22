package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.context.ContextUtil;

/**
 * 实现功能：弱控(允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:18
 */
public interface ExcessExecutionStrategy extends BudgetExecutionStrategy {
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
     * @return 弱控
     */
    @Override
    default String name() {
        return ContextUtil.getMessage("strategy_execution_excess");
    }

    /**
     * 策略描述
     *
     * @return 可超额使用预算,即预算池余额不够时可超额使用
     */
    @Override
    default String remark() {
        return ContextUtil.getMessage("strategy_execution_excess_remark");
    }
}
