package com.changhong.bems.service;

import com.changhong.bems.dao.ExecutionRecordDao;
import com.changhong.bems.dao.ExecutionRecordViewDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.ExecutionRecordView;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.service.BaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ExecutionRecordViewDao viewDao;

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
    public List<ExecutionRecord> getUseRecords(String eventCode, String bizId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_BIZ_ID, bizId));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_EVENT_CODE, eventCode));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_OPERATION, OperationType.USE));
        search.addFilter(new SearchFilter(ExecutionRecord.FIELD_IS_FREED, Boolean.FALSE));
        // 为保证释放顺序: 先占用后释放 -> 时间戳倒序
        search.addSortOrder(SearchOrder.desc(ExecutionRecord.FIELD_TIMESTAMP));
        return dao.findByFilters(search);
    }


    /**
     * 更新是否被释放标记
     * 为保证占用幂等性,通过此标记判断是否已释放,避免重复释放
     *
     * @param id 记录id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFreed(String id) {
        dao.updateFreed(id, Boolean.TRUE);
    }

    public PageResult<ExecutionRecordView> findViewByPage(Search search) {
        return viewDao.findByPage(search);
    }
}