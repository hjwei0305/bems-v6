package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算维度(Dimension)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:24
 */
@ApiModel(description = "预算维度DTO")
public class DimensionDto extends BaseEntityDto {
    private static final long serialVersionUID = 926724246030450088L;
    /**
     * 维度代码
     */
    @ApiModelProperty(value = "维度代码")
    private String code;
    /**
     * 维度名称
     */
    @ApiModelProperty(value = "维度名称")
    private String name;
    /**
     * UI组件名
     */
    @ApiModelProperty(value = "UI组件名")
    private String uiComponent;
    /**
     * 维度策略id
     */
    @ApiModelProperty(value = "维度策略id")
    private String strategyId;
    /**
     * 维度策略名称
     */
    @ApiModelProperty(value = "维度策略名称")
    private String strategyName;
    /**
     * 系统必要
     */
    @ApiModelProperty(value = "系统必要")
    private Boolean required = Boolean.FALSE;
    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
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

    public String getUiComponent() {
        return uiComponent;
    }

    public void setUiComponent(String uiComponent) {
        this.uiComponent = uiComponent;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}