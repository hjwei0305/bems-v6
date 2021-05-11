package com.changhong.bems.entity;

import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ICodeUnique;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 预算池(Pool)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@Entity
@Table(name = "pool")
@DynamicInsert
@DynamicUpdate
public class Pool extends BaseAuditableEntity implements ITenant, ICodeUnique, Serializable {
    private static final long serialVersionUID = 345291355065499642L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_ATTRIBUTE_CODE = "attributeCode";
    public static final String FIELD_ACTIVED = "actived";
    /**
     * 代码
     */
    @Column(name = "code")
    private String code;
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 预算维度属性code
     */
    @Column(name = "attribute_code")
    private Long attributeCode;
    /**
     * 币种代码
     */
    @Column(name = "currency_code")
    private String currencyCode;
    /**
     * 币种名称
     */
    @Column(name = "currency_name")
    private String currencyName;
    /**
     * 归口管理部门
     */
    @Column(name = "manage_org")
    private String manageOrg;
    /**
     * 归口管理部门名称
     */
    @Column(name = "manage_org_name")
    private String manageOrgName;
    /**
     * 期间分类
     */
    @Column(name = "period_category")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
    /**
     * 起始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;
    /**
     * 截止日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;
    /**
     * 是否启用
     */
    @Column(name = "is_actived")
    private Boolean actived = Boolean.TRUE;
    /**
     * 允许使用(业务可用)
     */
    @Column(name = "is_use")
    private Boolean use = Boolean.FALSE;
    /**
     * 允许结转
     */
    @Column(name = "is_roll")
    private Boolean roll = Boolean.FALSE;
    /**
     * 是否可延期
     */
    @Column(name = "is_delay")
    private Boolean delay = Boolean.FALSE;
    /**
     * 可用余额
     */
    @Column(name = "balance")
    private Double balance = 0d;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Long getAttributeCode() {
        return attributeCode;
    }

    public void setAttributeCode(Long attributeCode) {
        this.attributeCode = attributeCode;
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

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
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