package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDimensionDao;
import com.changhong.bems.entity.CategoryDimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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


}