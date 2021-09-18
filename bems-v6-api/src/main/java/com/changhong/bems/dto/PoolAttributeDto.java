package com.changhong.bems.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预算池维度(Pool)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@ApiModel(description = "预算池维度DTO")
public class PoolAttributeDto extends BaseAttributeDto {
    private static final long serialVersionUID = -26673484823027470L;
    /**
     * 代码
     */
    @ApiModelProperty(value = "代码")
    private String code;
    /**
     * 预算主体id
     */
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 预算维度属性id
     */
    @ApiModelProperty(value = "预算维度属性id")
    private String attributeId;
    /**
     * 币种代码
     */
    @ApiModelProperty(value = "币种代码")
    private String currencyCode;
    /**
     * 币种名称
     */
    @ApiModelProperty(value = "币种名称")
    private String currencyName;
    /**
     * 归口管理部门
     */
    @ApiModelProperty(value = "归口管理部门")
    private String manageOrg;
    /**
     * 归口管理部门名称
     */
    @ApiModelProperty(value = "归口管理部门名称")
    private String manageOrgName;
    /**
     * 期间分类
     */
    @ApiModelProperty(value = "期间分类")
    private PeriodType periodType;
    /**
     * 所属年度
     */
    @ApiModelProperty(value = "所属年度")
    private Integer year;
    /**
     * 起始日期
     */
    @ApiModelProperty(value = "起始日期", example = "2021-04-22")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    /**
     * 截止日期
     */
    @ApiModelProperty(value = "截止日期", example = "2021-04-22")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    /**
     * 执行策略id
     */
    @ApiModelProperty(value = "执行策略id")
    private String strategyId;
    /**
     * 执行策略名称
     */
    @ApiModelProperty(value = "执行策略名称")
    private String strategyName;
    /**
     * 是否启用
     */
    @ApiModelProperty(value = "是否启用")
    private Boolean actived;
    /**
     * 允许使用(业务可用)
     */
    @ApiModelProperty(value = "允许使用(业务可用)")
    private Boolean use;
    /**
     * 允许结转
     */
    @ApiModelProperty(value = "允许结转")
    private Boolean roll;
    /**
     * 是否可延期
     */
    @ApiModelProperty(value = "是否可延期")
    private Boolean delay;
    /**
     * 总额
     */
    @ApiModelProperty(value = "总额")
    private BigDecimal totalAmount;
    /**
     * 已用金额
     */
    @ApiModelProperty(value = "已用金额")
    private BigDecimal usedAmount;
    /**
     * 可用余额
     */
    @ApiModelProperty(value = "可用余额")
    private BigDecimal balance;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getManageOrg() {
        return manageOrg;
    }

    public void setManageOrg(String manageOrg) {
        this.manageOrg = manageOrg;
    }

    public String getManageOrgName() {
        return manageOrgName;
    }

    public void setManageOrgName(String manageOrgName) {
        this.manageOrgName = manageOrgName;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Boolean getActived() {
        return actived;
    }

    public void setActived(Boolean actived) {
        this.actived = actived;
    }

    public Boolean getUse() {
        return use;
    }

    public void setUse(Boolean use) {
        this.use = use;
    }

    public Boolean getRoll() {
        return roll;
    }

    public void setRoll(Boolean roll) {
        this.roll = roll;
    }

    public Boolean getDelay() {
        return delay;
    }

    public void setDelay(Boolean delay) {
        this.delay = delay;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}