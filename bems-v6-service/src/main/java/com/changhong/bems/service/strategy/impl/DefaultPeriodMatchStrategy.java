package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.BudgetUse;
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
     * @param dimValue 维度值
     * @return 返回匹配值
     */
    @Override
    public ResultData<Object> getMatchValue(BudgetUse budgetUse, String subjectId, String dimCode, String dimValue) {
        // TODO 期间关系策略
        return null;
    }

    /**
     * @param dimensionCode 维度代码
     * @return 检查是否满足维度适用范围
     */
    @Override
    public boolean checkScope(String dimensionCode) {
        return Constants.DIMENSION_CODE_PERIOD.equals(dimensionCode);
    }
}
