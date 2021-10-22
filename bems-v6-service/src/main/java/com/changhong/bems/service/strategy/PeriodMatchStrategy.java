package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.context.ContextUtil;

/**
 * 实现功能：期间关系维度匹配策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:17
 */
public interface PeriodMatchStrategy extends DimensionMatchStrategy{
    /**
     * 策略类别
     *
     * @return 策略类别
     */
    @Override
    default StrategyCategory category() {
        return StrategyCategory.DIMENSION;
    }

    /**
     * 策略名称
     *
     * @return 期间关系匹配
     */
    @Override
    default String name() {
        return ContextUtil.getMessage("strategy_dimension_match_period");
    }

    /**
     * 策略描述
     *
     * @return 标准期间(年,季,月)的客观包含关系
     */
    @Override
    default String remark() {
        return ContextUtil.getMessage("strategy_dimension_match_period_remark");
    }
}
