package com.changhong.bems.sdk.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * 实现功能：预算占用结果
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:07
 */
public class BudgetUseResult implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;

    /**
     * 占用预算编码
     */
    private String poolCode;
    /**
     * 当前预算池注入总额
     */
    private BigDecimal poolTotalAmount;
    /**
     * 当前预算池已使用额度(不含本次使用)
     */
    private BigDecimal poolUsedAmount;
    /**
     * 当前预算池可用余额(不含本次使用)
     */
    private BigDecimal poolBalanceAmount;
    /**
     * 本次使用金额
     */
    private BigDecimal useAmount;

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public BigDecimal getPoolTotalAmount() {
        return poolTotalAmount;
    }

    public void setPoolTotalAmount(BigDecimal poolTotalAmount) {
        this.poolTotalAmount = poolTotalAmount;
    }

    public BigDecimal getPoolUsedAmount() {
        return poolUsedAmount;
    }

    public void setPoolUsedAmount(BigDecimal poolUsedAmount) {
        this.poolUsedAmount = poolUsedAmount;
    }

    public BigDecimal getPoolBalanceAmount() {
        return poolBalanceAmount;
    }

    public void setPoolBalanceAmount(BigDecimal poolBalanceAmount) {
        this.poolBalanceAmount = poolBalanceAmount;
    }

    public BigDecimal getUseAmount() {
        return useAmount;
    }

    public void setUseAmount(BigDecimal useAmount) {
        this.useAmount = useAmount;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BudgetUseResult.class.getSimpleName() + "[", "]")
                .add("poolCode='" + poolCode + "'")
                .add("poolTotalAmount=" + poolTotalAmount)
                .add("poolUsedAmount=" + poolUsedAmount)
                .add("poolBalanceAmount=" + poolBalanceAmount)
                .add("useAmount=" + useAmount)
                .toString();
    }
}
