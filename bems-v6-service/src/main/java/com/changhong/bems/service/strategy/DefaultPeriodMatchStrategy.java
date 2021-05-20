package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.entity.Dimension;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 11:32
 */
public class DefaultPeriodMatchStrategy extends BaseMatchStrategy implements DimensionMatchStrategy {
    /**
     * 获取维度匹配值
     *
     * @param dimension 维度对象
     * @param dimValue  维度值
     * @return 返回匹配值
     */
    @Override
    public Object getMatchValue(BudgetUse budgetUse, Dimension dimension, String dimValue) {
        return null;
    }
}
