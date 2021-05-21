package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.EventDao;
import com.changhong.bems.entity.Event;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.limiter.support.lock.SeiLock;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
     * 检查和初始化数据
     * 当检测到租户下不存在维度数据时,默认初始化预制的维度数据
     */
    @Transactional(rollbackFor = Exception.class)
    @SeiLock(key = "'EventService:checkAndInit'")
    public ResultData<List<Event>> checkAndInit() {
        List<Event> eventList = dao.findAll();
        if (CollectionUtils.isEmpty(eventList)) {
            eventList = new ArrayList<>();
            String appCode = ContextUtil.getAppCode();

            Event event;
            event = new Event();
            event.setCode(Constants.EVENT_INJECTION_EFFECTIVE);
            event.setName("预算下达生效");
            event.setBizFrom(appCode);
            event.setRank(1);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_INJECTION_SUBMIT);
            event.setName("预算下达提交流程");
            event.setBizFrom(appCode);
            event.setRank(2);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_INJECTION_CANCEL);
            event.setName("预算下达终止流程");
            event.setBizFrom(appCode);
            event.setRank(3);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_INJECTION_COMPLETE);
            event.setName("预算下达流程完成");
            event.setBizFrom(appCode);
            event.setRank(4);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_ADJUSTMENT_EFFECTIVE);
            event.setName("预算调整生效");
            event.setBizFrom(appCode);
            event.setRank(5);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_ADJUSTMENT_SUBMIT);
            event.setName("预算调整提交流程");
            event.setBizFrom(appCode);
            event.setRank(6);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_ADJUSTMENT_CANCEL);
            event.setName("预算调整终止流程");
            event.setBizFrom(appCode);
            event.setRank(7);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_ADJUSTMENT_COMPLETE);
            event.setName("预算调整流程完成");
            event.setBizFrom(appCode);
            event.setRank(8);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_SPLIT_EFFECTIVE);
            event.setName("预算分解生效");
            event.setBizFrom(appCode);
            event.setRank(9);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_SPLIT_SUBMIT);
            event.setName("预算分解提交流程");
            event.setBizFrom(appCode);
            event.setRank(10);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_SPLIT_CANCEL);
            event.setName("预算分解终止流程");
            event.setBizFrom(appCode);
            event.setRank(11);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_SPLIT_COMPLETE);
            event.setName("预算分解流程完成");
            event.setBizFrom(appCode);
            event.setRank(12);
            super.save(event);
            eventList.add(event);
        }
        return ResultData.success(eventList);
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
     * 获取所有未冻结的业务实体
     *
     * @return 业务实体清单
     */
    @Override
    @Cacheable(key = "'allUnfrozen'")
    public List<Event> findAllUnfrozen() {
        return dao.findAllUnfrozen();
    }

    /**
     * 按标签获取事件
     * 主要用于报表统计查询
     */
    public List<Event> findByLabel(final String label) {
        // 通过getBean方式走缓存
        List<Event> eventList = ContextUtil.getBean(EventService.class).findAllUnfrozen();
        if (StringUtils.isNotBlank(label)) {
            return eventList.stream()
                    .filter(d -> StringUtils.containsIgnoreCase(d.getLabel(), label))
                    .collect(Collectors.toList());
        } else {
            return eventList;
        }
    }

    /**
     * 按指定业务来源系统获取预算事件
     *
     * @param bizFrom 业务来源系统
     * @return 预算事件清单
     */
    public List<Event> findByBizFrom(final String bizFrom) {
        // 通过getBean方式走缓存
        List<Event> eventList = ContextUtil.getBean(EventService.class).findAllUnfrozen();
        if (StringUtils.isNotBlank(bizFrom)) {
            return eventList.stream()
                    .filter(d -> StringUtils.equalsIgnoreCase(d.getBizFrom(), bizFrom))
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