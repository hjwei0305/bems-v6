package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * 实现功能：预算释放
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 16:37
 */
@ApiModel(description = "预算释放DTO")
public class BudgetFree implements Serializable {
    private static final long serialVersionUID = 176718134255627368L;

    /**
     * 业务事件
     */
    @NotBlank
    @ApiModelProperty(value = "业务事件", required = true)
    private String eventCode;
    /**
     * 业务id
     */
    @NotBlank
    @ApiModelProperty(value = "业务id", required = true)
    private String bizId;
    /**
     * 业务描述
     */
    @Size(max = 200)
    @ApiModelProperty(value = "业务描述")
    private String bizRemark;
    /**
     * 释放金额
     * 如果等于0,则释放上次全部占用金额;如果不为0,则按指定金额释放预算
     */
    // @Digits(integer = 36, fraction = 2)
    @ApiModelProperty(value = "释放金额", example = "如果等于0,则释放上次全部占用金额;如果不为0,则按指定金额释放预算")
    private BigDecimal amount = BigDecimal.ZERO;

    public BudgetFree() {
    }

    public BudgetFree(String eventCode, String bizId) {
        this.eventCode = eventCode;
        this.bizId = bizId;
    }

    public String getEventCode() {
        return eventCode;
    }

    public BudgetFree setEventCode(String eventCode) {
        this.eventCode = eventCode;
        return this;
    }

    public String getBizId() {
        return bizId;
    }

    public BudgetFree setBizId(String bizId) {
        this.bizId = bizId;
        return this;
    }

    public String getBizRemark() {
        return bizRemark;
    }

    public void setBizRemark(String bizRemark) {
        this.bizRemark = bizRemark;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BudgetFree setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BudgetFree.class.getSimpleName() + "[", "]")
                .add("eventCode='" + eventCode + "'")
                .add("bizId='" + bizId + "'")
                .add("bizRemark='" + bizRemark + "'")
                .add("amount=" + amount)
                .toString();
    }
}
