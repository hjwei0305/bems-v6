package com.changhong.bems.service;

import com.changhong.bems.dao.ExecutionRecordDao;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 预算执行记录(ExecutionRecord)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:10:03
 */
@Service
public class ExecutionRecordService extends BaseEntityService<ExecutionRecord> {
    @Autowired
    private ExecutionRecordDao dao;

    @Override
    protected BaseEntityDao<ExecutionRecord> getDao() {
        return dao;
    }

}