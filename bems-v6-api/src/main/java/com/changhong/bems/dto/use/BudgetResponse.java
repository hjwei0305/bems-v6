package com.changhong.bems.dto.use;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
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
    @ApiModelProperty(value = "是否占用成功")
    private Boolean success = Boolean.TRUE;
    @ApiModelProperty(value = "占用消息")
    private String message;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BudgetUseResult> getUseResults() {
        return useResults;
    }

    public BudgetResponse setUseResults(List<BudgetUseResult> useResults) {
        this.useResults = useResults;
        return this;
    }

    public void addUseResult(BudgetUseResult result) {
        if (this.useResults == null) {
            this.useResults = new ArrayList<>();
        }
        this.useResults.add(result);
    }
}
