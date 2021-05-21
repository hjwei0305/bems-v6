package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.DimensionDao;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.CategoryDimension;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.entity.Strategy;
import com.changhong.bems.service.strategy.DefaultEqualMatchStrategy;
import com.changhong.bems.service.strategy.DefaultOrgTreeMatchStrategy;
import com.changhong.bems.service.strategy.DefaultPeriodMatchStrategy;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预算维度(Dimension)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Service
@CacheConfig(cacheNames = DimensionService.CACHE_KEY)
public class DimensionService extends BaseEntityService<Dimension> {
    @Autowired
    private DimensionDao dao;
    @Autowired
    private CategoryDimensionService categoryDimensionService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private StrategyService strategyService;

    public static final String CACHE_KEY = "bems-v6:dimension";

    @Override
    protected BaseEntityDao<Dimension> getDao() {
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
        Dimension dimension = dao.findOne(id);
        if (Objects.nonNull(dimension)) {
            CategoryDimension categoryDimension = categoryDimensionService.getByDimensionCode(dimension.getCode());
            if (Objects.nonNull(categoryDimension)) {
                Category category = categoryService.findOne(categoryDimension.getCategoryId());
                String obj = Objects.isNull(category) ? categoryDimension.getCategoryId() : category.getName();
                // 维度已被预算类型[{0}]使用,禁止删除
                return OperateResult.operationFailure("dimension_00001", obj);
            }
            dao.delete(dimension);
            return OperateResult.operationSuccess();
        } else {
            // 维度不存在!
            return OperateResult.operationFailure("dimension_00002", id);
        }
    }

    /**
     * 数据保存操作
     */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Dimension> save(Dimension entity) {
        if (Objects.isNull(entity)) {
            return OperateResultWithData.operationFailure("dimension_00003");
        }
        // 科目
        if (StringUtils.equals(Constants.DIMENSION_CODE_ITEM, entity.getCode())) {
            // 设置为系统必须
            entity.setRequired(Boolean.TRUE);
        }
        // 期间
        else if (StringUtils.equals(Constants.DIMENSION_CODE_PERIOD, entity.getCode())) {
            // 设置为系统必须
            entity.setRequired(Boolean.TRUE);
        }
        return super.save(entity);
    }

    /**
     * 检查和初始化数据
     * 当检测到租户下不存在维度数据时,默认初始化预制的维度数据
     */
    @Transactional(rollbackFor = Exception.class)
    @SeiLock(key = "'DimensionService:checkAndInit'")
    public ResultData<List<Dimension>> checkAndInit() {
        List<Dimension> dimensions = dao.findAll();
        if (CollectionUtils.isEmpty(dimensions)) {
            List<Strategy> strategies = strategyService.checkAndInit();
            Map<String, Strategy> strategyMap = strategies.stream().collect(Collectors.toMap(Strategy::getCode, s -> s));

            dimensions = new ArrayList<>();
            Dimension dimension;
            Strategy strategy;
            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_PERIOD);
            dimension.setName("预算期间");
            dimension.setRequired(Boolean.TRUE);
            dimension.setRank(1);
            strategy = strategyMap.get(DefaultPeriodMatchStrategy.class.getSimpleName());
            dimension.setStrategyId(strategy.getId());
            dimension.setStrategyName(strategy.getName());
            dimension.setUiComponent("Period");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_ITEM);
            dimension.setName("预算科目");
            dimension.setRequired(Boolean.TRUE);
            dimension.setRank(2);
            strategy = strategyMap.get(DefaultEqualMatchStrategy.class.getSimpleName());
            dimension.setStrategyId(strategy.getId());
            dimension.setStrategyName(strategy.getName());
            dimension.setUiComponent("Subject");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_ORG);
            dimension.setName("组织机构");
            dimension.setRank(3);
            strategy = strategyMap.get(DefaultOrgTreeMatchStrategy.class.getSimpleName());
            dimension.setStrategyId(strategy.getId());
            dimension.setStrategyName(strategy.getName());
            dimension.setUiComponent("Organization");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_PROJECT);
            dimension.setName("项目");
            dimension.setRank(4);
            strategy = strategyMap.get(DefaultEqualMatchStrategy.class.getSimpleName());
            dimension.setStrategyId(strategy.getId());
            dimension.setStrategyName(strategy.getName());
            dimension.setUiComponent("ProjectList");
            super.save(dimension);
            dimensions.add(dimension);
        }
        return ResultData.success(dimensions);
    }

    /**
     * 基于主键集合查询集合数据对象
     */
    @Override
    @Cacheable(key = "'all'")
    public List<Dimension> findAll() {
        return dao.findAll();
    }

    /**
     * 根据维度代码获取预算维度对象
     *
     * @param code 预算代码
     * @return 预算维度对象
     */
    @Cacheable(key = "#code")
    public Dimension findByCode(String code) {
        // 通过getBean方式走缓存
        List<Dimension> dimensions = ContextUtil.getBean(DimensionService.class).findAll();
        return dimensions.stream().filter(d -> StringUtils.equals(code, d.getCode())).findFirst().orElse(null);
    }

    /**
     * 获取必要维度清单
     */
    public List<Dimension> getRequired() {
        // 通过getBean方式走缓存
        List<Dimension> dimensions = ContextUtil.getBean(DimensionService.class).findAll();
        return dimensions.stream().filter(Dimension::getRequired).collect(Collectors.toList());
    }
}