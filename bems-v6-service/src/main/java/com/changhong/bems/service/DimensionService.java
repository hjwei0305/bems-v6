package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dao.DimensionDao;
import com.changhong.bems.dto.StrategyDto;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.CategoryDimension;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.service.strategy.EqualMatchStrategy;
import com.changhong.bems.service.strategy.OrgTreeMatchStrategy;
import com.changhong.bems.service.strategy.PeriodMatchStrategy;
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
            // 占用默认固定策略
            StrategyDto strategy = strategyService.getByCode(StringUtils.uncapitalize(EqualMatchStrategy.class.getSimpleName()));
            if (Objects.nonNull(strategy)) {
                if (!StringUtils.equals(strategy.getCode(), entity.getStrategyId())) {
                    // 预算科目维度必须使用一致性匹配策略
                    return OperateResultWithData.operationFailure("dimension_00004");
                }
            } else {
                // 预算科目维度必须使用一致性匹配策略
                return OperateResultWithData.operationFailure("dimension_00004");
            }
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
            dimensions = new ArrayList<>();
            Dimension dimension;
            StrategyDto strategy;
            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_PERIOD);
            // 预算期间
            dimension.setName(ContextUtil.getMessage("default_dimension_period"));
            dimension.setRequired(Boolean.TRUE);
            dimension.setRank(1);
            strategy = strategyService.getByCode(StringUtils.uncapitalize(PeriodMatchStrategy.class.getSimpleName()));
            if (Objects.nonNull(strategy)) {
                dimension.setStrategyId(strategy.getCode());
                dimension.setStrategyName(strategy.getName());
            } else {
                dimension.setStrategyId(Constants.NONE);
                dimension.setStrategyName(Constants.NONE);
            }
            dimension.setUiComponent("Period");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_ITEM);
            // 预算科目
            dimension.setName(ContextUtil.getMessage("default_dimension_item"));
            dimension.setRequired(Boolean.TRUE);
            dimension.setRank(2);
            strategy = strategyService.getByCode(StringUtils.uncapitalize(EqualMatchStrategy.class.getSimpleName()));
            if (Objects.nonNull(strategy)) {
                dimension.setStrategyId(strategy.getCode());
                dimension.setStrategyName(strategy.getName());
            } else {
                dimension.setStrategyId(Constants.NONE);
                dimension.setStrategyName(Constants.NONE);
            }
            dimension.setUiComponent("Subject");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_ORG);
            // 组织机构
            dimension.setName(ContextUtil.getMessage("default_dimension_org"));
            dimension.setRank(3);
            strategy = strategyService.getByCode(StringUtils.uncapitalize(OrgTreeMatchStrategy.class.getSimpleName()));
            if (Objects.nonNull(strategy)) {
                dimension.setStrategyId(strategy.getCode());
                dimension.setStrategyName(strategy.getName());
            } else {
                dimension.setStrategyId(Constants.NONE);
                dimension.setStrategyName(Constants.NONE);
            }
            dimension.setUiComponent("Organization");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_PROJECT);
            // 项目
            dimension.setName(ContextUtil.getMessage("default_dimension_project"));
            dimension.setRank(4);
            strategy = strategyService.getByCode(StringUtils.uncapitalize(EqualMatchStrategy.class.getSimpleName()));
            if (Objects.nonNull(strategy)) {
                dimension.setStrategyId(strategy.getCode());
                dimension.setStrategyName(strategy.getName());
            } else {
                dimension.setStrategyId(Constants.NONE);
                dimension.setStrategyName(Constants.NONE);
            }
            dimension.setUiComponent("ProjectList");
            super.save(dimension);
            dimensions.add(dimension);

            dimension = new Dimension();
            dimension.setCode(Constants.DIMENSION_CODE_COST_CENTER);
            // 成本中心
            dimension.setName(ContextUtil.getMessage("default_dimension_cost_center"));
            dimension.setRank(5);
            strategy = strategyService.getByCode(StringUtils.uncapitalize(EqualMatchStrategy.class.getSimpleName()));
            if (Objects.nonNull(strategy)) {
                dimension.setStrategyId(strategy.getCode());
                dimension.setStrategyName(strategy.getName());
            } else {
                dimension.setStrategyId(Constants.NONE);
                dimension.setStrategyName(Constants.NONE);
            }
            dimension.setUiComponent("CostCenterList");
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
        return dao.findByProperty(Dimension.CODE_FIELD, code);
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