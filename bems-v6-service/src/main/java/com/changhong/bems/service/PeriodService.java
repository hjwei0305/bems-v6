package com.changhong.bems.service;

import com.changhong.bems.dao.PeriodDao;
import com.changhong.bems.entity.Period;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算期间(Period)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:22
 */
@Service
public class PeriodService extends BaseEntityService<Period> {
    @Autowired
    private PeriodDao dao;

    @Override
    protected BaseEntityDao<Period> getDao() {
        return dao;
    }

}