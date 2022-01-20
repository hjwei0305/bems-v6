package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.context.ContextUtil;

/**
 * 实现功能：强控(不允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:18
 */
public interface LimitExecutionStrategy extends BaseBudgetExecutionStrategy {
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
     * @return 强控
     */
    @Override
    default String name() {
        return ContextUtil.getMessage("strategy_execution_limit");
    }

    /**
     * 策略描述
     *
     * @return 预算使用严格控制在余额范围内
     */
    @Override
    default String remark() {
        return ContextUtil.getMessage("strategy_execution_limit_remark");
    }
}
