package com.changhong.bems.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 实现功能：使用趋势分析
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-08 16:10
 */
@ApiModel(description = "使用趋势结果")
public class UsageTrendVo implements Serializable {
    private static final long serialVersionUID = 927216826723414622L;
    /**
     * 预算年度
     */
    @ApiModelProperty(value = "预算年度")
    private Integer year;
    /**
     * 所属月度
     */
    @ApiModelProperty(value = "所属月度")
    private Integer month;
    /**
     * 额度
     */
    @ApiModelProperty(value = "额度")
    private BigDecimal amount = BigDecimal.ZERO;

    public UsageTrendVo() {
    }

    public UsageTrendVo(Integer year, Integer month, BigDecimal amount) {
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
