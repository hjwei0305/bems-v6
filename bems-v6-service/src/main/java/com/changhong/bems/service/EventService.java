package com.changhong.bems.service;

import com.changhong.bems.dao.EventDao;
import com.changhong.bems.entity.Event;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算事件(Event)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@Service
@CacheConfig(cacheNames = EventService.CACHE_KEY)
public class EventService extends BaseEntityService<Event> {
    @Autowired
    private EventDao dao;

    public static final String CACHE_KEY = "bems-v6:event";

    @Override
    protected BaseEntityDao<Event> getDao() {
        return dao;
    }

    /**
     * 数据保存操作
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Event> save(Event entity) {
        return super.save(entity);
    }

    /**
     * 主键删除
     *
     * @param s 主键
     * @return 返回操作结果对象
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResult delete(String s) {
        return super.delete(s);
    }

    /**
     * 基于主键集合查询集合数据对象
     */
    @Override
    @Cacheable(key = "'all'")
    public List<Event> findAll() {
        return dao.findAll();
    }

    /**
     * 按标签获取事件
     * 主要用于报表统计查询
     */
    public List<Event> findByLabel(final String label) {
        List<Event> eventList = findAll();
        if (StringUtils.isNotBlank(label)) {
            return eventList.stream()
                    .filter(d -> StringUtils.containsIgnoreCase(d.getLabel(), label))
                    .collect(Collectors.toList());
        } else {
            return eventList;
        }
    }

    /**
     * 根据事件代码获取预算事件对象
     *
     * @param code 预算事件代码
     * @return 预算事件对象
     */
    @Cacheable(key = "#code")
    public Event findByCode(String code) {
        return dao.findFirstByProperty(Event.CODE_FIELD, code);
    }
}