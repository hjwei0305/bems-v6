package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.use.BudgetUse;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.service.strategy.PeriodMatchStrategy;
import com.changhong.sei.core.dto.ResultData;

/**
 * 实现功能：期间关系维度匹配策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 11:32
 */
public class DefaultPeriodMatchStrategy extends BaseMatchStrategy implements PeriodMatchStrategy {
    /**
     * 获取维度匹配值
     *
     * @param dimension 维度对象
     * @param dimValue  维度值
     * @return 返回匹配值
     */
    @Override
    public ResultData<Object> getMatchValue(BudgetUse budgetUse, Dimension dimension, String dimValue) {
        // TODO 期间关系策略
        return null;
    }
}
