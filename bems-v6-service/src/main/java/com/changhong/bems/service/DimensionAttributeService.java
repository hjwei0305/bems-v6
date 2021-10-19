package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionAttributeDao;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.entity.BaseAttribute;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.sei.core.cache.CacheBuilder;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 预算维度属性(DimensionAttribute)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@Service
public class DimensionAttributeService {
    @Autowired
    private DimensionAttributeDao dao;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CacheBuilder cacheBuilder;

    /**
     * 添加一个预算维度属性
     *
     * @param attribute 属性
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<DimensionAttribute> createAttribute(String subjectId, BaseAttribute attribute) {
        if (Objects.isNull(attribute)) {
            // 添加的预算维度属性不能为空
            return ResultData.fail(ContextUtil.getMessage("dimension_attribute_00001"));
        }

        // 检查是否存在.存在直接返回,不存在则创建
        DimensionAttribute attr = this.getAttribute(subjectId, attribute.getAttributeCode());
        if (Objects.isNull(attr)) {
            DimensionAttribute dimensionAttribute = new DimensionAttribute(attribute);
            // 租户代码
            dimensionAttribute.setTenantCode(ContextUtil.getTenantCode());
            // 预算主体
            dimensionAttribute.setSubjectId(subjectId);
            // 刷新hash值
            dimensionAttribute.getAttributeCode();
            // 持久化
            dao.save(dimensionAttribute);
            attr = dimensionAttribute;
        }
        return ResultData.success(attr);
    }

    /**
     * 通过预算类型获取维度属性组合
     *
     * @param categoryId 预算类型id
     * @return 预算属性组合
     */
    public ResultData<String> getAttribute(String categoryId) {
        List<DimensionDto> dimensions = categoryService.getAssigned(categoryId);
        if (CollectionUtils.isEmpty(dimensions)) {
            // 预算类型[{0}]下未找到预算维度!
            return ResultData.fail(ContextUtil.getMessage("category_00007", categoryId));
        }
        StringJoiner joiner = new StringJoiner(",");
        // 使用到的维度字段名,按asci码排序,逗号(,)分隔
        dimensions.stream().map(DimensionDto::getCode).sorted().forEach(joiner::add);
        return ResultData.success(joiner.toString());
    }

    /**
     * 按属性维度hash获取
     * 按维度属性值一一匹配
     */
    public DimensionAttribute getAttribute(String subjectId, long code) {
        String key = getKey(subjectId, code);
        DimensionAttribute attribute = cacheBuilder.get(key);
        if (Objects.isNull(attribute)) {
            Search search = Search.createSearch();
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_SUBJECT_ID, subjectId));
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ATTRIBUTE_CODE, code));
            attribute = dao.findOneByFilters(search);
            if (Objects.nonNull(attribute)) {
                // 缓存1天 24 * 3600 * 1000
                cacheBuilder.set(key, attribute, TimeUnit.DAYS.toMillis(1));
            }
        }
        return attribute;
    }

    /**
     * 按属性查询
     *
     * @param property 维度属性字段名
     * @param value    维度属性值
     * @return 返回其中一个
     */
    public DimensionAttribute getFirstByProperty(String property, String value) {
        return dao.findFirstByProperty(property, value);
    }

    /**
     * 按维度属性组装预算维度属性查询,排除预算期间维度
     *
     * @param subjectId     预算主体id
     * @param baseAttribute 维度属性
     * @return 返回满足条件的维度清单
     */
    public List<DimensionAttribute> getAttributes(String subjectId, BaseAttribute baseAttribute) {
        Search search = Search.createSearch();
        // 预算主体id
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_SUBJECT_ID, subjectId));
        // 预算维度组合
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ATTRIBUTE, baseAttribute.getAttribute()));

        // 预算科目
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ITEM, baseAttribute.getItem()));
        // 组织
        if (StringUtils.isNotBlank(baseAttribute.getOrg())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ORG, baseAttribute.getOrg()));
        }
        // 项目
        if (StringUtils.isNotBlank(baseAttribute.getProject())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_PROJECT, baseAttribute.getProject()));
        }
        // 自定义1
        if (StringUtils.isNotBlank(baseAttribute.getUdf1())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF1, baseAttribute.getUdf1()));
        }
        // 自定义2
        if (StringUtils.isNotBlank(baseAttribute.getUdf2())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF2, baseAttribute.getUdf2()));
        }
        // 自定义3
        if (StringUtils.isNotBlank(baseAttribute.getUdf3())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF3, baseAttribute.getUdf3()));
        }
        // 自定义4
        if (StringUtils.isNotBlank(baseAttribute.getUdf4())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF4, baseAttribute.getUdf4()));
        }
        // 自定义5
        if (StringUtils.isNotBlank(baseAttribute.getUdf5())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF5, baseAttribute.getUdf5()));
        }
        return dao.findByFilters(search);
    }

    /**
     * 按维度属性组装预算维度属性查询,排除预算期间维度
     *
     * @param subjectId     预算主体id
     * @param baseAttribute 维度属性
     * @return 返回满足条件的维度清单
     */
    public List<DimensionAttribute> getAttributes(String subjectId, String periodId, BaseAttribute baseAttribute) {
        Search search = Search.createSearch();
        // 预算主体id
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_SUBJECT_ID, subjectId));
        // 预算维度组合
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ATTRIBUTE, baseAttribute.getAttribute()));

        // 预算科目
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ITEM, baseAttribute.getItem()));
        // 预算期间
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_PERIOD, periodId));
        // 组织
        if (StringUtils.isNotBlank(baseAttribute.getOrg())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ORG, baseAttribute.getOrg()));
        }
        // 项目
        if (StringUtils.isNotBlank(baseAttribute.getProject())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_PROJECT, baseAttribute.getProject()));
        }
        // 自定义1
        if (StringUtils.isNotBlank(baseAttribute.getUdf1())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF1, baseAttribute.getUdf1()));
        }
        // 自定义2
        if (StringUtils.isNotBlank(baseAttribute.getUdf2())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF2, baseAttribute.getUdf2()));
        }
        // 自定义3
        if (StringUtils.isNotBlank(baseAttribute.getUdf3())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF3, baseAttribute.getUdf3()));
        }
        // 自定义4
        if (StringUtils.isNotBlank(baseAttribute.getUdf4())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF4, baseAttribute.getUdf4()));
        }
        // 自定义5
        if (StringUtils.isNotBlank(baseAttribute.getUdf5())) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_UDF5, baseAttribute.getUdf5()));
        }
        return dao.findByFilters(search);
    }

    /**
     * 按预算主体和维度属性查询
     * 用于预算占用
     *
     * @param subjectIds 预算主体清单
     * @param attribute  维度组合
     * @param dimFilters 维度条件
     * @return 返回满足条件的维度清单
     */
    public List<DimensionAttribute> getAttributes(List<String> subjectIds, String attribute, Collection<SearchFilter> dimFilters) {
        Search search = Search.createSearch();
        // 预算主体id
        Set<String> subjectIdSet = new HashSet<>(subjectIds);
        if (subjectIdSet.size() > 1) {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_SUBJECT_ID, subjectIdSet, SearchFilter.Operator.IN));
        } else {
            search.addFilter(new SearchFilter(DimensionAttribute.FIELD_SUBJECT_ID, subjectIds.get(0)));
        }
        // 预算维度组合
        search.addFilter(new SearchFilter(DimensionAttribute.FIELD_ATTRIBUTE, attribute));
        // 其他维度条件
        if (CollectionUtils.isNotEmpty(dimFilters)) {
            for (SearchFilter filter : dimFilters) {
                search.addFilter(filter);
            }
        }
        return dao.findByFilters(search);
    }

    private String getKey(String subjectId, long code) {
        return "bems-v6:attribute:" + subjectId + code;
    }
}