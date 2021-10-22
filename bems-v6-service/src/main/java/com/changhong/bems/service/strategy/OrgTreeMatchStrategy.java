package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.StrategyCategory;
import com.changhong.sei.core.context.ContextUtil;

/**
 * 实现功能：组织机构树路径维度匹配策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 15:17
 */
public interface OrgTreeMatchStrategy extends DimensionMatchStrategy{
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
     * @return 组织机构树路径匹配
     */
    @Override
    default String name() {
        return ContextUtil.getMessage("strategy_dimension_match_org_tree");
    }

    /**
     * 策略描述
     *
     * @return 在同一条树分支路径上的节点(向上)匹配
     */
    @Override
    default String remark() {
        return ContextUtil.getMessage("strategy_dimension_match_org_tree_remark");
    }
}
