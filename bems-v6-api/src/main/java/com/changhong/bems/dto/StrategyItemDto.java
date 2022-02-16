package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 执行策略(StrategyItem)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@ApiModel(description = "科目执行策略DTO")
public class StrategyItemDto extends BudgetItemDto {
    private static final long serialVersionUID = -85112390830826629L;

    /**
     * 预算主体id
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    /**
     * 执行策略id
     */
    @Size(max = 50)
    @ApiModelProperty(value = "执行策略id")
    private String strategyId;
    /**
     * 执行策略名称
     */
    @ApiModelProperty(value = "执行策略名称")
    private String strategyName;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

}