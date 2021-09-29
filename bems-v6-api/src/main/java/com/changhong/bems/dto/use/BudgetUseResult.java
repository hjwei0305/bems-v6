package com.changhong.bems.dto.use;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

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
    private final String poolCode;
    /**
     * 总额
     */
    @ApiModelProperty(value = "当前预算池注入总额")
    private final BigDecimal poolTotalAmount;
    /**
     * 当前预算池已使用额度(不含本次使用)
     */
    @ApiModelProperty(value = "当前预算池已使用额度(不含本次使用)")
    private final BigDecimal poolUsedAmount;
    /**
     * 当前预算池可用余额(不含本次使用)
     */
    @ApiModelProperty(value = "当前预算池可用余额(不含本次使用)")
    private final BigDecimal poolBalanceAmount;

    @ApiModelProperty(value = "本次使用金额")
    private final BigDecimal useAmount;

    @ApiModelProperty(value = "预算池显示信息")
    private String display;

    public BudgetUseResult(String poolCode, BigDecimal poolTotalAmount, BigDecimal poolUsedAmount,
                           BigDecimal poolBalanceAmount, BigDecimal useAmount) {
        this.poolCode = poolCode;
        this.poolTotalAmount = poolTotalAmount;
        this.poolUsedAmount = poolUsedAmount;
        this.poolBalanceAmount = poolBalanceAmount;
        this.useAmount = useAmount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public BigDecimal getPoolTotalAmount() {
        return poolTotalAmount;
    }

    public BigDecimal getPoolUsedAmount() {
        return poolUsedAmount;
    }

    public BigDecimal getPoolBalanceAmount() {
        return poolBalanceAmount;
    }

    public BigDecimal getUseAmount() {
        return useAmount;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
