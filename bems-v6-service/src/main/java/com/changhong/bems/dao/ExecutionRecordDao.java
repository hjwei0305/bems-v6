package com.changhong.bems.dao;

import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 预算执行记录(ExecutionRecord)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:10:03
 */
@Repository
public interface ExecutionRecordDao extends BaseEntityDao<ExecutionRecord> {

    /**
     * 更新是否被释放标记
     * 为保证占用幂等性,通过此标记判断是否已释放,避免重复释放
     *
     * @param id    记录id
     * @param freed 释放标记
     */
    @Modifying
    @Query("update ExecutionRecord r set r.isFreed = :freed where r.id = :id ")
    void updateFreed(@Param("id") String id, @Param("freed") boolean freed);
}