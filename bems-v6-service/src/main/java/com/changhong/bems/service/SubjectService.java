package com.changhong.bems.service;

import com.changhong.bems.dao.SubjectDao;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算主体(Subject)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:25
 */
@Service
public class SubjectService extends BaseEntityService<Subject> {
    @Autowired
    private SubjectDao dao;

    @Override
    protected BaseEntityDao<Subject> getDao() {
        return dao;
    }

}