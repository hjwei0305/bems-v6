package com.changhong.bems.service.client;

import com.changhong.bems.dto.OrganizationDto;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-22 09:38
 */
@Component
public class OrganizationClientFallback implements OrganizationClient {
    /**
     * 通过代码获取组织机构
     *
     * @param code 代码
     * @return 组织机构
     */
    @Override
    public ResultData<OrganizationDto> findByCode(String code) {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_003"));
    }

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    @Override
    public ResultData<List<OrganizationDto>> findOrgTreeWithoutFrozen() {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_003"));
    }

    /**
     * 根据指定的节点id获取树
     *
     * @param nodeId 节点ID
     * @return 返回已指定节点ID为根的树
     */
    @Override
    public ResultData<OrganizationDto> getTree4Unfrozen(String nodeId) {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_003"));
    }

    /**
     * 通过组织机构id获取组织机构清单
     *
     * @param nodeId 组织机构id
     * @return 组织机构清单（非树形）
     */
    @Override
    public ResultData<List<OrganizationDto>> getChildrenNodes4Unfrozen(String nodeId) {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_003"));
    }

    /**
     * 通过组织机构id清单获取下级组织机构清单
     *
     * @param orgIds 组织机构id清单
     * @return 组织机构清单（非树形）
     */
    @Override
    public ResultData<List<OrganizationDto>> getChildrenNodes4UnfrozenByIds(Set<String> orgIds) {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_003"));
    }

    /**
     * 获取一个节点的所有父节点
     *
     * @param nodeId      节点Id
     * @param includeSelf 是否包含本节点
     * @return 父节点清单
     */
    @Override
    public ResultData<List<OrganizationDto>> getParentNodes(String nodeId, boolean includeSelf) {
        // 币种接口访问异常
        return ResultData.fail(ContextUtil.getMessage("external_003"));
    }
}
