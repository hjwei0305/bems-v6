package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.PoolService;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.bems.service.strategy.LimitExecutionStrategy;
import com.changhong.bems.service.vo.PoolLevel;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现功能：强控(不允许超额)策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-21 14:40
 */
public class DefaultLimitExecutionStrategy extends BaseExecutionStrategy implements LimitExecutionStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLimitExecutionStrategy.class);

    @Autowired
    private OrganizationManager organizationManager;
    @Autowired
    private PoolService poolService;

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
        Map<String, PoolLevel> poolLevelMap = null;

        // 组织id
        String orgId = useBudget.getOrg();
        // 检查是否包含组织维度
        if (attribute.contains(Constants.DIMENSION_CODE_ORG)) {
            // 按id进行映射方便后续使用
            Map<String, OrganizationDto> orgMap = null;
            if (Objects.nonNull(orgId)) {
                orgId = orgId.trim();
                if (StringUtils.isNotBlank(orgId) && !StringUtils.equalsIgnoreCase(Constants.NONE, orgId)) {
                    // 获取指定节点的所有父节点(含自己)
                    ResultData<List<OrganizationDto>> resultData = organizationManager.getParentNodes(useBudget.getOrg(), Boolean.TRUE);
                    if (resultData.successful()) {
                        List<OrganizationDto> orgList = resultData.getData();
                        if (CollectionUtils.isNotEmpty(orgList)) {
                            // 组织id映射
                            orgMap = orgList.stream().collect(Collectors.toMap(OrganizationDto::getId, o -> o));
                            orgList.clear();
                        }
                    } else {
                        return ResultData.fail(resultData.getMessage());
                    }
                }
            }
            if (Objects.isNull(orgMap)) {
                // 预算占用时,组织维度值不能为空!
                return ResultData.fail(ContextUtil.getMessage("pool_00012"));
            }

            /*
            组织机构向上查找规则:
            1.按组织机构树路径,从预算占用的节点开始,向上依次查找
            2.当按组织节点找到存在的预算池,不管余额是否满足,都将停止向上查找
             */
            boolean isUp = true;
            String parentId = useBudget.getOrg();
            OrganizationDto org = orgMap.get(parentId);
            while (isUp && Objects.nonNull(org)) {
                String oId = org.getId();
                // 按组织id匹配预算池
                List<PoolAttributeView> pools = poolAttributes.stream().filter(p -> StringUtils.equals(oId, p.getOrg())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(pools)) {
                    // 通过组织id匹配到预算池进行排序
                    poolLevelMap = this.sortByPeriod(pools);
                    isUp = false;
                } else {
                    // 没有可用的预算池,继续查找上级组织的预算池
                }
                parentId = org.getParentId();
                if (StringUtils.isNotBlank(parentId)) {
                    org = orgMap.get(parentId);
                } else {
                    org = null;
                }
            }
        } else {
            // 按预算期间的优先级排序
            poolLevelMap = this.sortByPeriod(poolAttributes);
        }

        if (Objects.nonNull(poolLevelMap) && poolLevelMap.size() > 0) {
            BudgetResponse response = new BudgetResponse();
            response.setBizId(useBudget.getBizId());
            // 占用总金额
            double amount = useBudget.getAmount();
            // 已占用金额
            double useAmount = 0;

            ExecutionRecord record;
            Collection<PoolLevel> levelList = poolLevelMap.values();
            //
            List<PoolLevel> pools = levelList.stream().sorted(Comparator.comparingLong(PoolLevel::getLevel)).collect(Collectors.toList());
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
            return ResultData.success(response);
        } else {
            // 预算占用时,未找到满足条件[{0}]的预算池!
            return ResultData.fail(ContextUtil.getMessage("pool_00009", "强控策略"));
        }
    }
}
