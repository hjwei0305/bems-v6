package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionAttributeDao;
import com.changhong.bems.entity.DimensionAttribute;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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

    @Override
    protected BaseEntityDao<DimensionAttribute> getDao() {
        return dao;
    }

}