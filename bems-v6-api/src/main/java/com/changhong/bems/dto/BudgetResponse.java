package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 实现功能：预算占用结果
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:07
 */
@ApiModel(description = "预算占用结果")
public class BudgetResponse implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;

    /**
     * 业务id
     */
    @ApiModelProperty(value = "业务id")
    private String bizId;
    /**
     * 预算占用结果
     */
    @ApiModelProperty(value = "预算占用结果")
    private List<BudgetUseResult> useResults;

    public String getBizId() {
        return bizId;
    }

    public BudgetResponse setBizId(String bizId) {
        this.bizId = bizId;
        return this;
    }

    public List<BudgetUseResult> getUseResults() {
        return useResults;
    }

    public BudgetResponse setUseResults(List<BudgetUseResult> useResults) {
        this.useResults = useResults;
        return this;
    }
}
