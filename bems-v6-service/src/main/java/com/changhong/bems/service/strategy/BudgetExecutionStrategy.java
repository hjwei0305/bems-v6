package com.changhong.bems.service.strategy;

import com.changhong.bems.dto.use.BudgetResponse;
import com.changhong.bems.dto.use.BudgetUse;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;

import java.util.Collection;

/**
 * 实现功能：预算执行策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 09:32
 */
public interface BudgetExecutionStrategy {

    /**
     * 执行预算执行策略
     *
     * @param optimalPool     最优占用预算池
     * @param useBudget       预算占用参数
     * @param otherDimFilters 其他占用维度
     * @return 返回执行结果
     */
    ResultData<BudgetResponse> execution(PoolAttributeView optimalPool, BudgetUse useBudget, Collection<SearchFilter> otherDimFilters);
}
