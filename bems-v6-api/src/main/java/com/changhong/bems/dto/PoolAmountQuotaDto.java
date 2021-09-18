package com.changhong.bems.dto;

import java.math.BigDecimal;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-18 00:56
 */
public class PoolAmountQuotaDto {
    /**
     * 预算池代码
     */
    private String poolCode;
    /**
     * 总金额
     */
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /**
     * 使用金额
     */
    private BigDecimal useAmount = BigDecimal.ZERO;

    public PoolAmountQuotaDto() {
    }

    public PoolAmountQuotaDto(String poolCode) {
        this.poolCode = poolCode;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getUseAmount() {
        return useAmount;
    }

    public void setUseAmount(BigDecimal useAmount) {
        this.useAmount = useAmount;
    }

    /**
     * 当前余额
     */
    public BigDecimal getBalance() {
        return totalAmount.subtract(useAmount);
    }

    public void addTotalAmount(BigDecimal amount) {
        totalAmount = totalAmount.add(amount);
    }

    public void addUseAmount(BigDecimal amount) {
        useAmount = useAmount.add(amount);
    }
}
