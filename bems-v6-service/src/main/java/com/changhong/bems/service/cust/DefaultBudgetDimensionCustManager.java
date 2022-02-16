package com.changhong.bems.service.cust;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.KeyValueDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.dto.ProjectDto;
import com.changhong.bems.entity.Period;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.bems.service.PeriodService;
import com.changhong.bems.service.StrategyItemService;
import com.changhong.bems.service.SubjectService;
import com.changhong.bems.service.client.CorporationProjectManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-20 12:25
 */
public class DefaultBudgetDimensionCustManager implements BudgetDimensionCustManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBudgetDimensionCustManager.class);

    /**
     * 预算主体服务对象
     */
    @Autowired
    private SubjectService subjectService;
    /**
     * 预算主体科目服务对象
     */
    @Autowired
    private PeriodService periodService;
    /**
     * 预算主体科目服务对象
     */
    @Autowired
    private StrategyItemService subjectItemService;
    /**
     * 公司项目服务对象
     */
    @Autowired
    private CorporationProjectManager projectManager;

    private final RedisTemplate<String, Object> redisTemplate;

    public DefaultBudgetDimensionCustManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取指定预算主体的科目(维度组件专用)
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    @Override
    public List<StrategyItem> getBudgetItems(String subjectId) {
        return subjectItemService.findBySubjectUnfrozen(subjectId);
    }

    /**
     * 按预算主体和期间类型获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    @Override
    public List<Period> getPeriods(String subjectId, PeriodType type) {
        return periodService.findBySubjectUnclosed(subjectId, type);
    }

    /**
     * 按预算主体获取组织机构
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    @Override
    public ResultData<List<OrganizationDto>> getOrgTree(String subjectId) {
        return subjectService.getOrgTree(subjectId);
    }

    /**
     * 按预算主体获取公司项目
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    @Override
    public ResultData<List<ProjectDto>> getProjects(String subjectId, String searchValue, Set<String> excludeIds) {
        Subject subject = subjectService.getSubject(subjectId);
        if (Objects.isNull(subject)) {
            return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
        }
        return projectManager.findByPage(subject.getCorporationCode(), searchValue, excludeIds);
    }

    /**
     * 获取预算维度主数据
     *
     * @param subjectId 预算主体id
     * @param dimCode   预算维度代码
     * @return 导出预算模版数据
     */
    @Override
    public ResultData<Map<String, Object>> getDimensionValues(String subjectId, String dimCode) {
        Subject subject = subjectService.getSubject(subjectId);
        if (Objects.isNull(subject)) {
            return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
        }
        List<KeyValueDto> list;
        Map<String, Object> data = new HashMap<>();
        switch (dimCode) {
            case Constants.DIMENSION_CODE_PERIOD:
                data.put("head", Lists.newArrayList("预算期间"));
                List<Period> periods = periodService.findBySubjectUnclosed(subjectId);
                list = periods.stream().map(p -> new KeyValueDto(p.getName(), "")).collect(Collectors.toList());
                break;
            case Constants.DIMENSION_CODE_ITEM:
                data.put("head", Lists.newArrayList("预算科目"));
                List<StrategyItem> subjectItems = subjectItemService.findBySubjectUnfrozen(subjectId);
                list = subjectItems.stream().map(p -> new KeyValueDto(p.getName(), "")).collect(Collectors.toList());
                break;
            case Constants.DIMENSION_CODE_ORG:
                data.put("head", Lists.newArrayList("组织机构", "组织路径"));
                ResultData<List<OrganizationDto>> resultData = subjectService.getOrgChildren(subjectId);
                if (resultData.successful()) {
                    List<OrganizationDto> orgList = resultData.getData();
                    list = orgList.stream().map(o -> new KeyValueDto(o.getName(), o.getNamePath())).collect(Collectors.toList());
                } else {
                    return ResultData.fail(resultData.getMessage());
                }
                break;
            case Constants.DIMENSION_CODE_PROJECT:
                data.put("head", Lists.newArrayList("公司项目"));
                ResultData<List<ProjectDto>> listResultData = projectManager.findByErpCode(subject.getCorporationCode());
                if (listResultData.successful()) {
                    List<ProjectDto> projectList = listResultData.getData();
                    list = projectList.stream().map(o -> new KeyValueDto(o.getName(), "")).collect(Collectors.toList());
                } else {
                    return ResultData.fail(listResultData.getMessage());
                }
                break;
            case Constants.DIMENSION_CODE_COST_CENTER:
                data.put("head", Lists.newArrayList("成本中心"));
                // TODO 提供成本中心接口
                return ResultData.fail(ContextUtil.getMessage("dimension_00006"));

            // break;
            case Constants.DIMENSION_CODE_UDF1:
                // TODO 提供二开接口
                return ResultData.fail(ContextUtil.getMessage("dimension_00006"));
            case Constants.DIMENSION_CODE_UDF2:
                return ResultData.fail(ContextUtil.getMessage("dimension_00006"));
            case Constants.DIMENSION_CODE_UDF3:
                return ResultData.fail(ContextUtil.getMessage("dimension_00006"));
            case Constants.DIMENSION_CODE_UDF4:
                return ResultData.fail(ContextUtil.getMessage("dimension_00006"));
            case Constants.DIMENSION_CODE_UDF5:
                return ResultData.fail(ContextUtil.getMessage("dimension_00006"));
            default:
                // 不支持的预算维度
                return ResultData.fail(ContextUtil.getMessage("dimension_00005", dimCode));
        }
        data.put("data", list);
        return ResultData.success(data);
    }

    /**
     * 按维度获取指定预算主体下的名称与值映射关系
     * 如:map.put("科目名称","科目代码")
     *
     * @param subject 预算主体
     * @param dimCode 预算维度代码
     * @return 维度名称与值映射关系
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public Map<String, String> getDimensionNameValueMap(Subject subject, String dimCode) {
        String subjectId = subject.getId();
        // BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.DIMENSION_MAP_CACHE_KEY_PREFIX.concat(dimCode).concat(":").concat(subjectId));
        // Map<String, String> data = (Map<String, String>) operations.get();
        // if (Objects.nonNull(data) && !data.isEmpty()) {
        //     return data;
        // }

        Map<String, String> data = new HashMap<>();
        switch (dimCode) {
            case Constants.DIMENSION_CODE_PERIOD:
                List<Period> periods = periodService.findBySubjectUnclosed(subjectId);
                if (CollectionUtils.isNotEmpty(periods)) {
                    data = periods.parallelStream().collect(Collectors.toMap(Period::getName, Period::getId));
                }
                break;
            case Constants.DIMENSION_CODE_ITEM:
                List<StrategyItem> subjectItems = subjectItemService.findBySubjectUnfrozen(subjectId);
                if (CollectionUtils.isNotEmpty(subjectItems)) {
                    data = subjectItems.parallelStream().collect(Collectors.toMap(StrategyItem::getName, StrategyItem::getCode));
                }
                break;
            case Constants.DIMENSION_CODE_ORG:
                ResultData<List<OrganizationDto>> resultData = subjectService.getOrgChildren(subjectId);
                if (resultData.successful()) {
                    List<OrganizationDto> orgList = resultData.getData();
                    data = orgList.parallelStream().collect(Collectors.toMap(OrganizationDto::getName, OrganizationDto::getId));
                }
                break;
            case Constants.DIMENSION_CODE_PROJECT:
                ResultData<List<ProjectDto>> listResultData = projectManager.findByErpCode(subject.getCorporationCode());
                if (listResultData.successful()) {
                    List<ProjectDto> projectList = listResultData.getData();
                    data = projectList.stream().collect(Collectors.toMap(proj -> proj.getName() + "(" + proj.getCode() + ")", ProjectDto::getId));
                }
                break;
            case Constants.DIMENSION_CODE_COST_CENTER:
                // TODO 提供成本中心接口

                break;
            case Constants.DIMENSION_CODE_UDF1:
                // TODO 提供二开接口
            case Constants.DIMENSION_CODE_UDF2:
            case Constants.DIMENSION_CODE_UDF3:
            case Constants.DIMENSION_CODE_UDF4:
            case Constants.DIMENSION_CODE_UDF5:
                LOG.error(ContextUtil.getMessage("dimension_00006"));
                break;
            default:
                // 不支持的预算维度
                LOG.error(ContextUtil.getMessage("dimension_00005", dimCode));
        }
        // if (!data.isEmpty()) {
        //     operations.set(data, 10, TimeUnit.HOURS);
        // }
        return data;
    }
}
