package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.KeyValueDto;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.dto.ProjectDto;
import com.changhong.bems.entity.Period;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.service.client.CorporationProjectManager;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-08 13:07
 */
@Service
public class DimensionComponentService {
    /**
     * 预算主体科目服务对象
     */
    @Autowired
    private PeriodService periodService;
    /**
     * 预算主体科目服务对象
     */
    @Autowired
    private SubjectItemService subjectItemService;
    /**
     * 预算主体服务对象
     */
    @Autowired
    private SubjectService subjectService;
    /**
     * 公司项目服务对象
     */
    @Autowired
    private CorporationProjectManager projectManager;

    /**
     * 获取指定预算主体的科目(维度组件专用)
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    public List<SubjectItem> getBudgetItems(String subjectId) {
        return subjectItemService.findBySubjectUnfrozen(subjectId);
    }

    /**
     * 按预算主体和期间类型获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    public List<Period> getPeriods(String subjectId, PeriodType type) {
        return periodService.findBySubjectUnclosed(subjectId, type);
    }

    /**
     * 按预算主体获取组织机构
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    public ResultData<List<OrganizationDto>> getOrgTree(String subjectId) {
        return subjectService.getOrgTree(subjectId);
    }

    /**
     * 按预算主体获取公司项目
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
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
    public ResultData<Map<String, Object>> getDimensionValues(String subjectId, String dimCode) {
        Subject subject = subjectService.getSubject(subjectId);
        if (Objects.isNull(subject)) {
            return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
        }
        List<KeyValueDto> list;
        Map<String, Object> data = new HashMap<>();
        switch (dimCode) {
            case Constants.DIMENSION_CODE_PERIOD:
                data.put("head", Lists.newArrayList("ID", "名称"));
                List<Period> periods = periodService.findBySubjectUnclosed(subjectId);
                list = periods.stream().map(p -> new KeyValueDto(p.getId(), p.getName())).collect(Collectors.toList());
                break;
            case Constants.DIMENSION_CODE_ITEM:
                data.put("head", Lists.newArrayList("代码", "名称"));
                List<SubjectItem> subjectItems = subjectItemService.findBySubjectUnfrozen(subjectId);
                list = subjectItems.stream().map(p -> new KeyValueDto(p.getCode(), p.getName())).collect(Collectors.toList());
                break;
            case Constants.DIMENSION_CODE_ORG:
                data.put("head", Lists.newArrayList("ID", "名称"));
                ResultData<List<OrganizationDto>> resultData = subjectService.getOrgChildren(subjectId);
                if (resultData.successful()) {
                    List<OrganizationDto> orgList = resultData.getData();
                    list = orgList.stream().map(o -> new KeyValueDto(o.getId(), o.getNamePath())).collect(Collectors.toList());
                } else {
                    return ResultData.fail(resultData.getMessage());
                }
                break;
            case Constants.DIMENSION_CODE_PROJECT:
                data.put("head", Lists.newArrayList("项目代码", "项目名称"));
                ResultData<List<ProjectDto>> listResultData = projectManager.findByErpCode(subject.getCorporationCode());
                if (listResultData.successful()) {
                    List<ProjectDto> projectList = listResultData.getData();
                    list = projectList.stream().map(o -> new KeyValueDto(o.getCode(), o.getName())).collect(Collectors.toList());
                } else {
                    return ResultData.fail(listResultData.getMessage());
                }
                break;
            case Constants.DIMENSION_CODE_COST_CENTER:
                data.put("head", Lists.newArrayList("成本中心", "成本中心名称"));
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
}
