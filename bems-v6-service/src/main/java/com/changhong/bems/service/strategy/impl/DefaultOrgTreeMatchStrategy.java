package com.changhong.bems.service.strategy.impl;

import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.bems.service.strategy.OrgTreeMatchStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实现功能：组织机构树路径维度匹配策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 11:32
 */
public class DefaultOrgTreeMatchStrategy extends BaseMatchStrategy implements OrgTreeMatchStrategy {

    @Autowired
    private OrganizationManager organizationManager;

    /**
     * 获取维度匹配值
     *
     * @param dimValue 维度值
     * @return 返回匹配值
     */
    @Override
    public ResultData<Object> getMatchValue(BudgetUse budgetUse, String dimValue) {
        ResultData<List<OrganizationDto>> resultData = organizationManager.getParentNodes(dimValue, Boolean.TRUE);
        if (resultData.successful()) {
            List<OrganizationDto> orgList = resultData.getData();
            if (CollectionUtils.isNotEmpty(orgList)) {
                Set<String> orgIds = orgList.stream().map(OrganizationDto::getId).collect(Collectors.toSet());
                return ResultData.success(orgIds);
            } else {
                // 预算占用时,通过组织维度[{0}]未找到上级节点
                return ResultData.fail(ContextUtil.getMessage("pool_00011", dimValue));
            }
        } else {
            return ResultData.fail(resultData.getMessage());
        }
    }
}
