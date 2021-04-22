package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectDao;
import com.changhong.bems.dto.CorporationDto;
import com.changhong.bems.dto.CurrencyDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.service.client.CorporationManager;
import com.changhong.bems.service.client.CurrencyManager;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 预算主体(Subject)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:25
 */
@Service
public class SubjectService extends BaseEntityService<Subject> {
    @Autowired
    private SubjectDao dao;
    @Autowired
    private CurrencyManager currencyManager;
    @Autowired
    private CorporationManager corporationManager;
    @Autowired
    private OrganizationManager organizationManager;

    @Override
    protected BaseEntityDao<Subject> getDao() {
        return dao;
    }

    /**
     * 获取币种数据
     *
     * @return 查询结果
     */
    public ResultData<List<CurrencyDto>> findCurrencies() {
        return currencyManager.findAllUnfrozen();
    }

    /**
     * 获取当前用户有权限的公司
     *
     * @return 当前用户有权限的公司
     */
    public ResultData<List<CorporationDto>> findUserAuthorizedCorporations() {
        return corporationManager.findUserAuthorizedCorporations();
    }

    /**
     * 获取组织机构树(不包含冻结)
     *
     * @return 组织机构树清单
     */
    public ResultData<List<OrganizationDto>> findOrgTree() {
        return organizationManager.findOrgTreeWithoutFrozen();
    }
}