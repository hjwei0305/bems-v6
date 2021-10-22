package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.context.ContextUtil;

/**
 * 实现功能：一致性维度匹配策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:16
 */
public interface EqualMatchStrategy extends DimensionMatchStrategy {
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
     * @return 维度值一致性匹配
     */
    @Override
    default String name() {
        return ContextUtil.getMessage("strategy_dimension_match_equal");
    }

    /**
     * 策略描述
     *
     * @return 维度值一致性匹配
     */
    @Override
    default String remark() {
        return ContextUtil.getMessage("strategy_dimension_match_equal_remark");
    }
}
