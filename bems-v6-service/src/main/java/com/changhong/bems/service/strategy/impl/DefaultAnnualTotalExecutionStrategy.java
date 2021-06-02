package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import com.changhong.bems.service.strategy.AnnualTotalExecutionStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

/**
 * 实现功能：年度总额控制
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-21 14:41
 */
public class DefaultAnnualTotalExecutionStrategy extends BaseExecutionStrategy implements AnnualTotalExecutionStrategy {

    public DefaultAnnualTotalExecutionStrategy(PoolService poolService) {
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
        // 占用结果
        BudgetResponse response = new BudgetResponse();
        response.setBizId(useBudget.getBizId());

        // 占用总金额
        double amount = useBudget.getAmount();
        // 当前预算池余额
        double poolBalance = poolService.getPoolBalanceByCode(optimalPool.getCode());
        // 检查当前预算池余额是否满足占用
        if (amount > poolBalance) {
            // 预算占用日期
            LocalDate useDate = LocalDate.parse(useBudget.getDate(), DateTimeFormatter.ISO_DATE);
            // 获取同期间预算池(含自己但不含占用日期之前的预算池)
            final List<PoolAttributeView> poolAttributes = poolService.getSamePeriodBudgetPool(optimalPool, useDate);
            // 已占用金额
            double useAmount = 0;
            for (PoolAttributeView pool : poolAttributes) {
                // 需要占用的金额 = 占用总额 -已占额
                double needAmount = amount - useAmount;
                if (needAmount == 0) {
                    break;
                }
                // 当前预算池余额
                double poolAmount = poolService.getPoolBalanceByCode(pool.getCode());
                // 需要占用金额 >= 预算池余额
                if (needAmount >= poolAmount) {
                    // 占用全部预算池金额
                    this.recordUseBudgetPool(response, pool, useBudget, poolAmount);
                    useAmount += poolAmount;
                } else {
                    // 占用部分预算池金额
                    this.recordUseBudgetPool(response, pool, useBudget, needAmount);
                    useAmount += needAmount;
                }
            }
            if (amount > useAmount || amount < useAmount) {
                // 预算占用时,当前余额[{0}]不满足占用金额[{1}]!
                return ResultData.fail(ContextUtil.getMessage("pool_00013", useAmount, amount));
            }
            return ResultData.success(response);
        } else {
            // 余额满足直接占用
            this.recordUseBudgetPool(response, optimalPool, useBudget, amount);
            return ResultData.success(response);
        }
    }
}
