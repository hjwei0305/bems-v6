package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDao;
import com.changhong.bems.entity.Category;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算类型(Category)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@Service
public class CategoryService extends BaseEntityService<Category> {
    @Autowired
    private CategoryDao dao;

    @Override
    protected BaseEntityDao<Category> getDao() {
        return dao;
    }

}