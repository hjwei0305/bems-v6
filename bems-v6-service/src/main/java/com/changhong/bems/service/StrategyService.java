package com.changhong.bems.service;

import com.changhong.bems.dao.StrategyDao;
import com.changhong.bems.entity.Strategy;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * (Strategy)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 11:12:04
 */
@Service
public class StrategyService extends BaseEntityService<Strategy> {
    @Autowired
    private StrategyDao dao;

    @Override
    protected BaseEntityDao<Strategy> getDao() {
        return dao;
    }

}