package com.changhong.bems.service;

import com.changhong.bems.dao.LogRecordDao;
import com.changhong.bems.dao.LogRecordViewDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.LogRecord;
import com.changhong.bems.entity.LogRecordView;
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
import java.util.Objects;


/**
 * 预算执行记录(LogRecord)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:10:03
 */
@Service
public class LogRecordService extends BaseEntityService<LogRecord> {
    @Autowired
    private LogRecordDao dao;
    @Autowired
    private LogRecordViewDao viewDao;

    @Override
    protected BaseEntityDao<LogRecord> getDao() {
        return dao;
    }

    /**
     * 通过事件和业务id获取占用记录
     *
     * @param eventCode 事件代码
     * @param bizId     业务id
     * @return 返回满足条件的占用记录
     */
    public List<LogRecord> getUseRecords(String eventCode, String bizId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(LogRecord.FIELD_BIZ_ID, bizId));
        search.addFilter(new SearchFilter(LogRecord.FIELD_EVENT_CODE, eventCode));
        search.addFilter(new SearchFilter(LogRecord.FIELD_OPERATION, OperationType.USE));
        search.addFilter(new SearchFilter(LogRecord.FIELD_IS_FREED, Boolean.FALSE));
        // 为保证释放顺序: 先占用后释放 -> 时间戳倒序
        search.addSortOrder(SearchOrder.desc(LogRecord.FIELD_TIMESTAMP));
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

    public PageResult<LogRecordView> findViewByPage(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 按时间戳排序
        search.addSortOrder(SearchOrder.desc(LogRecordView.FIELD_TIMESTAMP));
        return viewDao.findByPage(search);
    }

    public List<LogRecordView> findLogRecords(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 按时间戳排序
        search.addSortOrder(SearchOrder.desc(LogRecordView.FIELD_TIMESTAMP));
        return viewDao.findByFilters(search);
    }
}