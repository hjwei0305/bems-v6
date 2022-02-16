package com.changhong.bems.service.cust;

import com.changhong.bems.dto.OrganizationDto;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.dto.ProjectDto;
import com.changhong.bems.entity.Period;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.sei.core.dto.ResultData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-20 12:24
 */
public interface BudgetDimensionCustManager {

    /**
     * 获取指定预算主体的科目(维度组件专用)
     *
     * @param subjectId 预算主体id
     * @return 子实体清单
     */
    List<StrategyItem> getBudgetItems(String subjectId);

    /**
     * 按预算主体和期间类型获取期间
     *
     * @param subjectId 预算主体id
     * @param type      预算期间类型
     * @return 期间清单
     */
    List<Period> getPeriods(String subjectId, PeriodType type);

    /**
     * 按预算主体获取组织机构
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    ResultData<List<OrganizationDto>> getOrgTree(String subjectId);

    /**
     * 按预算主体获取公司项目
     *
     * @param subjectId 预算主体id
     * @return 期间清单
     */
    ResultData<List<ProjectDto>> getProjects(String subjectId, String searchValue, Set<String> excludeIds);

    /**
     * 获取预算维度主数据
     *
     * @param subjectId 预算主体id
     * @param dimCode   预算维度代码
     * @return 导出预算模版数据
     */
    ResultData<Map<String, Object>> getDimensionValues(String subjectId, String dimCode);

    /**
     * 按维度获取指定预算主体下的名称与值映射关系
     * 如:map.put("科目名称","科目代码")
     *
     * @param subject 预算主体
     * @param dimCode 预算维度代码
     * @return 维度名称与值映射关系
     */
    Map<String, String> getDimensionNameValueMap(Subject subject, String dimCode);
}
