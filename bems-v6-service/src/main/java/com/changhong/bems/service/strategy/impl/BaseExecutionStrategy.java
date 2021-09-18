package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.dto.BudgetUseResult;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.LogRecord;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 实现功能：执行控制策略基类
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 18:29
 */
public abstract class BaseExecutionStrategy {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseExecutionStrategy.class);
    protected final PoolService poolService;

    public BaseExecutionStrategy(PoolService poolService) {
        this.poolService = poolService;
    }

    /**
     * 记录预算占用日志
     *
     * @param pool      被占用预算池
     * @param useBudget 预算占用参数
     * @param useAmount 占用金额
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordUseBudgetPool(BudgetResponse response, PoolAttributeView pool, BudgetUse useBudget, BigDecimal useAmount) {
        LogRecord record = new LogRecord(pool.getCode(), Boolean.FALSE, OperationType.USE, useAmount, useBudget.getEventCode());
        record.setSubjectId(pool.getSubjectId());
        record.setAttributeCode(pool.getAttributeCode());
        record.setBizId(useBudget.getBizId());
        record.setBizCode(useBudget.getBizCode());
        record.setBizRemark(useBudget.getBizRemark());

        // 占用记录
        poolService.recordLog(record);

        response.addUseResult(new BudgetUseResult(record.getPoolCode(), pool.getTotalAmount(), pool.getUsedAmount(),
                pool.getBalance(), record.getAmount()));
    }
}
