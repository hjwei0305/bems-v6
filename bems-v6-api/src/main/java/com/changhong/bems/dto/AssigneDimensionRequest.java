package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-26 15:47
 */
@ApiModel(description = "分配预算维度DTO")
public class AssigneDimensionRequest implements Serializable {
    private static final long serialVersionUID = -2652295325558282931L;
    /**
     * 预算类型id
     */
    @NotBlank
    @ApiModelProperty(value = "预算类型id")
    private String categoryId;
    /**
     * 维度代码清单
     */
    @NotEmpty
    @ApiModelProperty(value = "维度代码清单")
    private Set<String> dimensionCodes;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Set<String> getDimensionCodes() {
        return dimensionCodes;
    }

    public void setDimensionCodes(Set<String> dimensionCodes) {
        this.dimensionCodes = dimensionCodes;
    }
}
