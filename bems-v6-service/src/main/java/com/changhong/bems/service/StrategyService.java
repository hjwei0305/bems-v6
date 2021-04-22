package com.changhong.bems.service;

import com.changhong.bems.dao.StrategyDao;
import com.changhong.bems.dto.StrategyCategory;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.Dimension;
import com.changhong.bems.entity.Item;
import com.changhong.bems.entity.Strategy;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 预算策略(Strategy)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 11:12:04
 */
@Service
public class StrategyService extends BaseEntityService<Strategy> {
    @Autowired
    private StrategyDao dao;
    @Autowired
    private DimensionService dimensionService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ItemService itemService;

    @Override
    protected BaseEntityDao<Strategy> getDao() {
        return dao;
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        Dimension dimension = dimensionService.findFirstByProperty(Dimension.FIELD_STRATEGY_ID, id);
        if (Objects.nonNull(dimension)) {
            // 策略已被维度[{0}]使用,禁止删除
            return OperateResult.operationFailure("strategy_00001", dimension.getName());
        }
        Category category = categoryService.findFirstByProperty(Category.FIELD_STRATEGY_ID, id);
        if (Objects.nonNull(category)) {
            // 策略已被预算类型[{0}]使用,禁止删除
            return OperateResult.operationFailure("strategy_00002", category.getName());
        }
        Item item = itemService.findFirstByProperty(Item.FIELD_STRATEGY_ID, id);
        if (Objects.nonNull(item)) {
            // 策略已被预算科目[{0}]使用,禁止删除
            return OperateResult.operationFailure("strategy_00003", item.getName());
        }
        return OperateResult.operationSuccess();
    }

    /**
     * 按分类查询策略
     *
     * @param category 分类
     * @return 策略清单
     */
    public List<Strategy> findByCategory(StrategyCategory category) {
        return dao.findListByProperty(Strategy.FIELD_CATEGORY, category);
    }
}