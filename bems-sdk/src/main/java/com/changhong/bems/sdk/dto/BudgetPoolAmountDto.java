package com.changhong.bems.sdk.dto;

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
public class BudgetPoolAmountDto implements Serializable {
    private static final long serialVersionUID = -3676168862062617480L;
    /**
     * 占用预算编码
     */
    private String poolCode;
    /**
     * 预算池注入总额
     */
    private BigDecimal totalAmount;
    /**
     * 当前预算池已使用额度
     */
    private BigDecimal usedAmount;
    /**
     * 当前预算池可用余额
     */
    private BigDecimal balanceAmount;
    /**
     * 预算期间
     */
    private String period;
    /**
     * 预算期间名称
     */
    private String periodName;
    /**
     * 预算科目
     */
    private String item;
    /**
     * 预算科目名称
     */
    private String itemName;
    /**
     * 组织
     */
    private String org;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 项目
     */
    private String project;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 自定义1
     */
    private String udf1;
    /**
     * 自定义1名称
     */
    private String udf1Name;
    /**
     * 自定义2
     */
    private String udf2;
    /**
     * 自定义2名称
     */
    private String udf2Name;
    /**
     * 自定义3
     */
    private String udf3;
    /**
     * 自定义3名称
     */
    private String udf3Name;
    /**
     * 自定义4
     */
    private String udf4;
    /**
     * 自定义4名称
     */
    private String udf4Name;
    /**
     * 自定义5
     */
    private String udf5;
    /**
     * 自定义5名称
     */
    private String udf5Name;
    /**
     * 预算池显示信息
     */
    private String display;

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

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf1Name() {
        return udf1Name;
    }

    public void setUdf1Name(String udf1Name) {
        this.udf1Name = udf1Name;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf2Name() {
        return udf2Name;
    }

    public void setUdf2Name(String udf2Name) {
        this.udf2Name = udf2Name;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getUdf3Name() {
        return udf3Name;
    }

    public void setUdf3Name(String udf3Name) {
        this.udf3Name = udf3Name;
    }

    public String getUdf4() {
        return udf4;
    }

    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    public String getUdf4Name() {
        return udf4Name;
    }

    public void setUdf4Name(String udf4Name) {
        this.udf4Name = udf4Name;
    }

    public String getUdf5() {
        return udf5;
    }

    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }

    public String getUdf5Name() {
        return udf5Name;
    }

    public void setUdf5Name(String udf5Name) {
        this.udf5Name = udf5Name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        StringJoiner display = new StringJoiner("|")
                // 期间
                .add(periodName)
                // 科目
                .add(itemName);
        // 组织
        if (StringUtils.isNotBlank(orgName)) {
            display.add(orgName);
        }
        // 项目
        if (StringUtils.isNotBlank(projectName)) {
            display.add(projectName);
        }
        // UDF1
        if (StringUtils.isNotBlank(udf1Name)) {
            display.add(udf1Name);
        }
        // UDF2
        if (StringUtils.isNotBlank(udf2Name)) {
            display.add(udf2Name);
        }
        // UDF3
        if (StringUtils.isNotBlank(udf3Name)) {
            display.add(udf3Name);
        }
        // UDF4
        if (StringUtils.isNotBlank(udf4Name)) {
            display.add(udf4Name);
        }
        // UDF5
        if (StringUtils.isNotBlank(udf5Name)) {
            display.add(udf5Name);
        }
        return display.toString();
    }
}
