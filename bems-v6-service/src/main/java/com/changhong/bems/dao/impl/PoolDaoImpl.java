package com.changhong.bems.dao.impl;

import com.changhong.bems.dao.PoolExtDao;
import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.PoolQuickQueryParam;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dao.impl.BaseEntityDaoImpl;
import com.changhong.sei.core.dto.serach.PageInfo;
import com.changhong.sei.core.dto.serach.PageResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-19 16:01
 */
public class PoolDaoImpl extends BaseEntityDaoImpl<Pool> implements PoolExtDao {

    public PoolDaoImpl(EntityManager entityManager) {
        super(Pool.class, entityManager);
    }
/*
select `p`.`id`                                                                                        AS `id`,
       `p`.`code`                                                                                      AS `code`,
       `p`.`subject_id`                                                                                AS `subject_id`,
       `p`.`attribute_code`                                                                            AS `attribute_code`,
       `p`.`currency_code`                                                                             AS `currency_code`,
       `p`.`currency_name`                                                                             AS `currency_name`,
       `p`.`manage_org`                                                                                AS `manage_org`,
       `p`.`manage_org_name`                                                                           AS `manage_org_name`,
       `p`.`period_category`                                                                           AS `period_category`,
       `p`.`year`                                                                                      AS `year`,
       `p`.`start_date`                                                                                AS `start_date`,
       `p`.`end_date`                                                                                  AS `end_date`,
       `p`.`is_actived`                                                                                AS `is_actived`,
       `p`.`is_delay`                                                                                  AS `is_delay`,
       `p`.`total_amount`                                                                              AS `total_amount`,
       `p`.`used_amount`                                                                               AS `used_amount`,
       `p`.`balance`                                                                                   AS `balance`,
       `p`.`tenant_code`                                                                               AS `tenant_code`,
       `p`.`created_date`                                                                              AS `created_date`,
       `a`.`attribute`                                                                                 AS `attribute`,
       `a`.`period_id`                                                                                 AS `period_id`,
       `a`.`period_name`                                                                               AS `period_name`,
       `a`.`item_code`                                                                                 AS `item_code`,
       `a`.`item_name`                                                                                 AS `item_name`,
       `a`.`org`                                                                                       AS `org`,
       `a`.`org_name`                                                                                  AS `org_name`,
       `a`.`project`                                                                                   AS `project`,
       `a`.`project_name`                                                                              AS `project_name`,
       `a`.`udf1`                                                                                      AS `udf1`,
       `a`.`udf1_name`                                                                                 AS `udf1_name`,
       `a`.`udf2`                                                                                      AS `udf2`,
       `a`.`udf2_name`                                                                                 AS `udf2_name`,
       `a`.`udf3`                                                                                      AS `udf3`,
       `a`.`udf3_name`                                                                                 AS `udf3_name`,
       `a`.`udf4`                                                                                      AS `udf4`,
       `a`.`udf4_name`                                                                                 AS `udf4_name`,
       `a`.`udf5`                                                                                      AS `udf5`,
       `a`.`udf5_name`                                                                                 AS `udf5_name`
# select count(*)
from `bems_v6`.`pool` `p` left join `bems_v6`.`dimension_attribute` `a` on a.subject_id='70DCA496-D2F9-11EB-8BB5-0242C0A84425' and
        `p`.`subject_id` = `a`.`subject_id` and `p`.`attribute_code` = `a`.`attribute_code` and
        `p`.`tenant_code` = `a`.`tenant_code`
where p.subject_id='70DCA496-D2F9-11EB-8BB5-0242C0A84425' and p.year='2021'
order by p.code
         */

    /**
     * 分页查询预算池
     *
     * @param queryParam 查询对象
     * @return 分页结果
     */
    @Override
    public PageResult<PoolAttributeDto> queryPoolPaging(PoolQuickQueryParam queryParam) {
        Map<String, Object> paramMap = new HashMap<>(7);
        paramMap.put("subjectId", queryParam.getSubjectId());
        paramMap.put("year", queryParam.getYear());

        StringBuilder fromAndWhere = new StringBuilder(128);
        fromAndWhere.append("from Pool p join DimensionAttribute a on ");
        fromAndWhere.append("a.subjectId = :subjectId ");
        fromAndWhere.append("and p.subjectId = a.subjectId and p.attributeCode = a.attributeCode and p.tenantCode = a.tenantCode ");
        // 预算科目代码清单
        if (CollectionUtils.isNotEmpty(queryParam.getItemCodes())) {
            fromAndWhere.append("and a.item in (:itemCodes) ");
            paramMap.put("itemCodes", queryParam.getItemCodes());
        }
        // 预算期间id清单
        if (CollectionUtils.isNotEmpty(queryParam.getPeriodIds())) {
            fromAndWhere.append("and a.period in (:periodIds) ");
            paramMap.put("periodIds", queryParam.getPeriodIds());
        }
        // 预算组织id清单
        if (CollectionUtils.isNotEmpty(queryParam.getOrgIds())) {
            fromAndWhere.append("and a.org in (:orgIds) ");
            paramMap.put("orgIds", queryParam.getOrgIds());
        }
        // 预算项目编码清单
        if (CollectionUtils.isNotEmpty(queryParam.getProjectIds())) {
            fromAndWhere.append("and a.project in (:projectIds) ");
            paramMap.put("projectIds", queryParam.getProjectIds());
        }
        // 自定义1清单
        if (CollectionUtils.isNotEmpty(queryParam.getUdf1s())) {
            fromAndWhere.append("and a.udf1 in (:udf1s) ");
            paramMap.put("udf1s", queryParam.getUdf1s());
        }
        // 自定义2清单
        if (CollectionUtils.isNotEmpty(queryParam.getUdf2s())) {
            fromAndWhere.append("and a.udf2 in (:udf2s) ");
            paramMap.put("udf2s", queryParam.getUdf2s());
        }
        // 自定义3清单
        if (CollectionUtils.isNotEmpty(queryParam.getUdf3s())) {
            fromAndWhere.append("and a.udf3 in (:udf3s) ");
            paramMap.put("udf3s", queryParam.getUdf3s());
        }
        // 自定义4清单
        if (CollectionUtils.isNotEmpty(queryParam.getUdf4s())) {
            fromAndWhere.append("and a.udf4 in (:udf4s) ");
            paramMap.put("udf4s", queryParam.getUdf4s());
        }
        // 自定义5清单
        if (CollectionUtils.isNotEmpty(queryParam.getUdf5s())) {
            fromAndWhere.append("and a.udf5 in (:udf5s) ");
            paramMap.put("udf5s", queryParam.getUdf5s());
        }
        fromAndWhere.append("where p.subjectId = :subjectId and p.year = :year ");
        if (Objects.nonNull(queryParam.getPeriodType())) {
            fromAndWhere.append("and p.periodType = :periodType ");
            paramMap.put("periodType", queryParam.getPeriodType());
        }

        // 快速查询关键字
        if (!StringUtils.isBlank(queryParam.getQuickSearchValue())) {
            fromAndWhere.append("and (p.code like :quickSearchValue ")
                    .append("or a.item like :quickSearchValue ")
                    .append("or a.itemName like :quickSearchValue ")
                    .append("or a.periodName like :quickSearchValue ")
                    .append("or a.orgName like :quickSearchValue ")
                    .append("or a.project like :quickSearchValue ")
                    .append("or a.projectName like :quickSearchValue ")
                    .append("or a.udf1Name like :quickSearchValue ")
                    .append("or a.udf2Name like :quickSearchValue ")
                    .append("or a.udf3Name like :quickSearchValue ")
                    .append("or a.udf4Name like :quickSearchValue ")
                    .append("or a.udf5Name like :quickSearchValue ")
                    .append(") ");
            paramMap.put("quickSearchValue", "%".concat(queryParam.getQuickSearchValue()).concat("%"));
        }

        Query countQuery = entityManager.createQuery("select count(*) " + fromAndWhere);

        StringBuilder selectData = new StringBuilder(32);
        selectData.append("select new com.changhong.bems.dto.PoolAttributeDto(")
                .append("p.id, p.code, p.subjectId, p.currencyCode, p.currencyName, p.manageOrg, p.manageOrgName, ")
                .append("p.periodType, p.year, p.startDate, p.endDate, p.actived, p.delay, ")
                .append("p.totalAmount, p.usedAmount, p.balance, a.attribute, a.attributeCode, a.period, a.periodName, ")
                .append("a.item, a.itemName, a.org, a.orgName, a.project, a.projectName, a.udf1, a.udf1Name, ")
                .append("a.udf2, a.udf2Name, a.udf3, a.udf3Name, a.udf4, a.udf4Name, a.udf5, a.udf5Name ")
                .append(") ");
        // 拼接查询条件
        selectData.append(fromAndWhere)
                // 默认按预算编码排倒序
                .append("order by p.code desc ");

        Query query = entityManager.createQuery(selectData.toString());
        // query.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        // List<Map<String, Object>> resultMap = query.getResultList();

        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
            query.setParameter(entry.getKey(), entry.getValue());
        }

        // 处理分页查询
        PageInfo pageInfo = queryParam.getPageInfo();
        // 获取查询的COUNT
        long total = (long) countQuery.getSingleResult();
        // 设置起始查询位置
        int start = (pageInfo.getPage() - 1) * pageInfo.getRows();
        int pageSize = pageInfo.getRows();
        if (start < total && pageSize > 0) {
            query.setFirstResult(start);
            query.setMaxResults(pageSize);
        }
        // 计算总页数
        int totalPage = (total % pageSize == 0) ? (int) (total / pageSize) : ((int) (total / pageSize) + 1);
        PageResult<PoolAttributeDto> pageResult = new PageResult<>();
        // 行数据
        pageResult.setRows(query.getResultList());
        // 总条数
        pageResult.setRecords(total);
        // 总页数
        pageResult.setTotal(totalPage);
        // 当前页
        pageResult.setPage(pageInfo.getPage());
        return pageResult;
    }

}
