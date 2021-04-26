package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.entity.Category;
import com.changhong.bems.entity.Pool;
import com.changhong.bems.entity.Subject;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private SubjectService subjectService;
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
        if (CategoryType.GENERAL == entity.getType()) {
            entity.setSubjectId(CategoryType.GENERAL.name());
        } else if (CategoryType.PRIVATE == entity.getType()) {
            if (StringUtils.isEmpty(entity.getSubjectId())) {
                // 非通用预算类型,预算主体不能为空!
                return OperateResultWithData.operationFailure("category_00002");
            }
        } else {
            // 错误的预算类型分类
            return OperateResultWithData.operationFailure("category_00003");
        }
        return super.preInsert(entity);
    }

    /**
     * 更新数据保存数据之前额外操作回调方法 默认为空逻辑，子类根据需要覆写添加逻辑即可
     *
     * @param entity 待更新数据对象
     */
    @Override
    protected OperateResultWithData<Category> preUpdate(Category entity) {
        if (CategoryType.GENERAL == entity.getType()) {
            entity.setSubjectId(CategoryType.GENERAL.name());
        } else if (CategoryType.PRIVATE == entity.getType()) {
            if (StringUtils.isEmpty(entity.getSubjectId())) {
                // 非通用预算类型,预算主体不能为空!
                return OperateResultWithData.operationFailure("category_00002");
            }
        } else {
            // 错误的预算类型分类
            return OperateResultWithData.operationFailure("category_00003");
        }
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

    /**
     * 创建预算类型
     *
     * @param subjectId 预算主体id
     * @param id        通用预算类型id
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> reference(String subjectId, String id) {
        Subject subject = subjectService.findOne(subjectId);
        if (Objects.isNull(subject)) {
            // 预算主体不存在
            return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
        }

        Category category = dao.findOne(id);
        if (Objects.isNull(category)) {
            // 预算类型不存在
            return ResultData.fail(ContextUtil.getMessage("category_00004", id));
        } else {
            if (CategoryType.GENERAL != category.getType()) {
                // 不是通用预算类型
                return ResultData.fail(ContextUtil.getMessage("category_00005", category.getName()));
            }
        }
        Category privateCategory = new Category();
        privateCategory.setName(category.getName());
        privateCategory.setType(CategoryType.PRIVATE);
        privateCategory.setSubjectId(subjectId);
        privateCategory.setSubjectName(subject.getName());
        privateCategory.setPeriodType(category.getPeriodType());
        privateCategory.setOrderCategory(category.getOrderCategory());
        privateCategory.setUse(category.getUse());
        privateCategory.setRoll(category.getRoll());
        privateCategory.setReferenceId(id);
        this.save(privateCategory);
        return ResultData.success();
    }
}