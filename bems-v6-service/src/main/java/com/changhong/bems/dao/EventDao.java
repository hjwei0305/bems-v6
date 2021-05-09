package com.changhong.bems.dao;

import com.changhong.bems.entity.Event;
import com.changhong.sei.core.dao.BaseEntityDao;
import org.springframework.stereotype.Repository;

/**
 * 预算事件(Event)数据库访问类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Repository
public interface EventDao extends BaseEntityDao<Event> {

}