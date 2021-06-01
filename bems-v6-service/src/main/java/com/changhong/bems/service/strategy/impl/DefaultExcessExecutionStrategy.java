package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.dto.BudgetUseResult;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.bems.service.strategy.ExcessExecutionStrategy;
import com.changhong.bems.service.vo.PoolLevel;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现功能：弱控(允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-21 14:40
 */
public class DefaultExcessExecutionStrategy extends BaseExecutionStrategy implements ExcessExecutionStrategy {

    @Autowired
    private OrganizationManager organizationManager;
    @Autowired
    private PoolService poolService;

    @Override
    public OrganizationManager getOrgManager() {
        return organizationManager;
    }

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
    @Override
    public ResultData<BudgetResponse> execution(String attribute, BudgetUse useBudget, List<PoolAttributeView> poolAttributes, Collection<SearchFilter> otherDimFilters) {
        ResultData<Map<String, PoolLevel>> resultData = this.sortPools(attribute, useBudget, poolAttributes);
        if (resultData.failed()) {
            return ResultData.fail(resultData.getMessage());
        }
        Map<String, PoolLevel> poolLevelMap = resultData.getData();
        if (Objects.nonNull(poolLevelMap) && poolLevelMap.size() > 0) {
            BudgetResponse response = new BudgetResponse();
            response.setBizId(useBudget.getBizId());
            // 占用总金额
            double amount = useBudget.getAmount();
            // 已占用金额
            double useAmount = 0;

            ExecutionRecord record;
            //
            List<PoolLevel> pools = poolLevelMap.values().stream().sorted(Comparator.comparingLong(PoolLevel::getLevel)).collect(Collectors.toList());
            for (PoolLevel poolLevel : pools) {
                // 预算池代码
                String poolCode = poolLevel.getPoolCode();
                // 需要占用的金额 = 占用总额 -已占额
                double needAmount = amount - useAmount;
                if (needAmount == 0) {
                    break;
                }
                // 当前预算池余额
                double poolAmount = poolService.getPoolBalanceByCode(poolCode);
                // 需要占用金额 >= 预算池余额
                if (needAmount >= poolAmount) {
                    // 占用全部预算池金额
                    record = new ExecutionRecord(poolCode, OperationType.USE, poolAmount, useBudget.getEventCode());

                    useAmount += poolAmount;
                } else {
                    // 占用部分预算池金额
                    record = new ExecutionRecord(poolCode, OperationType.USE, needAmount, useBudget.getEventCode());
                    useAmount += needAmount;
                }
                record.setSubjectId(poolLevel.getSubjectId());
                record.setAttributeCode(poolLevel.getAttributeCode());
                record.setBizId(useBudget.getBizId());
                record.setBizCode(useBudget.getBizCode());
                record.setBizRemark(useBudget.getBizRemark());

                // 占用记录
                poolService.recordLog(record);
                // 占用结果
                response.addUseResult(new BudgetUseResult(poolCode, record.getAmount()));
            }
            if (amount > useAmount || amount < useAmount) {
                // 预算占用时,当前余额[{0}]不满足占用金额[{1}]!
                return ResultData.fail(ContextUtil.getMessage("pool_00013", useAmount, amount));
            }
            return ResultData.success(response);
        } else {
            // 预算占用时,未找到满足条件[{0}]的预算池!
            return ResultData.fail(ContextUtil.getMessage("pool_00009", "强控策略"));
        }
    }
}
