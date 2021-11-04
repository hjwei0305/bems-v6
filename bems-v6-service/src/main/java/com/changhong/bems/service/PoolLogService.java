package com.changhong.bems.service;

import com.changhong.bems.dao.PoolLogDao;
import com.changhong.bems.dto.OperationType;
import com.changhong.bems.entity.PoolLog;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.dto.serach.SearchOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 预算池日志记录(PoolLog)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-25 15:10:03
 */
@Service
public class PoolLogService {
    @Autowired
    private PoolLogDao dao;

    /**
     * 通过事件和业务id获取占用记录
     *
     * @param eventCode 事件代码
     * @param bizId     业务id
     * @return 返回满足条件的占用记录
     */
    public List<PoolLog> getUseRecords(String eventCode, String bizId) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(PoolLog.FIELD_BIZ_ID, bizId));
        search.addFilter(new SearchFilter(PoolLog.FIELD_EVENT_CODE, eventCode));
        search.addFilter(new SearchFilter(PoolLog.FIELD_OPERATION, OperationType.USE));
        search.addFilter(new SearchFilter(PoolLog.FIELD_IS_FREED, Boolean.FALSE));
        // 为保证释放顺序: 先占用后释放 -> 时间戳倒序
        search.addSortOrder(SearchOrder.desc(PoolLog.FIELD_TIMESTAMP));
        return dao.findByFilters(search);
    }

    /**
     * 记录日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void addLogRecord(PoolLog poolLog) {
        poolLog.setTenantCode(ContextUtil.getTenantCode());
        // 操作时间
        poolLog.setOpTime(LocalDateTime.now());
        poolLog.setTimestamp(System.nanoTime());
        // 操作人
        if (StringUtils.isBlank(poolLog.getOpUserAccount())) {
            SessionUser user = ContextUtil.getSessionUser();
            poolLog.setOpUserAccount(user.getAccount());
            poolLog.setOpUserName(user.getUserName());
        }
        dao.save(poolLog);
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

    public PageResult<PoolLog> findByPage(Search search) {
        if (Objects.isNull(search)) {
            search = Search.createSearch();
        }
        // 按时间戳排序
        search.addSortOrder(SearchOrder.desc(PoolLog.FIELD_TIMESTAMP));
        return dao.findByPage(search);
    }
}