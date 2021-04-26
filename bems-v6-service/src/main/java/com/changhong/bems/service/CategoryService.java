package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 预算类型(Category)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@Service
public class CategoryService extends BaseEntityService<Category> {
    @Autowired
    private CategoryDao dao;
    @Autowired
    private PoolService poolService;

    @Override
    protected BaseEntityDao<Category> getDao() {
        return dao;
    }

    /**
     * 创建数据保存数据之前额外操作回调方法 默认为空逻辑，子类根据需要覆写添加逻辑即可
     *
     * @param entity 待创建数据对象
     */
    @Override
    protected OperateResultWithData<Category> preInsert(Category entity) {
        return super.preInsert(entity);
    }

    /**
     * 更新数据保存数据之前额外操作回调方法 默认为空逻辑，子类根据需要覆写添加逻辑即可
     *
     * @param entity 待更新数据对象
     */
    @Override
    protected OperateResultWithData<Category> preUpdate(Category entity) {
        return super.preUpdate(entity);
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        Pool pool = poolService.findFirstByProperty(Pool.ID, id);
        if (Objects.nonNull(pool)) {
            // 已被使用,禁止删除!
            return OperateResult.operationFailure("category_00001");
        }
        return OperateResult.operationSuccess();
    }

    /**
     * 查询通用预算类型
     *
     * @return 查询结果
     */
    public List<Category> findByGeneral() {
        return dao.findListByProperty(Category.FIELD_TYPE, CategoryType.GENERAL);
    }

    /**
     * 根据预算主体查询私有预算类型
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<Category> findPrivate(String subjectId) {
        List<Category> categoryList = new ArrayList<>();
        List<Category> generalList = findByGeneral();
        List<Category> privateList = dao.findListByProperty(Category.FIELD_SUBJECT_ID, subjectId);
        if (CollectionUtils.isEmpty(privateList)) {
            if (CollectionUtils.isNotEmpty(generalList)) {
                categoryList.addAll(generalList);
            }
        } else {
            if (CollectionUtils.isNotEmpty(generalList)) {
                Set<String> ids = privateList.stream().map(Category::getReferenceId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(ids)) {
                    categoryList.addAll(generalList.stream().filter(c -> ids.contains(c.getId())).collect(Collectors.toList()));
                } else {
                    categoryList.addAll(generalList);
                }
            }
            categoryList.addAll(privateList);
        }
        return categoryList;
    }
}