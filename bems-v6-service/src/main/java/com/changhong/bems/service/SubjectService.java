package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.SubjectDao;
import com.changhong.bems.dao.SubjectOrganizationDao;
import com.changhong.bems.dto.Classification;
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
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.DataAuthEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import com.changhong.sei.util.IdGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private SubjectOrganizationDao subjectOrganizationDao;
    @Autowired
    private UserAuthorizeManager userAuthorizeManager;
    @Autowired
    private CurrencyManager currencyManager;
    @Autowired
    private CorporationManager corporationManager;
    @Autowired(required = false)
    private OrganizationManager organizationManager;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private SubjectItemService subjectItemService;
    @Autowired
    private StrategyService strategyService;

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
     * 按公司代码获取组织机构树(不包含冻结)
     *
     * @param corpCode 公司代码
     * @return 组织机构树清单
     */
    public ResultData<OrganizationDto> findOrgTree(String corpCode) {
        ResultData<OrganizationDto> resultData;
        if (Objects.nonNull(organizationManager)) {
            ResultData<CorporationDto> corpResultData = corporationManager.findByCode(corpCode);
            if (corpResultData.successful()) {
                CorporationDto corporation = corpResultData.getData();
                if (Objects.nonNull(corporation)) {
                    if (StringUtils.isNotBlank(corporation.getOrganizationId())) {
                        resultData = organizationManager.getTree4Unfrozen(corporation.getOrganizationId());
                    } else {
                        // 公司[{0}]对应的组织机构未配置，请检查！
                        resultData = ResultData.fail(ContextUtil.getMessage("subject_00013", corpCode));
                    }
                } else {
                    // 公司[{0}]不存在，请检查！
                    resultData = ResultData.fail(ContextUtil.getMessage("subject_00012", corpCode));
                }
            } else {
                resultData = ResultData.fail(corpResultData.getMessage());
            }
        } else {
            resultData = ResultData.fail(ContextUtil.getMessage("pool_00030"));
        }
        return resultData;
    }

    /**
     * 通过预算主体获取组织机构树(不包含冻结)
     *
     * @return 组织机构树
     */
    public ResultData<List<OrganizationDto>> getOrgTree(String subjectId) {
        ResultData<List<OrganizationDto>> resultData = this.getOrgChildren(subjectId);
        if (resultData.successful()) {
            // 构造成树
            return ResultData.success(buildTree(resultData.getData()));
        } else {
            return ResultData.fail(resultData.getMessage());
        }
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
            return ResultData.fail(ContextUtil.getMessage("subject_00003"));
        }
        if (Objects.nonNull(organizationManager)) {
            // 通过组织机构id获取组织机构清单
            ResultData<List<OrganizationDto>> resultData;
            if (Objects.equals(Classification.DEPARTMENT, subject.getClassification())) {
                // 获取分配的组织机构清单
                List<SubjectOrganization> subjectOrganizations = this.getSubjectOrganizations(subjectId);
                if (CollectionUtils.isNotEmpty(subjectOrganizations)) {
                    Set<String> nodeIds = subjectOrganizations.stream().map(SubjectOrganization::getOrgId).collect(Collectors.toSet());
                    resultData = organizationManager.getChildrenNodes4UnfrozenByIds(nodeIds);
                } else {
                    // 预算主体[{0}]未维护适用组织范围!
                    resultData = ResultData.fail(ContextUtil.getMessage("subject_00007", subject.getName()));
                }
            } else if (Objects.equals(Classification.PROJECT, subject.getClassification())) {
                ResultData<CorporationDto> corpResultData = corporationManager.findByCode(subject.getCorporationCode());
                if (corpResultData.successful()) {
                    CorporationDto corporation = corpResultData.getData();
                    if (Objects.nonNull(corporation)) {
                        if (StringUtils.isNotBlank(corporation.getOrganizationId())) {
                            resultData = organizationManager.getChildrenNodes4Unfrozen(corporation.getOrganizationId());
                        } else {
                            // 预算主体[{0}]配置的公司[{0}]对应的组织机构未配置，请检查！
                            resultData = ResultData.fail(ContextUtil.getMessage("subject_00009", subject.getName(), subject.getCorporationCode()));
                        }
                    } else {
                        // 预算主体[{0}]配置的公司[{0}]不存在，请检查！
                        resultData = ResultData.fail(ContextUtil.getMessage("subject_00008", subject.getName(), subject.getCorporationCode()));
                    }
                } else {
                    resultData = ResultData.fail(corpResultData.getMessage());
                }
            } else if (Objects.equals(Classification.COST_CENTER, subject.getClassification())) {
                // TODO 成本中心接口
                resultData = ResultData.fail(ContextUtil.getMessage("开发中"));
            } else {
                // 不支持的预算分类
                resultData = ResultData.fail(ContextUtil.getMessage("subject_00010"));
            }
            return resultData;
        } else {
            return ResultData.fail(ContextUtil.getMessage("pool_00030"));
        }
    }

    /**
     * 按组织级主体id获取分配的组织机构
     *
     * @param subjectId 组织级主体id
     * @return 分配的组织机构清单
     */
    public List<SubjectOrganization> getSubjectOrganizations(String subjectId) {
        return subjectOrganizationDao.findListByProperty(SubjectOrganization.FIELD_SUBJECT_ID, subjectId);
    }

    /**
     * 更新一个预算主体冻结状态
     *
     * @param id id
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> updateFrozen(String id, boolean state) {
        Subject subject = dao.findOne(id);
        if (Objects.isNull(subject)) {
            // 业务领域不存在.
            return ResultData.fail(ContextUtil.getMessage("subject_00003", id));
        }
        subject.setFrozen(state);
        dao.save(subject);
        return ResultData.success();
    }

    /**
     * 数据保存操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Subject> save(Subject entity) {
        if (StringUtils.isBlank(entity.getCode())) {
            entity.setCode(String.valueOf(IdGenerator.nextId()));
        }

        if (Objects.equals(Classification.PROJECT, entity.getClassification())) {
            // 检查同一公司下有且只有一个项目级主体
            Search search = Search.createSearch();
            search.addFilter(new SearchFilter(Subject.FIELD_CORP_CODE, entity.getCorporationCode()));
            search.addFilter(new SearchFilter(Subject.FIELD_CLASSIFICATION, Classification.PROJECT));
            Subject existed = dao.findFirstByFilters(search);
            if (Objects.nonNull(existed) && !StringUtils.equals(entity.getId(), existed.getId())) {
                // 公司[{0}]下已存在一个项目级主体[{1}].
                return OperateResultWithData.operationFailure("subject_00011", entity.getCorporationCode(), existed.getName());
            }
        } else if (Objects.equals(Classification.DEPARTMENT, entity.getClassification())
                && CollectionUtils.isEmpty(entity.getOrgList())) {
            // 组织级预算主体需维护适用组织范围
            return OperateResultWithData.operationFailure("subject_00006");
        }
        Subject existed = dao.findByProperty(Subject.FIELD_NAME, entity.getName());
        if (Objects.nonNull(existed) && !StringUtils.equals(entity.getId(), existed.getId())) {
            // 已存在预算主体
            return OperateResultWithData.operationFailure("subject_00005", existed.getName());
        }

        if (StringUtils.isNotBlank(entity.getId())) {
            // 清除策略缓存
            strategyService.cleanStrategyCache(entity.getId(), null);
        }
        // 持久化
        entity = dao.save(entity);

        // 保存组织级主体关联的组织机构
        if (Objects.equals(Classification.DEPARTMENT, entity.getClassification())) {
            // 获取分配的组织机构清单
            List<SubjectOrganization> orgList = this.getSubjectOrganizations(entity.getId());
            if (CollectionUtils.isNotEmpty(orgList)) {
                subjectOrganizationDao.deleteAll(orgList);
            }
            SubjectOrganization org;
            orgList = new ArrayList<>();
            for (OrganizationDto orgDto : entity.getOrgList()) {
                org = new SubjectOrganization();
                org.setSubjectId(entity.getId());
                org.setOrgId(orgDto.getId());
                org.setOrgName(orgDto.getName());
                org.setTenantCode(entity.getTenantCode());
                orgList.add(org);
            }
            subjectOrganizationDao.save(orgList);
        }
        return OperateResultWithData.operationSuccessWithData(entity);
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
     * 基于主键查询单一数据对象
     *
     * @param id 主体id
     */
    @Override
    public Subject findOne(String id) {
        Subject subject = dao.findOne(id);
        if (Objects.nonNull(subject)) {
            if (Objects.equals(Classification.DEPARTMENT, subject.getClassification())) {
                List<SubjectOrganization> list = this.getSubjectOrganizations(id);
                if (CollectionUtils.isNotEmpty(list)) {
                    OrganizationDto org;
                    Set<OrganizationDto> orgSet = new HashSet<>(list.size());
                    for (SubjectOrganization so : list) {
                        org = new OrganizationDto();
                        org.setId(so.getOrgId());
                        org.setName(so.getOrgName());
                        orgSet.add(org);
                    }
                    subject.setOrgList(orgSet);
                }
            }
        }
        return subject;
    }

    /**
     * 通过公司代码获取预算主体
     * 如果公司存在多个预算主体,则还需要通过组织确定
     * 如果组织为空,则默认返回第一个
     * 如果组织不为空,则按组织树路径向上匹配预算主体上配置的组织
     *
     * @param corpCode 公司代码
     * @return 返回预算主体清单
     */
    public Subject getSubject(String corpCode, String orgId) {
        Subject subject = null;
        List<Subject> subjectList = dao.findListByProperty(Subject.FIELD_CORP_CODE, corpCode);
        if (CollectionUtils.isNotEmpty(subjectList)) {
            if (subjectList.size() == 1) {
                subject = subjectList.get(0);
            } else {
                if (StringUtils.isBlank(orgId) || StringUtils.equalsIgnoreCase(Constants.NONE, orgId)) {
                    subject = subjectList.get(0);
                } else {
                    // 按id进行映射方便后续使用
                    Map<String, OrganizationDto> orgMap = null;
                    // 获取指定节点的所有父节点(含自己)
                    ResultData<List<OrganizationDto>> resultData = organizationManager.getParentNodes(orgId, Boolean.TRUE);
                    if (resultData.successful()) {
                        List<OrganizationDto> orgList = resultData.getData();
                        if (CollectionUtils.isNotEmpty(orgList)) {
                            // 组织id映射
                            orgMap = orgList.stream().collect(Collectors.toMap(OrganizationDto::getId, o -> o));
                            orgList.clear();
                        }
                    }
                    if (Objects.nonNull(orgMap)) {
                    /*
                        组织机构向上查找规则:
                        1.按组织机构树路径,从预算占用的节点开始,向上依次查找
                        2.当按组织节点找到存在的预算池,不管余额是否满足,都将停止向上查找
                     */
                        String parentId = orgId;
                        OrganizationDto org = orgMap.get(parentId);
                        while (Objects.nonNull(org)) {
                            String oId = org.getId();
                            // 按组织id匹配预算池
                            /* TODO 改名后修改
                            subject = subjectList.stream().filter(p -> StringUtils.equals(oId, p.getOrgId())).findFirst().orElse(null);
                            if (Objects.nonNull(subject)) {
                                break;
                            } else {
                                // 没有可用的预算池,继续查找上级组织的预算池
                                parentId = org.getParentId();
                                if (StringUtils.isNotBlank(parentId)) {
                                    org = orgMap.get(parentId);
                                } else {
                                    org = null;
                                }
                            }*/
                        }
                    }
                }
            }
        }
        return subject;
    }


    /**
     * 通过节点清单构建树
     *
     * @param nodes 节点清单
     * @return 树
     */
    private List<OrganizationDto> buildTree(List<OrganizationDto> nodes) {
        List<OrganizationDto> result = new ArrayList<>();
        if (nodes == null || nodes.size() == 0) {
            return result;
        }
        //将输入节点排序
        List<OrganizationDto> sordedNodes = nodes.stream().sorted(Comparator.comparingInt(n -> n.getNodeLevel() + n.getRank())).collect(Collectors.toList());
        //获取清单中的顶级节点
        for (OrganizationDto node : sordedNodes) {
            String parentId = node.getParentId();
            OrganizationDto parent = sordedNodes.stream().filter((n) -> StringUtils.equals(n.getId(), parentId)
                    && !StringUtils.equals(n.getId(), node.getId())).findAny().orElse(null);
            if (parent == null) {
                //递归构造子节点
                findChildren(node, sordedNodes);
                result.add(node);
            }
        }
        return result.stream().sorted(Comparator.comparingInt(OrganizationDto::getRank)).collect(Collectors.toList());
    }


    /**
     * 递归查找子节点并设置子节点
     *
     * @param treeNode 树形节点（顶级节点）
     * @param nodes    节点清单
     * @return 树形节点
     */
    private OrganizationDto findChildren(OrganizationDto treeNode, List<OrganizationDto> nodes) {
        for (OrganizationDto node : nodes) {
            if (treeNode.getId().equals(node.getParentId())) {
                if (treeNode.getChildren() == null) {
                    treeNode.setChildren(new ArrayList<>());
                }
                treeNode.getChildren().add(findChildren(node, nodes));
            }
        }
        return treeNode;
    }
}