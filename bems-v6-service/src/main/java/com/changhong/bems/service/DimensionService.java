package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionDao;
import com.changhong.bems.entity.Dimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算维度(Dimension)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Service
public class DimensionService extends BaseEntityService<Dimension> {
    @Autowired
    private DimensionDao dao;

    @Override
    protected BaseEntityDao<Dimension> getDao() {
        return dao;
    }

}