package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.entity.PoolAttributeView;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.bems.service.vo.PoolLevel;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 实现功能：执行控制策略基类
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-23 18:29
 */
public abstract class BaseExecutionStrategy {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseExecutionStrategy.class);


    public abstract OrganizationManager getOrgManager();

    /**
     * @param attribute
     * @param useBudget
     * @param poolAttributes
     * @return
     */
    protected ResultData<Map<String, PoolLevel>> sortPools(String attribute, BudgetUse useBudget, List<PoolAttributeView> poolAttributes) {
        Map<String, PoolLevel> poolLevelMap = null;
        // 组织id
        String orgId = useBudget.getOrg();
        // 检查是否包含组织维度
        if (attribute.contains(Constants.DIMENSION_CODE_ORG)) {
            OrganizationManager organizationManager = getOrgManager();
            if (Objects.isNull(organizationManager)) {
                return ResultData.fail("对组织维度检查,OrganizationManager不能为空.");
            }
            // 按id进行映射方便后续使用
            Map<String, OrganizationDto> orgMap = null;
            if (Objects.nonNull(orgId)) {
                orgId = orgId.trim();
                if (StringUtils.isNotBlank(orgId) && !StringUtils.equalsIgnoreCase(Constants.NONE, orgId)) {
                    // 获取指定节点的所有父节点(含自己)
                    ResultData<List<OrganizationDto>> resultData = organizationManager.getParentNodes(orgId, Boolean.TRUE);
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
            String parentId = orgId;
            OrganizationDto org = orgMap.get(parentId);
            while (isUp && Objects.nonNull(org)) {
                String oId = org.getId();
                // 按组织id匹配预算池
                List<PoolAttributeView> pools = poolAttributes.stream().filter(p -> StringUtils.equals(oId, p.getOrg())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(pools)) {
                    // 通过组织id匹配到预算池进行排序
                    poolLevelMap = this.sortByPeriod(pools);
                    isUp = false;
                }
                // 没有可用的预算池,继续查找上级组织的预算池
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
        return ResultData.success(poolLevelMap);
    }

    /**
     * 首先按期间类型下标进行排序: 下标值越大优先级越高
     */
    private Map<String, PoolLevel> sortByPeriod(List<PoolAttributeView> pools) {
        Map<String, PoolLevel> poolLevelMap = new HashMap<>(pools.size());
        PoolLevel level;
        String poolCode;
        for (PoolAttributeView pool : pools) {
            poolCode = pool.getCode();
            level = poolLevelMap.get(poolCode);
            if (Objects.isNull(level)) {
                level = new PoolLevel(pool.getSubjectId(), poolCode, pool.getAttributeCode());
            }
            // 期间类型枚举下标
            level.setLevel(level.getLevel() + pool.getPeriodType().ordinal());
            poolLevelMap.put(poolCode, level);
        }
        return poolLevelMap;
    }
}
