package com.changhong.bems.service;

import com.changhong.bems.dao.ExecutionRecordDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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

    /**
     * 通过事件和业务id获取占用记录
     *
     * @param eventCode 事件代码
     * @param bizId     业务id
     * @return 返回满足条件的占用记录
     */
    public ExecutionRecord getUseRecord(String eventCode, String bizId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_BIZ_ID, bizId));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_EVENT_CODE, eventCode));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_OPERATION, OperationType.USE));
        return dao.findFirstByFilters(search);
    }

    /**
     * 通过事件和业务id获取占用记录
     *
     * @param eventCode 事件代码
     * @param bizId     业务id
     * @return 返回满足条件的占用记录
     */
    public List<ExecutionRecord> getUseRecords(String eventCode, String bizId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_BIZ_ID, bizId));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_EVENT_CODE, eventCode));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_OPERATION, OperationType.USE));
        return dao.findByFilters(search);
    }

}