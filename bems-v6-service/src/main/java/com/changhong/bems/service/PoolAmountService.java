package com.changhong.bems.service;

import com.changhong.bems.dao.PoolAmountDao;
import com.changhong.bems.entity.PoolAmount;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算池金额(PoolAmount)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:14:01
 */
@Service
public class PoolAmountService extends BaseEntityService<PoolAmount> {
    @Autowired
    private PoolAmountDao dao;

    @Override
    protected BaseEntityDao<PoolAmount> getDao() {
        return dao;
    }

}