package com.changhong.bems.controller;

import com.changhong.bems.api.CategoryApi;
import com.changhong.bems.dto.CategoryDto;
import com.changhong.bems.dto.CreateCategoryDto;
import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.entity.Category;
import com.changhong.bems.service.CategoryService;
import com.changhong.sei.core.controller.BaseEntityController;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.service.BaseEntityService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 预算类型(Category)控制类
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@RestController
@Api(value = "CategoryApi", tags = "预算类型服务")
@RequestMapping(path = CategoryApi.PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController extends BaseEntityController<Category, CategoryDto> implements CategoryApi {
    /**
     * 预算类型服务对象
     */
    @Autowired
    private CategoryService service;

    @Override
    public BaseEntityService<Category> getService() {
        return service;
    }

    /**
     * 创建预算类型
     *
     * @param dto 业务实体DTO
     * @return 操作结果
     */
    @Override
    public ResultData<Void> create(CreateCategoryDto dto) {
        try {
            List<Category> categoryList = new ArrayList<>();
            OrderCategory[] categories = dto.getOrderCategories();
            for (OrderCategory category : categories) {
                Category entity = entityModelMapper.map(dto, Category.class);
                entity.setOrderCategory(category);
                categoryList.add(entity);
            }
            service.save(categoryList);
            return ResultData.success();
        } catch (Exception e) {
            return ResultData.fail(e.getMessage());
        }
    }

    /**
     * 查询通用预算类型
     *
     * @return 查询结果
     */
    @Override
    public ResultData<List<CategoryDto>> findByGeneral() {
        return ResultData.success(convertToDtos(service.findByGeneral()));
    }

    /**
     * 根据预算主体查询私有预算类型
     *
     * @param subjectId 预算主体id
     * @return 分页查询结果
     */
    @Override
    public ResultData<List<CategoryDto>> findBySubject(String subjectId) {
        return ResultData.success(convertToDtos(service.findBySubject(subjectId)));
    }

    /**
     * 创建预算类型
     *
     * @param subjectId 预算主体id
     * @param id        通用预算类型id
     * @return 操作结果
     */
    @Override
    public ResultData<Void> reference(String subjectId, String id) {
        return service.reference(subjectId, id);
    }
}