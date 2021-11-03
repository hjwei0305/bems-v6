package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.OperationType;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.use.BudgetResponse;
import com.changhong.bems.dto.use.BudgetUse;
import com.changhong.bems.dto.use.BudgetUseResult;
import com.changhong.bems.entity.PoolLog;
import com.changhong.bems.service.PoolService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.StringJoiner;

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
    public void recordUseBudgetPool(BudgetResponse response, PoolAttributeDto pool, BudgetUse useBudget, BigDecimal useAmount) {
        PoolLog record = new PoolLog(pool.getCode(), Boolean.FALSE, OperationType.USE, useAmount, useBudget.getEventCode());
        record.setSubjectId(pool.getSubjectId());
        record.setAttributeCode(pool.getAttributeCode());
        record.setBizId(useBudget.getBizId());
        record.setBizCode(useBudget.getBizCode());
        record.setBizRemark(useBudget.getBizRemark());

        // 占用记录
        poolService.recordLog(record);

        if (BigDecimal.ZERO.compareTo(useAmount) == 0) {
            // 如果占用金额等于0,则不返回占用结果
            return;
        }

        BudgetUseResult result = new BudgetUseResult(record.getPoolCode(), pool.getTotalAmount(), pool.getUsedAmount(),
                pool.getBalance(), record.getAmount());
        StringJoiner display = new StringJoiner("|")
                // 期间
                .add(pool.getPeriodName())
                // 科目
                .add(pool.getItemName());
        // 组织
        if (StringUtils.isNotBlank(pool.getOrgName())) {
            display.add(pool.getOrgName());
        }
        // 项目
        if (StringUtils.isNotBlank(pool.getProjectName())) {
            display.add(pool.getProjectName());
        }
        // UDF1
        if (StringUtils.isNotBlank(pool.getUdf1Name())) {
            display.add(pool.getUdf1Name());
        }
        // UDF2
        if (StringUtils.isNotBlank(pool.getUdf2Name())) {
            display.add(pool.getUdf2Name());
        }
        // UDF3
        if (StringUtils.isNotBlank(pool.getUdf3Name())) {
            display.add(pool.getUdf3Name());
        }
        // UDF4
        if (StringUtils.isNotBlank(pool.getUdf4Name())) {
            display.add(pool.getUdf4Name());
        }
        // UDF5
        if (StringUtils.isNotBlank(pool.getUdf5Name())) {
            display.add(pool.getUdf5Name());
        }
        result.setDisplay(display.toString());
        response.addUseResult(result);
    }
}
