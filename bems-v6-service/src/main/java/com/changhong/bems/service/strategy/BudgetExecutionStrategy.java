package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.vo.PoolLevel;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 09:32
 */
public interface BudgetExecutionStrategy {

    /**
     * 执行预算执行策略
     * 按执行策略排序预算池使用优先顺序
     *
     * @param attribute       维度组合
     * @param useBudget       预算占用参数
     * @param poolAttributes  大致预算池范围
     * @param otherDimFilters 其他维度条件
     * @return 返回执行结果
     */
    ResultData<BudgetResponse> execution(String attribute, BudgetUse useBudget, List<PoolAttributeView> poolAttributes, Collection<SearchFilter> otherDimFilters);
}
