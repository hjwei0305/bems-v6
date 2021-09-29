package com.changhong.bems.service;

import com.changhong.bems.dao.CategoryDao;
import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.dto.DimensionDto;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.*;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.service.BaseEntityService;
import com.changhong.sei.core.service.Validation;
import com.changhong.sei.core.service.bo.OperateResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
@CacheConfig(cacheNames = CategoryService.CACHE_KEY)
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
    @Autowired
    private CategoryConfigService categoryConfigService;

    public static final String CACHE_KEY = "bems-v6:category:dimension";

    @Override
    protected BaseEntityDao<Category> getDao() {
        return dao;
    }

    /**
     * 主键删除
     *
     * @param id 主键
     * @return 返回操作结果对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResult delete(String id) {
        Category category = dao.findOne(id);
        if (Objects.isNull(category)) {
            return OperateResult.operationFailure("category_00004", id);
        }
        Order order = orderService.findFirstByProperty(Order.FIELD_CATEGORY_ID, id);
        if (Objects.nonNull(order)) {
            // 已被使用,禁止删除!
            return OperateResult.operationFailure("category_00001");
        }
        dao.delete(category);
        return OperateResult.operationSuccess("core_service_00028");
    }

    /**
     * 数据保存操作
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Category> saveOrUpdate(Category entity, OrderCategory[] orderCategories) {
        Validation.notNull(entity, "持久化对象不能为空");
        if (CategoryType.GENERAL == entity.getType()) {
            entity.setSubjectId(CategoryType.GENERAL.name());
            entity.setSubjectName(CategoryType.GENERAL.name());
        } else if (CategoryType.PRIVATE == entity.getType()) {
            if (StringUtils.isEmpty(entity.getSubjectId())) {
                // 非通用预算类型,预算主体不能为空!
                return ResultData.fail(ContextUtil.getMessage("category_00002"));
            }
        } else {
            // 错误的预算类型分类
            return ResultData.fail(ContextUtil.getMessage("category_00003"));
        }

        Search search = Search.createSearch();
        boolean isNew = isNew(entity);
        if (isNew) {
            // 创建前设置租户代码
            if (StringUtils.isBlank(entity.getTenantCode())) {
                entity.setTenantCode(ContextUtil.getTenantCode());
            }
        } else {
            search.addFilter(new SearchFilter(Category.ID, entity.getId(), SearchFilter.Operator.NE));
        }
        search.addFilter(new SearchFilter(Category.FIELD_SUBJECT_ID, entity.getSubjectId()));
        search.addFilter(new SearchFilter(Category.FIELD_NAME, entity.getName()));
        Category existed = dao.findFirstByFilters(search);
        if (Objects.nonNull(existed)) {
            // 已存在预算类型
            return ResultData.fail(ContextUtil.getMessage("category_00006", existed.getName()));
        }

        Category saveEntity = dao.save(entity);
        if (isNew) {
            if (CategoryType.PRIVATE == entity.getType()
                    && StringUtils.isNotBlank(entity.getReferenceId()) && !"none".equals(entity.getReferenceId())) {
                // 引用的
                categoryDimensionService.addReferenceDimension(entity.getId(), entity.getReferenceId());
            } else {
                // 新增添加必须的维度
                categoryDimensionService.addRequiredDimension(entity.getId());
            }
        }
        // 更新订单配置
        categoryConfigService.putConfigData(entity.getId(), entity.getPeriodType(), orderCategories);
        return ResultData.success(saveEntity);
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
     * 根据预算主体查询私有预算类型
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    public List<Dimension> findDimensionBySubject(String subjectId) {
        List<Category> categoryList = this.findBySubject(subjectId);
        Set<String> categoryIds = categoryList.stream().map(Category::getId).collect(Collectors.toSet());
        return categoryDimensionService.getDimensionCodeByCategory(categoryIds);
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
        privateCategory.setUse(category.getUse());
        privateCategory.setRoll(category.getRoll());
        privateCategory.setReferenceId(id);
        // 获取当前预算类型支持的订单类型
        OrderCategory[] orderCategories = categoryConfigService.findPeriodTypes(id);
        ResultData<Category> result = this.saveOrUpdate(privateCategory, orderCategories);
        if (result.successful()) {
            return ResultData.success();
        } else {
            return ResultData.fail(result.getMessage());
        }
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
    @Cacheable(key = "#categoryId")
    public List<DimensionDto> getAssigned(String categoryId) {
        List<DimensionDto> list = new ArrayList<>();
        List<CategoryDimension> categoryDimensions = categoryDimensionService.getByCategoryId(categoryId);
        if (CollectionUtils.isNotEmpty(categoryDimensions)) {
            DimensionDto dto;
            List<Dimension> dimensionList = dimensionService.findAll();
            Map<String, Dimension> dimensionMap = dimensionList.stream().collect(Collectors.toMap(Dimension::getCode, d -> d));

            for (CategoryDimension cd : categoryDimensions) {
                Dimension dimension = dimensionMap.get(cd.getDimensionCode());
                if (Objects.nonNull(dimension)) {
                    dto = new DimensionDto();
                    dto.setCode(dimension.getCode());
                    dto.setName(dimension.getName());
                    dto.setStrategyId(dimension.getStrategyId());
                    dto.setStrategyName(dimension.getStrategyName());
                    dto.setUiComponent(dimension.getUiComponent());
                    dto.setRequired(dimension.getRequired());
                    dto.setRank(cd.getRank());
                    list.add(dto);
                }
            }
            // 排序
            list.sort(Comparator.comparing(DimensionDto::getRank));
        }
        return list;
    }

    /**
     * 为指定预算类型分配预算维度
     *
     * @return 分配结果
     */
    @CacheEvict(key = "#categoryId")
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> assigne(String categoryId, Set<String> dimensionCodes) {
        List<CategoryDimension> dimensionList = new ArrayList<>();
        CategoryDimension categoryDimension;
        for (String code : dimensionCodes) {
            Dimension dimension = dimensionService.findByCode(code);
            if (Objects.isNull(dimension)) {
                // 维度不存在
                return ResultData.fail(ContextUtil.getMessage("dimension_00002", code));
            }
            categoryDimension = new CategoryDimension();
            categoryDimension.setCategoryId(categoryId);
            categoryDimension.setDimensionCode(dimension.getCode());
            categoryDimension.setRank(dimension.getRank());
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
    @CacheEvict(key = "#categoryId")
    @Transactional(rollbackFor = Exception.class)
    public ResultData<Void> unassigne(String categoryId, Set<String> dimensionCodes) {
        List<CategoryDimension> dimensionList = categoryDimensionService.getCategoryDimensions(categoryId, dimensionCodes);
        if (CollectionUtils.isNotEmpty(dimensionList)) {
            Set<String> ids = dimensionList.stream().map(CategoryDimension::getId).collect(Collectors.toSet());
            categoryDimensionService.delete(ids);
        }
        return ResultData.success();
    }

    /**
     * 通过订单类型获取预算类型
     *
     * @param category 订单类型
     * @return 业务实体
     */
    public List<Category> getByCategory(String subjectId, OrderCategory category) {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Category.FIELD_SUBJECT_ID, subjectId));
        search.addFilter(new SearchFilter(Category.FIELD_TYPE, CategoryType.PRIVATE));
        search.addFilter(new SearchFilter(Category.FROZEN, Boolean.FALSE));
        List<Category> privateList = dao.findByFilters(search);

        search.clearAll();
        search.addFilter(new SearchFilter(Category.FIELD_TYPE, CategoryType.GENERAL));
        search.addFilter(new SearchFilter(Category.FROZEN, Boolean.FALSE));
        List<Category> generalList = dao.findByFilters(search);
        List<Category> categoryList = new ArrayList<>();
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
        Set<String> ids = categoryList.stream().map(Category::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(ids)) {
            // 按预算类型id清单和订单类型获取配置的预算期间
            Set<PeriodType> periodTypeSet = categoryConfigService.findPeriodTypes(ids, category);
            // 过滤满足配置条件的预算类型
            categoryList = categoryList.stream().filter(c -> periodTypeSet.contains(c.getPeriodType())).collect(Collectors.toList());
        }

        return categoryList;
    }
}