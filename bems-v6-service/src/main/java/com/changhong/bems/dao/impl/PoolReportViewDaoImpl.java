package com.changhong.bems.dao.impl;

import com.changhong.bems.dao.PoolReportViewExtDao;
import com.changhong.bems.dto.report.AnnualBudgetResponse;
import com.changhong.bems.entity.PoolReportView;
import com.changhong.sei.core.dao.impl.BaseEntityDaoImpl;
import org.apache.commons.collections.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-03 18:22
 */
public class PoolReportViewDaoImpl extends BaseEntityDaoImpl<PoolReportView> implements PoolReportViewExtDao {

    public PoolReportViewDaoImpl(EntityManager entityManager) {
        super(PoolReportView.class, entityManager);
    }

    /**
     * 获取年度预算分析报表数据
     *
     * @param subjectId 预算主体
     * @param year      年度
     * @return 年度预算分析报表数据结果
     */
    @Override
    public List<AnnualBudgetResponse> annualBudgetAnalysis(String subjectId, Integer year, Set<String> itemCodes) {
        String jpql = "select new com.changhong.bems.dto.report.AnnualBudgetResponse(p.subjectId,max(p.subjectName), p.year, p.item,max(p.itemName), sum(p.injectAmount), sum(p.usedAmount)) from PoolReportView p where p.subjectId=:subjectId and p.year=:year";
        if (CollectionUtils.isNotEmpty(itemCodes)) {
            jpql += " and p.item in (:itemCodes)";
        }
        jpql += " group by p.subjectId,p.year,p.item order by p.item";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("subjectId", subjectId);
        query.setParameter("year", year);
        if (CollectionUtils.isNotEmpty(itemCodes)) {
            query.setParameter("itemCodes", itemCodes);
        }
        return query.getResultList();
    }

}
