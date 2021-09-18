package com.changhong.bems.sdk.dto;

import java.io.Serializable;
import java.math.BigDecimal;

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
     * 占用金额
     */
    private BigDecimal useAmount;

    public BudgetUseResult() {
    }

    public BudgetUseResult(String poolCode, BigDecimal useAmount) {
        this.poolCode = poolCode;
        this.useAmount = useAmount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public BigDecimal getUseAmount() {
        return useAmount;
    }

    public void setUseAmount(BigDecimal useAmount) {
        this.useAmount = useAmount;
    }
}