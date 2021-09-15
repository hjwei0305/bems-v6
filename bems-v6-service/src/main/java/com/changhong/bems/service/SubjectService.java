package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectDao;
import com.changhong.bems.dto.CorporationDto;
import com.changhong.bems.dto.CurrencyDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.entity.*;
import com.changhong.bems.service.client.CorporationManager;
import com.changhong.bems.service.client.CurrencyManager;
import com.changhong.bems.service.client.OrganizationManager;
import com.changhong.sei.basic.sdk.UserAuthorizeManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.DataAuthEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


/**
 * 预算主体(Subject)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:25
 */
@Service
public class SubjectService extends BaseEntityService<Subject> implements DataAuthEntityService {
    @Autowired
    private SubjectDao dao;
    @Autowired
    private UserAuthorizeManager userAuthorizeManager;
    @Autowired
    private CurrencyManager currencyManager;
    @Autowired
    private CorporationManager corporationManager;
    @Autowired
    private OrganizationManager organizationManager;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private SubjectItemService subjectItemService;

    @Override
    protected BaseEntityDao<Subject> getDao() {
        return dao;
    }

    /**
     * 从平台基础应用获取一般用户有权限的数据实体Id清单
     * 对于数据权限对象的业务实体，需要override，使用BASIC提供的通用工具来获取
     *
     * @param entityClassName 权限对象实体类型
     * @param featureCode     功能项代码
     * @param userId          用户Id
     * @return 数据实体Id清单
     */
    @Override
    public List<String> getNormalUserAuthorizedEntitiesFromBasic(String entityClassName, String featureCode, String userId) {
        return userAuthorizeManager.getNormalUserAuthorizedEntities(entityClassName, featureCode, userId);
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

    /**
     * 通过预算主体获取组织机构树(不包含冻结)
     *
     * @return 组织机构树
     */
    public ResultData<OrganizationDto> getOrgTree(String subjectId) {
        Subject subject = dao.findOne(subjectId);
        if (Objects.isNull(subject)) {
            // 未找到预算主体
            return ResultData.fail(ContextUtil.getMessage("subject_item_00004", subjectId));
        }
        // 控制组织的范围只能是预算主体指定的组织及下级
        return organizationManager.getTree4Unfrozen(subject.getOrgId());
    }

    /**
     * 通过预算主体获取组织机构清单(不包含冻结)
     *
     * @return 组织机构子节点清单
     */
    public ResultData<List<OrganizationDto>> getOrgChildren(String subjectId) {
        Subject subject = dao.findOne(subjectId);
        if (Objects.isNull(subject)) {
            // 未找到预算主体
            return ResultData.fail(ContextUtil.getMessage("subject_item_00004", subjectId));
        }
        // 通过组织机构id获取组织机构清单
        return organizationManager.getChildrenNodes4Unfrozen(subject.getOrgId());
    }

    /**
     * 创建数据保存数据之前额外操作回调方法 默认为空逻辑，子类根据需要覆写添加逻辑即可
     *
     * @param entity 待创建数据对象
     */
    @Override
    protected OperateResultWithData<Subject> preInsert(Subject entity) {
        OperateResultWithData<Subject> result = super.preInsert(entity);
        if (result.successful()) {
            Subject existed = dao.findByProperty(Subject.FIELD_NAME, entity.getName());
            if (Objects.nonNull(existed)) {
                // 已存在预算主体
                return OperateResultWithData.operationFailure("subject_00005", existed.getName());
            }
        }
        return result;
    }

    /**
     * 更新数据保存数据之前额外操作回调方法 默认为空逻辑，子类根据需要覆写添加逻辑即可
     *
     * @param entity 待更新数据对象
     */
    @Override
    protected OperateResultWithData<Subject> preUpdate(Subject entity) {
        OperateResultWithData<Subject> result = super.preUpdate(entity);
        if (result.successful()) {
            Subject existed = dao.findByProperty(Subject.FIELD_NAME, entity.getName());
            if (Objects.nonNull(existed) && !StringUtils.equals(entity.getId(), existed.getId())) {
                // 已存在预算主体
                return OperateResultWithData.operationFailure("subject_00005", existed.getName());
            }
        }
        return result;
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        SubjectItem item = subjectItemService.findFirstByProperty(SubjectItem.FIELD_SUBJECT_ID, id);
        if (Objects.nonNull(item)) {
            // 已被预算科目[{0}]使用,禁止删除!
            return OperateResult.operationFailure("subject_00004", item.getName());
        }
        Period period = periodService.findFirstByProperty(Period.FIELD_SUBJECT_ID, id);
        if (Objects.nonNull(period)) {
            // 已被预算期间[{0}]使用,禁止删除!
            return OperateResult.operationFailure("subject_00002", period.getName());
        }
        Category category = categoryService.findFirstByProperty(Category.FIELD_SUBJECT_ID, id);
        if (Objects.nonNull(category)) {
            // 已被预算类型[{0}]使用,禁止删除!
            return OperateResult.operationFailure("subject_00001", category.getName());
        }
        return OperateResult.operationSuccess();
    }

    /**
     * 通过公司代码获取预算主体
     *
     * @param corpCode 公司代码
     * @return 返回预算主体清单
     */
    public List<Subject> getByCorpCode(String corpCode) {
        return dao.findListByProperty(Subject.FIELD_CORP_CODE, corpCode);
    }
}