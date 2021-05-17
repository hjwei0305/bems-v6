package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionDao;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.CategoryDimension;
import com.changhong.bems.entity.Dimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预算维度(Dimension)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Service
@CacheConfig(cacheNames = DimensionService.CACHE_KEY)
public class DimensionService extends BaseEntityService<Dimension> {
    @Autowired
    private DimensionDao dao;
    @Autowired
    private CategoryDimensionService categoryDimensionService;
    @Autowired
    private CategoryService categoryService;

    public static final String CACHE_KEY = "bems-v6:dimension";

    @Override
    protected BaseEntityDao<Dimension> getDao() {
        return dao;
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResult delete(String id) {
        Dimension dimension = dao.findOne(id);
        if (Objects.nonNull(dimension)) {
            CategoryDimension categoryDimension = categoryDimensionService.getByDimensionCode(dimension.getCode());
            if (Objects.nonNull(categoryDimension)) {
                Category category = categoryService.findOne(categoryDimension.getCategoryId());
                String obj = Objects.isNull(category) ? categoryDimension.getCategoryId() : category.getName();
                // 维度已被预算类型[{0}]使用,禁止删除
                return OperateResult.operationFailure("dimension_00001", obj);
            }
            dao.delete(dimension);
            return OperateResult.operationSuccess();
        } else {
            // 维度不存在!
            return OperateResult.operationFailure("dimension_00002", id);
        }
    }

    /**
     * 数据保存操作
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Dimension> save(Dimension entity) {
        return super.save(entity);
    }

    /**
     * 基于主键集合查询集合数据对象
     */
    @Override
    @Cacheable(key = "'all'")
    public List<Dimension> findAll() {
        return dao.findAll();
    }

    /**
     * 根据维度代码获取预算维度对象
     *
     * @param code 预算代码
     * @return 预算维度对象
     */
    @Cacheable(key = "#code")
    public Dimension findByCode(String code) {
        List<Dimension> dimensions = findAll();
        return dimensions.stream().filter(d -> StringUtils.equals(code, d.getCode())).findFirst().orElse(null);
    }

    /**
     * 获取必要维度清单
     */
    public List<Dimension> getRequired() {
        List<Dimension> dimensions = findAll();
        return dimensions.stream().filter(Dimension::getRequired).collect(Collectors.toList());
    }
}