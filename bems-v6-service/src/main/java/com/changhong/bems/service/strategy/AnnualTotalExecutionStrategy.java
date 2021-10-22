package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;

/**
 * 实现功能：年度总额控制执行策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:19
 */
public interface AnnualTotalExecutionStrategy extends BudgetExecutionStrategy {
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
        return "组织机构树路径匹配";
    }

    /**
     * 策略描述
     *
     * @return 策略描述
     */
    @Override
    default String remark() {
        return "组织机构树路径匹配";
    }
}
