package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-25 17:38
 */
@ApiModel(description = "预算类型DTO")
public class CreateCategoryDto implements Serializable {
    private static final long serialVersionUID = 3878281517404933790L;
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    /**
     * 类型分类
     */
    @NotNull
    @ApiModelProperty(value = "类型分类", required = true)
    private CategoryType type;
    /**
     * 预算主体id
     */
    @Size(max = 36)
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    /**
     * 预算主体名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "预算主体名称", required = true)
    private String subjectName;
    /**
     * 期间类型
     */
    @NotNull
    @ApiModelProperty(value = "期间类型", required = true)
    private PeriodType periodType;
    /**
     * 管理类型(订单类型)
     */
    @NotEmpty
    @ApiModelProperty(value = "管理类型(订单类型)", required = true)
    private OrderCategory[] orderCategories;
    /**
     * 允许使用(业务可用)
     */
    @ApiModelProperty(value = "允许使用(业务可用)")
    private Boolean use = Boolean.FALSE;
    /**
     * 允许结转
     */
    @ApiModelProperty(value = "允许结转")
    private Boolean roll = Boolean.FALSE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public OrderCategory[] getOrderCategories() {
        return orderCategories;
    }

    public void setOrderCategories(OrderCategory[] orderCategories) {
        this.orderCategories = orderCategories;
    }

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public Boolean getRoll() {
        return roll;
    }

    public void setRoll(Boolean roll) {
        this.roll = roll;
    }
}
