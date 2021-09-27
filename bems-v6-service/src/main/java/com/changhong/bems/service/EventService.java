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
import java.util.Objects;
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
            event.setCode(Constants.EVENT_BUDGET_INJECTION);
            event.setName("预算注入");
            event.setBizFrom(appCode);
            event.setRank(1);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_ADJUSTMENT);
            event.setName("预算调整");
            event.setBizFrom(appCode);
            event.setRank(2);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_SPLIT);
            event.setName("预算分解");
            event.setBizFrom(appCode);
            event.setRank(3);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_INJECTION_CANCEL);
            event.setName("撤销预算注入");
            event.setBizFrom(appCode);
            event.setRank(4);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_ADJUSTMENT_CANCEL);
            event.setName("撤销预算调整");
            event.setBizFrom(appCode);
            event.setRank(5);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_SPLIT_CANCEL);
            event.setName("撤销预算分解");
            event.setBizFrom(appCode);
            event.setRank(6);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_TRUNDLE);
            event.setName("预算滚动结转");
            event.setBizFrom(appCode);
            event.setRank(7);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_FREEZE);
            event.setName("预算冻结");
            event.setBizFrom(appCode);
            event.setRank(8);
            event.setRequired(Boolean.TRUE);
            super.save(event);
            eventList.add(event);
            event = new Event();
            event.setCode(Constants.EVENT_BUDGET_UNFREEZE);
            event.setName("预算解冻");
            event.setBizFrom(appCode);
            event.setRank(9);
            event.setRequired(Boolean.TRUE);
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
    public String getEventName(String code) {
        Event event = dao.findFirstByProperty(Event.CODE_FIELD, code);
        return Objects.isNull(event) ? "" : event.getName();
    }
}