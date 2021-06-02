package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionAttributeDao;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.entity.BaseAttribute;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.sei.core.cache.CacheBuilder;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * 预算维度属性(DimensionAttribute)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@Service
public class DimensionAttributeService extends BaseEntityService<DimensionAttribute> {
    @Autowired
    private DimensionAttributeDao dao;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CacheBuilder cacheBuilder;

    @Override
    protected BaseEntityDao<DimensionAttribute> getDao() {
        return dao;
    }

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
            dimensionAttribute.setSubjectId(subjectId);
            // 刷新hash值
            dimensionAttribute.getAttributeCode();
            this.save(dimensionAttribute);
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

    private String getKey(String subjectId, long code) {
        return "bems-v6:attribute:" + subjectId + code;
    }
}