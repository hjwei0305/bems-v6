package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.dto.ResultData;

/**
 * 实现功能：预算维度匹配策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 09:32
 */
public interface BaseDimensionMatchStrategy extends AbstractStrategy {

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
     * 获取维度匹配值
     *
     * @param dimValue 维度值
     * @return 返回匹配值
     */
    ResultData<Object> getMatchValue(BudgetUse budgetUse, String dimValue);

    /**
     * @param dimensionCode 维度代码
     * @return 检查是否满足维度适用范围
     */
    boolean checkScope(String dimensionCode);
}
