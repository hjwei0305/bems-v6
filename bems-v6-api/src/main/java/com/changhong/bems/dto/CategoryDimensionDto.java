package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算类型维度关系(CategoryDimension)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:25
 */
@ApiModel(description = "预算类型维度关系DTO")
public class CategoryDimensionDto extends BaseEntityDto {
    private static final long serialVersionUID = -64167950231296018L;
    /**
     * 预算类型id
     */
    @ApiModelProperty(value = "预算类型id")
    private String categoryId;
    /**
     * 预算维度代码
     */
    @ApiModelProperty(value = "预算维度代码")
    private String dimensionCode;
    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private Integer rank = 0;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public void setDimensionCode(String dimensionCode) {
        this.dimensionCode = dimensionCode;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

}