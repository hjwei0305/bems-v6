package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 * 预算主体(Subject)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@ApiModel(description = "批量创建预算主体DTO")
public class SubjectBatchCreate implements Serializable {
    private static final long serialVersionUID = -52007275362348933L;

    /**
     * 公司代码
     */
    @NotEmpty
    @ApiModelProperty(value = "公司代码清单", required = true)
    private Set<String> corpCodes;
    /**
     * 预算分类
     */
    @NotNull
    @ApiModelProperty(value = "预算分类", required = true)
    private Classification classification;
    /**
     * 执行策略id
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "执行策略id", required = true)
    private String strategyId;

    public Set<String> getCorpCodes() {
        return corpCodes;
    }

    public void setCorpCodes(Set<String> corpCodes) {
        this.corpCodes = corpCodes;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }
}