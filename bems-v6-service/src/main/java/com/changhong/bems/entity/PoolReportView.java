package com.changhong.bems.entity;

import com.changhong.sei.core.entity.ITenant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预算报表(PoolReportView)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:27
 */
@Entity
@Table(name = "view_report_pool")
public class PoolReportView extends BaseAttribute implements ITenant, Serializable {
    private static final long serialVersionUID = 345291355065499642L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_CODE = "code";
    /**
     * 预算池编号
     */
    @Column(name = "code")
    private String code;
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 预算主体名称
     */
    @Column(name = "subject_name")
    private String subjectName;
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
     * 所属年度
     */
    @Column(name = "year")
    private Integer year;
    /**
     * 总注入(外部)
     */
    @Column(name = "inject_amount")
    private BigDecimal injectAmount = BigDecimal.ZERO;
    /**
     * 总使用(外部)
     */
    @Column(name = "used_amount")
    private BigDecimal usedAmount = BigDecimal.ZERO;
    /**
     * 总调入(内部)
     */
    @Column(name = "revise_in_amount")
    private BigDecimal reviseInAmount = BigDecimal.ZERO;
    /**
     * 总调出(内部)
     */
    @Column(name = "revise_out_amount")
    private BigDecimal reviseOutAmount = BigDecimal.ZERO;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

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

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getInjectAmount() {
        return injectAmount;
    }

    public void setInjectAmount(BigDecimal injectAmount) {
        this.injectAmount = injectAmount;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }

    public BigDecimal getReviseInAmount() {
        return reviseInAmount;
    }

    public void setReviseInAmount(BigDecimal reviseInAmount) {
        this.reviseInAmount = reviseInAmount;
    }

    public BigDecimal getReviseOutAmount() {
        return reviseOutAmount;
    }

    public void setReviseOutAmount(BigDecimal reviseOutAmount) {
        this.reviseOutAmount = reviseOutAmount;
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