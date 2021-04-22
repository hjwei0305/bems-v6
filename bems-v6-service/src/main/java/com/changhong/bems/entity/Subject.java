package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 预算主体(Subject)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:25
 */
@Entity
@Table(name = "subject")
@DynamicInsert
@DynamicUpdate
public class Subject extends BaseAuditableEntity implements Serializable {
    private static final long serialVersionUID = 851011858666429840L;
    /**
     * 主体名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 公司代码
     */
    @Column(name = "corporation_code")
    private String corporationCode;
    /**
     * 公司名称
     */
    @Column(name = "corporation_name")
    private String corporationName;
    /**
     * 组织代码
     */
    @Column(name = "org_code")
    private String orgCode;
    /**
     * 组织名称
     */
    @Column(name = "org_name")
    private String orgName;
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
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCorporationCode() {
        return corporationCode;
    }

    public void setCorporationCode(String corporationCode) {
        this.corporationCode = corporationCode;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}