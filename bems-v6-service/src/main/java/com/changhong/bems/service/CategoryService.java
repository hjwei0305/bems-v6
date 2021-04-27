package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.Validation;
import com.changhong.sei.core.service.bo.OperateResult;
import com.changhong.sei.core.service.bo.OperateResultWithData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private OrderService orderService;
    @Autowired
    private CategoryDimensionService categoryDimensionService;
    @Autowired
    private DimensionService dimensionService;

    @Override
    protected BaseEntityDao<Category> getDao() {
        return dao;
    }

    /**
     * 删除数据保存数据之前额外操作回调方法 子类根据需要覆写添加逻辑即可
     *
     * @param id 待删除数据对象主键
     */
    @Override
    protected OperateResult preDelete(String id) {
        Category category = dao.findOne(id);
        if (Objects.isNull(category)) {
            return OperateResult.operationFailure("category_00004", id);
        }
        if (category.getReferenced()) {
            // 预算类型已被使用,不允许删除
            return OperateResult.operationFailure("category_00001");
        }
        Order order = orderService.findFirstByProperty(Order.FIELD_CATEGORY_ID, id);
        if (Objects.nonNull(order)) {
            // 已被使用,禁止删除!
            return OperateResult.operationFailure("category_00001");
        }
        return OperateResult.operationSuccess();
    }

    /**
     * 数据保存操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResultWithData<Category> save(Category entity) {
        Validation.notNull(entity, "持久化对象不能为空");
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

        boolean isNew = isNew(entity);
        if (isNew) {
            // 创建前设置租户代码
            if (StringUtils.isBlank(entity.getTenantCode())) {
                entity.setTenantCode(ContextUtil.getTenantCode());
            }
        } else {
            Category category = dao.findOne(entity.getId());
            if (category.getReferenced()) {
                // 预算类型已被使用,不允许修改
                return OperateResultWithData.operationFailure("category_00006");
            }
        }
        Category saveEntity = dao.save(entity);
        if (isNew) {
            categoryDimensionService.addRequiredDimension(entity.getId());
            return OperateResultWithData.operationSuccessWithData(saveEntity, "core_service_00026");
        } else {
            return OperateResultWithData.operationSuccessWithData(saveEntity, "core_service_00027");
        }
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
    public List<Category> findBySubject(String subjectId) {
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
                    categoryList.addAll(generalList.stream().filter(c -> !ids.contains(c.getId())).collect(Collectors.toList()));
                } else {
                    categoryList.addAll(generalList);
                }
            }
            categoryList.addAll(privateList);
        }
        return categoryList;
    }

    /**
     * 引用通用预算类型
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

        category.setReferenced(Boolean.TRUE);
        this.save(category);
        return ResultData.success();
    }

    /**
     * 冻结/解冻预算类型
     *
     * @param id 预算类型id
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> frozen(String id, boolean frozen) {
        Category category = dao.findOne(id);
        if (Objects.isNull(category)) {
            // 预算类型不存在
            return ResultData.fail(ContextUtil.getMessage("category_00004", id));
        }
        category.setFrozen(frozen);
        this.save(category);
        return ResultData.success();
    }

    /**
     * 获取未分配的预算维度
     *
     * @param categoryId 预算类型
     * @return 子实体清单
     */
    public List<Dimension> getUnassigned(String categoryId) {
        List<Dimension> dimensionList = dimensionService.findAll();
        List<CategoryDimension> categoryDimensions = categoryDimensionService.getByCategoryId(categoryId);
        if (CollectionUtils.isNotEmpty(categoryDimensions)) {
            Set<String> codes = categoryDimensions.stream().map(CategoryDimension::getDimensionCode).collect(Collectors.toSet());
            return dimensionList.stream().filter(d -> !codes.contains(d.getCode())).collect(Collectors.toList());
        } else {
            return dimensionList;
        }
    }

    /**
     * 获取已分配的预算维度
     *
     * @param categoryId 预算类型
     * @return 子实体清单
     */
    public List<Dimension> getAssigned(String categoryId) {
        List<CategoryDimension> categoryDimensions = categoryDimensionService.getByCategoryId(categoryId);
        Set<String> codes = categoryDimensions.stream().map(CategoryDimension::getDimensionCode).collect(Collectors.toSet());
        List<Dimension> list = dimensionService.findByCodes(codes);
        if (CollectionUtils.isNotEmpty(list)) {
            // 必要维度排序在前
            list.sort(Comparator.comparing(Dimension::getRequired).reversed());
        }
        return list;
    }

    /**
     * 为指定预算类型分配预算维度
     *
     * @return 分配结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> assigne(String categoryId, Set<String> dimensionCodes) {
        Category category = dao.findOne(categoryId);
        if (category.getReferenced()) {
            // 预算类型已被使用,不允许修改
            return ResultData.fail("category_00006");
        }
        List<CategoryDimension> dimensionList = new ArrayList<>();
        CategoryDimension categoryDimension;
        for (String code : dimensionCodes) {
            categoryDimension = new CategoryDimension();
            categoryDimension.setCategoryId(categoryId);
            categoryDimension.setDimensionCode(code);
            dimensionList.add(categoryDimension);
        }
        categoryDimensionService.save(dimensionList);
        return ResultData.success();
    }

    /**
     * 解除预算类型与维度分配关系
     *
     * @return 分配结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> unassigne(String categoryId, Set<String> dimensionCodes) {
        Category category = dao.findOne(categoryId);
        if (category.getReferenced()) {
            // 预算类型已被使用,不允许修改
            return ResultData.fail("category_00006");
        }
        List<CategoryDimension> dimensionList = categoryDimensionService.getCategoryDimensions(categoryId, dimensionCodes);
        if (CollectionUtils.isNotEmpty(dimensionList)) {
            Set<String> ids = dimensionList.stream().map(CategoryDimension::getId).collect(Collectors.toSet());
            categoryDimensionService.delete(ids);
        }
        return ResultData.success();
    }
}