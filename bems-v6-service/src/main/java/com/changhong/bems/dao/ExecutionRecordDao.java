package com.changhong.bems.dao;

import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算执行记录(ExecutionRecord)数据库访问类
 *
 * @author sei
 * @since 2021-04-25 15:10:03
 */
@Repository
public interface ExecutionRecordDao extends BaseEntityDao<ExecutionRecord> {

}