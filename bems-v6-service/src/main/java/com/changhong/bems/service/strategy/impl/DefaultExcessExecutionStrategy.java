package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import com.changhong.bems.service.strategy.ExcessExecutionStrategy;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * 实现功能：弱控(允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-21 14:40
 */
public class DefaultExcessExecutionStrategy extends BaseExecutionStrategy implements ExcessExecutionStrategy {

    public DefaultExcessExecutionStrategy(PoolService poolService) {
        super(poolService);
    }

    /**
     * 执行预算执行策略
     *
     * @param optimalPool     最优占用预算池
     * @param useBudget       预算占用参数
     * @param otherDimFilters 其他占用维度
     * @return 返回执行结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultData<BudgetResponse> execution(PoolAttributeView optimalPool, BudgetUse useBudget, Collection<SearchFilter> otherDimFilters) {
        // 占用总金额
        double useAmount = useBudget.getAmount();
        // 允许超额使用,即直接占用预算,不做余额检查

        // 占用结果
        BudgetResponse response = new BudgetResponse();
        response.setBizId(useBudget.getBizId());
        this.recordUseBudgetPool(response, optimalPool, useBudget, useAmount);
        return ResultData.success(response);
    }
}
