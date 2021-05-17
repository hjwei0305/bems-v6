package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 实现功能：预算占用
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:07
 */
@ApiModel(description = "预算占用结果")
public class BudgetUseResult implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;

    @ApiModelProperty(value = "占用预算编码")
    private String poolCode;
    @ApiModelProperty(value = "占用后预算池余额")
    private double balance;
    @ApiModelProperty(value = "占用金额")
    private double useAmount;

    public BudgetUseResult() {

    }

    public BudgetUseResult(String poolCode, double balance, double useAmount) {
        this.poolCode = poolCode;
        this.balance = balance;
        this.useAmount = useAmount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public double getBalance() {
        return balance;
    }

    public double getUseAmount() {
        return useAmount;
    }
}
