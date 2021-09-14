package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 预算策略(Strategy)DTO类
 *
 * @author sei
 * @since 2021-04-22 11:12:07
 */
@ApiModel(description = "预算策略DTO")
public class StrategyDto extends BaseEntityDto {
    private static final long serialVersionUID = -51089250630553840L;
    /**
     * 策略代码
     */
    @Size(max = 50)
    @ApiModelProperty(value = "策略代码", required = true)
    private String code;
    /**
     * 策略名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "策略名称", required = true)
    private String name;
    /**
     * 策略类路径
     */
    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(value = "策略类路径", required = true)
    private String classPath;
    /**
     * 策略类别
     */
    @NotNull
    @ApiModelProperty(value = "策略类别", required = true)
    private StrategyCategory category;
    /**
     * 策略描述
     */
    @Size(max = 200)
    @ApiModelProperty(value = "策略描述")
    private String remark;
    /**
     * 执行优先级
     */
    @ApiModelProperty(value = "执行优先级(数字越大,优先级越低)")
    private Integer rank = 0;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public StrategyCategory getCategory() {
        return category;
    }

    public void setCategory(StrategyCategory category) {
        this.category = category;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}