package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.service.strategy.OrgTreeMatchStrategy;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 11:32
 */
public class DefaultOrgTreeMatchStrategy extends BaseMatchStrategy implements OrgTreeMatchStrategy {
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
