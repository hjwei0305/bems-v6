package com.changhong.bems.service;

import com.changhong.bems.dao.DimensionDao;
import com.changhong.bems.dto.KeyValueDto;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.CategoryDimension;
import com.changhong.bems.entity.Dimension;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 预算维度(Dimension)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Service
public class DimensionService extends BaseEntityService<Dimension> {
    @Autowired
    private DimensionDao dao;
    @Autowired
    private CategoryDimensionService categoryDimensionService;
    @Autowired
    private CategoryService categoryService;

    @Override
    protected BaseEntityDao<Dimension> getDao() {
        return dao;
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        Dimension dimension = dao.findOne(id);
        if (Objects.nonNull(dimension)) {
            CategoryDimension categoryDimension = categoryDimensionService.findFirstByProperty(CategoryDimension.FIELD_DIMENSION_CODE, dimension.getCode());
            if (Objects.nonNull(categoryDimension)) {
                Category category = categoryService.findOne(categoryDimension.getCategoryId());
                String obj = Objects.isNull(category) ? categoryDimension.getCategoryId() : category.getName();
                // 维度已被预算类型[{0}]使用,禁止删除
                return OperateResult.operationFailure("dimension_00001", obj);
            }
            return OperateResult.operationSuccess();
        } else {
            // 维度不存在!
            return OperateResult.operationFailure("dimension_00002");
        }
    }

    /**
     * 获取所有预制的维度代码
     *
     * @return 策略清单
     */
    public Set<KeyValueDto> findAllCodes() {
        Set<KeyValueDto> set = new LinkedHashSet<>();
        set.add(new KeyValueDto("period", "预算期间"));
        set.add(new KeyValueDto("item", "预算科目"));
        set.add(new KeyValueDto("org", "组织机构"));
        set.add(new KeyValueDto("project", "项目"));
        set.add(new KeyValueDto("udf1", "自定义1"));
        set.add(new KeyValueDto("udf2", "自定义2"));
        set.add(new KeyValueDto("udf3", "自定义3"));
        set.add(new KeyValueDto("udf4", "自定义4"));
        set.add(new KeyValueDto("udf5", "自定义5"));
        return set;
    }
}