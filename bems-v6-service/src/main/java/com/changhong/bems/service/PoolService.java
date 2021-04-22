package com.changhong.bems.service;

import com.changhong.bems.dao.PoolDao;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算池(Pool)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Service
public class PoolService extends BaseEntityService<Pool> {
    @Autowired
    private PoolDao dao;

    @Override
    protected BaseEntityDao<Pool> getDao() {
        return dao;
    }

}