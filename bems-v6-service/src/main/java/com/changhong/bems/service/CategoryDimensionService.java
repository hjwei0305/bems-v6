package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDimensionDao;
import com.changhong.bems.entity.CategoryDimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


/**
 * 预算类型维度关系(CategoryDimension)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:24
 */
@Service
public class CategoryDimensionService extends BaseEntityService<CategoryDimension> {
    @Autowired
    private CategoryDimensionDao dao;

    @Override
    protected BaseEntityDao<CategoryDimension> getDao() {
        return dao;
    }

    /**
     * 根据预算类型id和维度代码获取分配关系
     *
     * @param categoryId 预算类型id
     * @param codes      预算维度代码
     * @return 返回分配关系清单
     */
    public List<CategoryDimension> getCategoryDimensions(String categoryId, Collection<String> codes) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(CategoryDimension.FIELD_CATEGORY_ID, categoryId));
        search.addFilter(new SearchFilter(CategoryDimension.FIELD_DIMENSION_CODE, codes, SearchFilter.Operator.IN));
        return findByFilters(search);
    }

}