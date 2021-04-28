package com.changhong.bems.service;

import com.changhong.bems.dao.StrategyDao;
import com.changhong.bems.dto.StrategyCategory;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.entity.Strategy;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 预算策略(Strategy)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 11:12:04
 */
@Service
@CacheConfig(cacheNames = StrategyService.CACHE_KEY)
public class StrategyService extends BaseEntityService<Strategy> {
    @Autowired
    private StrategyDao dao;
    @Autowired
    private DimensionService dimensionService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectItemService subjectItemService;

    public static final String CACHE_KEY = "bems-v6:strategy";

    @Override
    protected BaseEntityDao<Strategy> getDao() {
        return dao;
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResult delete(String id) {
        Strategy entity = findOne(id);
        if (Objects.nonNull(entity)) {
            Dimension dimension = dimensionService.findFirstByProperty(Dimension.FIELD_STRATEGY_ID, id);
            if (Objects.nonNull(dimension)) {
                // 策略已被维度[{0}]使用,禁止删除
                return OperateResult.operationFailure("strategy_00001", dimension.getName());
            }
            Subject subject = subjectService.findFirstByProperty(Subject.FIELD_STRATEGY_ID, id);
            if (Objects.nonNull(subject)) {
                // 策略已被预算主体[{0}]使用,禁止删除
                return OperateResult.operationFailure("strategy_00002", subject.getName());
            }
            SubjectItem item = subjectItemService.findFirstByProperty(SubjectItem.FIELD_STRATEGY_ID, id);
            if (Objects.nonNull(item)) {
                // 策略已被预算科目[{0}]使用,禁止删除
                return OperateResult.operationFailure("strategy_00003", item.getName());
            }
            dao.delete(entity);
            return OperateResult.operationSuccess("core_service_00028");
        } else {
            return OperateResult.operationWarning("core_service_00029");
        }
    }

    /**
     * 数据保存操作
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Strategy> save(Strategy entity) {
        return super.save(entity);
    }

    /**
     * 基于主键查询单一数据对象
     */
    @Override
    @Cacheable(key = "#id")
    public Strategy findOne(String id) {
        return dao.findOne(id);
    }

    /**
     * 基于主键集合查询集合数据对象
     */
    @Override
    @Cacheable(key = "'all'")
    public List<Strategy> findAll() {
        return dao.findAll();
    }

    /**
     * 按分类查询策略
     *
     * @param category 分类
     * @return 策略清单
     */
    @Cacheable(key = "#category.name()")
    public List<Strategy> findByCategory(StrategyCategory category) {
        return dao.findListByProperty(Strategy.FIELD_CATEGORY, category);
    }
}