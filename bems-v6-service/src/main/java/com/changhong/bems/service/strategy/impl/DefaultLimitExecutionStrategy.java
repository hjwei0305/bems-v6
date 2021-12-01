package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.service.PoolService;
import com.changhong.bems.service.strategy.LimitExecutionStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * 实现功能：强控(不允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-21 14:40
 */
public class DefaultLimitExecutionStrategy extends BaseExecutionStrategy implements LimitExecutionStrategy {

    public DefaultLimitExecutionStrategy(PoolService poolService) {
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
    public ResultData<BudgetResponse> execution(PoolAttributeDto optimalPool, BudgetUse useBudget, Collection<SearchFilter> otherDimFilters) {
        // 占用总金额
        BigDecimal useAmount = useBudget.getAmount();
        // 预算池代码
        String poolCode = optimalPool.getCode();
        // 当前预算池余额
        BigDecimal poolBalance = poolService.getPoolBalanceByCode(poolCode);
        if (useAmount.compareTo(poolBalance) > 0) {
            // 预算占用时,当前余额[{0}]不满足占用金额[{1}]!
            String message = ContextUtil.getMessage("pool_00013", poolBalance, useAmount);
            LOG.error(message);
            return ResultData.fail(message);
        }
        // 占用结果
        BudgetResponse response = new BudgetResponse();
        response.setBizId(useBudget.getBizId());
        this.recordUseBudgetPool(response, optimalPool, useBudget, useAmount);
        return ResultData.success(response);
    }
}
