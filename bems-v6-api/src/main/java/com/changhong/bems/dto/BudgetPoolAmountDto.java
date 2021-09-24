package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * 实现功能：预算池金额
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:07
 */
@ApiModel(description = "预算池金额")
public class BudgetPoolAmountDto extends BaseAttributeDto implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;

    @ApiModelProperty(value = "占用预算编码")
    private final String poolCode;
    /**
     * 总额
     */
    @ApiModelProperty(value = "预算池注入总额")
    private final BigDecimal totalAmount;
    /**
     * 当前预算池已使用额度
     */
    @ApiModelProperty(value = "当前预算池已使用额度")
    private final BigDecimal usedAmount;
    /**
     * 当前预算池可用余额
     */
    @ApiModelProperty(value = "当前预算池可用余额")
    private final BigDecimal balanceAmount;

    @ApiModelProperty(value = "预算池显示信息")
    private String display;

    public BudgetPoolAmountDto(String poolCode, BigDecimal totalAmount,
                               BigDecimal usedAmount, BigDecimal balanceAmount) {
        this.poolCode = poolCode;
        this.totalAmount = totalAmount;
        this.usedAmount = usedAmount;
        this.balanceAmount = balanceAmount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public String getDisplay() {
        return this.toString();
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        StringJoiner display = new StringJoiner("|")
                // 期间
                .add(getPeriodName())
                // 科目
                .add(getItemName());
        // 组织
        if (StringUtils.isNotBlank(getOrgName())) {
            display.add(getOrgName());
        }
        // 项目
        if (StringUtils.isNotBlank(getProjectName())) {
            display.add(getProjectName());
        }
        // UDF1
        if (StringUtils.isNotBlank(getUdf1Name())) {
            display.add(getUdf1Name());
        }
        // UDF2
        if (StringUtils.isNotBlank(getUdf2Name())) {
            display.add(getUdf2Name());
        }
        // UDF3
        if (StringUtils.isNotBlank(getUdf3Name())) {
            display.add(getUdf3Name());
        }
        // UDF4
        if (StringUtils.isNotBlank(getUdf4Name())) {
            display.add(getUdf4Name());
        }
        // UDF5
        if (StringUtils.isNotBlank(getUdf5Name())) {
            display.add(getUdf5Name());
        }
        return display.toString();
    }
}
