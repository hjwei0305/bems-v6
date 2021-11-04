package com.changhong.bems.dao.impl;

import com.changhong.bems.dao.PoolAttributeAmountExtDao;
import com.changhong.bems.dto.report.ExecutionAnalysisRequest;
import com.changhong.bems.dto.report.ExecutionAnalysisVo;
import com.changhong.bems.dto.report.UsageTrendRequest;
import com.changhong.bems.dto.report.UsageTrendVo;
import com.changhong.bems.entity.PoolAttributeAmount;
import com.changhong.sei.core.dao.impl.BaseEntityDaoImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-19 16:01
 */
public class PoolAttributeAmountDaoImpl extends BaseEntityDaoImpl<PoolAttributeAmount> implements PoolAttributeAmountExtDao {

    public PoolAttributeAmountDaoImpl(EntityManager entityManager) {
        super(PoolAttributeAmount.class, entityManager);
    }

    /**
     * 预算分析报表数据
     *
     * @param request 预算分析报表数据查询
     * @return 预算分析报表数据结果
     */
    @Override
    public List<ExecutionAnalysisVo> executionAnalysis(ExecutionAnalysisRequest request) {
        Map<String, Object> params = new HashMap<>(7);
        StringBuilder jpql = new StringBuilder();
        jpql.append("select new com.changhong.bems.dto.report.ExecutionAnalysisVo(%s) from PoolAttributeAmount t ")
                .append("join DimensionAttribute a on t.subjectId = a.subjectId and t.attributeCode = a.attributeCode ");
        // 预算主体
        params.put("subjectId", request.getSubjectId());
        // 预算年度
        params.put("year", request.getYear());

        StringBuilder selectField = new StringBuilder("a.item,max(a.itemName)");
        StringBuilder groupByField = new StringBuilder("a.item");
        // 科目
        if (CollectionUtils.isNotEmpty(request.getItemCodes())) {
            jpql.append(" and a.item in (:itemCodes) ");
            params.put("itemCodes", request.getItemCodes());
        }

        // 组织
        if (CollectionUtils.isNotEmpty(request.getOrgIds())) {
            jpql.append(" and a.org in (:orgIds) ");
            params.put("orgIds", request.getOrgIds());
            selectField.append(",a.org,max(a.orgName)");
            groupByField.append(",a.org");
        } else {
            selectField.append(",'',''");
        }
        // 项目
        if (CollectionUtils.isNotEmpty(request.getProjectCodes())) {
            jpql.append(" and a.project in (:projectCodes) ");
            params.put("projectCodes", request.getProjectCodes());
            selectField.append(",a.project,max(a.projectName)");
            groupByField.append(",a.project");
        } else {
            selectField.append(",'',''");
        }
        // 自定义1
        if (CollectionUtils.isNotEmpty(request.getUdf1s())) {
            jpql.append(" and a.udf1 in (:udf1s) ");
            params.put("udf1s", request.getUdf1s());
            selectField.append(",a.udf1,max(a.udf1Name)");
            groupByField.append(",a.udf1");
        } else {
            selectField.append(",'',''");
        }
        // 自定义2
        if (CollectionUtils.isNotEmpty(request.getUdf2s())) {
            jpql.append(" and a.udf2 in (:udf2s) ");
            params.put("udf2s", request.getUdf2s());
            selectField.append(",a.udf2,max(a.udf2Name)");
            groupByField.append(",a.udf2");
        } else {
            selectField.append(",'',''");
        }
        // 自定义3
        if (CollectionUtils.isNotEmpty(request.getUdf3s())) {
            jpql.append(" and a.udf3 in (:udf3s) ");
            params.put("udf3s", request.getUdf3s());
            selectField.append(",a.udf3,max(a.udf3Name)");
            groupByField.append(",a.udf3");
        } else {
            selectField.append(",'',''");
        }
        // 自定义4
        if (CollectionUtils.isNotEmpty(request.getUdf4s())) {
            jpql.append(" and a.udf4 in (:udf4s) ");
            params.put("udf4s", request.getUdf4s());
            selectField.append(",a.udf4,max(a.udf4Name)");
            groupByField.append(",a.udf4");
        } else {
            selectField.append(",'',''");
        }
        // 自定义5
        if (CollectionUtils.isNotEmpty(request.getUdf5s())) {
            jpql.append(" and a.udf5 in (:udf5s) ");
            params.put("udf5s", request.getUdf5s());
            selectField.append(",a.udf5,max(a.udf5Name)");
            groupByField.append(",a.udf5");
        } else {
            selectField.append(",'',''");
        }
        selectField.append(",sum(t.initInjectAmount),sum(t.injectAmount),sum(t.usedAmount)");
        jpql.append("where t.subjectId = :subjectId and t.year = :year group by %s order by a.item ");

        String ql = String.format(jpql.toString(), selectField, groupByField);
        Query query = entityManager.createQuery(ql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.getResultList();
    }

    /**
     * 预算使用趋势报表数据
     *
     * @param request 查询
     * @return 预算使用趋势报表数据结果
     */
    @Override
    public List<UsageTrendVo> usageTrend(UsageTrendRequest request) {
        Map<String, Object> params = new HashMap<>(7);
        StringBuilder jpql = new StringBuilder();
        jpql.append("select new com.changhong.bems.dto.report.UsageTrendVo(t.year,t.month,sum(t.usedAmount)) from PoolAttributeAmount t ")
                .append("join DimensionAttribute a on t.subjectId = a.subjectId and t.attributeCode = a.attributeCode ");
        // 预算主体
        params.put("subjectId", request.getSubjectId());
        // 预算年度
        List<Integer> years = new ArrayList<>();
        Collections.addAll(years, request.getYear());
        params.put("year", years);

        StringBuilder groupByField = new StringBuilder();
        // 科目
        if (StringUtils.isNotBlank(request.getItemCode())) {
            jpql.append(" and a.item = :itemCode ");
            params.put("itemCode", request.getItemCode());
        }

        // 组织
        if (StringUtils.isNotBlank(request.getOrgId())) {
            jpql.append(" and a.org = :orgId ");
            params.put("orgId", request.getOrgId());
            groupByField.append(",a.org");
        }
        // 项目
        if (StringUtils.isNotBlank(request.getProjectCode())) {
            jpql.append(" and a.project = :projectCode ");
            params.put("projectCode", request.getProjectCode());
            groupByField.append(",a.project");
        }
        // 自定义1
        if (StringUtils.isNotBlank(request.getUdf1())) {
            jpql.append(" and a.udf1 = :udf1 ");
            params.put("udf1", request.getUdf1());
            groupByField.append(",a.udf1");
        }
        // 自定义2
        if (StringUtils.isNotBlank(request.getUdf2())) {
            jpql.append(" and a.udf2 = :udf2 ");
            params.put("udf2", request.getUdf2());
            groupByField.append(",a.udf2");
        }
        // 自定义3
        if (StringUtils.isNotBlank(request.getUdf3())) {
            jpql.append(" and a.udf3 = :udf3 ");
            params.put("udf3", request.getUdf3());
            groupByField.append(",a.udf3");
        }
        // 自定义4
        if (StringUtils.isNotBlank(request.getUdf4())) {
            jpql.append(" and a.udf4 = :udf4 ");
            params.put("udf4", request.getUdf4());
            groupByField.append(",a.udf4");
        }
        // 自定义5
        if (StringUtils.isNotBlank(request.getUdf5())) {
            jpql.append(" and a.udf5 = :udf5 ");
            params.put("udf5", request.getUdf5());
            groupByField.append(",a.udf5");
        }
        jpql.append("where t.subjectId = :subjectId and t.year in (:year) group by t.year,t.month,a.item ");
        jpql.append(groupByField);

        Query query = entityManager.createQuery(jpql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query.getResultList();
    }
}
