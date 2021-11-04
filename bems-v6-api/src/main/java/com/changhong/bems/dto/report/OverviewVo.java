package com.changhong.bems.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 实现功能：预算概览分析结果
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-04 17:05
 */
@ApiModel(description = "预算概览分析结果")
public class OverviewVo implements Serializable {
    private static final long serialVersionUID = -5768522763204791692L;

    /**
     * 所属年度
     */
    @ApiModelProperty(value = "所属年度")
    private Integer year;

    /**
     * 可使用
     */
    @ApiModelProperty(value = "可使用")
    private BigDecimal[] balance;
    /**
     * 已使用
     */
    @ApiModelProperty(value = "已使用")
    private BigDecimal[] used;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal[] getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal[] balance) {
        this.balance = balance;
    }

    public BigDecimal[] getUsed() {
        return used;
    }

    public void setUsed(BigDecimal[] used) {
        this.used = used;
    }
}
