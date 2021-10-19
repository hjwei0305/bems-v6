package com.changhong.bems.entity.vo;

import com.changhong.bems.dto.PeriodType;
import com.changhong.bems.entity.BaseAttribute;
import com.changhong.sei.core.entity.ITenant;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预算池(Pool)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
public class PoolAttributeVo extends BaseAttribute implements ITenant, Serializable {
    private static final long serialVersionUID = 345291355065499642L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_CORP_CODE = "corpCode";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_ACTIVE = "actived";
    public static final String FIELD_USE = "use";
    public static final String FIELD_PERIOD_TYPE = "periodType";
    /**
     * 预算池编号
     */
    private String code;
    /**
     * 预算主体id
     */
    private String subjectId;
    /**
     * 公司代码
     */
    private String corpCode;
    /**
     * 币种代码
     */
    private String currencyCode;
    /**
     * 币种名称
     */
    private String currencyName;
    /**
     * 归口管理部门
     */
    private String manageOrg;
    /**
     * 归口管理部门名称
     */
    private String manageOrgName;
    /**
     * 期间分类
     */
    private PeriodType periodType;
    /**
     * 所属年度
     */
    private Integer year;
    /**
     * 起始日期
     */
    private LocalDate startDate;
    /**
     * 截止日期
     */
    private LocalDate endDate;
    /**
     * 执行策略id
     */
    private String strategyId;
    /**
     * 执行策略名称
     */
    private String strategyName;
    /**
     * 是否启用
     */
    private Boolean actived = Boolean.TRUE;
    /**
     * 允许使用(业务可用)
     */
    private Boolean use = Boolean.FALSE;
    /**
     * 允许结转
     */
    private Boolean roll = Boolean.FALSE;
    /**
     * 是否可延期
     */
    private Boolean delay = Boolean.FALSE;
    /**
     * 总额
     */
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /**
     * 使用量
     */
    private BigDecimal usedAmount = BigDecimal.ZERO;
    /**
     * 可用余额
     */
    private BigDecimal balance = BigDecimal.ZERO;
    /**
     * 租户代码
     */
    private String tenantCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCorpCode() {
        return corpCode;
    }

    public PoolAttributeVo setCorpCode(String corpCode) {
        this.corpCode = corpCode;
        return this;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}