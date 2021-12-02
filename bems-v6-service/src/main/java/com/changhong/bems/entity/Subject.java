package com.changhong.bems.entity;

import com.changhong.bems.dto.Classification;
import com.changhong.bems.dto.OrganizationDto;
import com.changhong.sei.core.dto.auth.IDataAuthEntity;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.IFrozen;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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
public class Subject extends BaseAuditableEntity implements ITenant, IFrozen, IDataAuthEntity, Serializable {
    private static final long serialVersionUID = 851011858666429840L;
    public static final String FIELD_CORP_CODE = "corporationCode";
    public static final String FIELD_CLASSIFICATION = "classification";
    public static final String FIELD_NAME = "name";
    /**
     * 主体代码
     */
    @Column(name = "code", updatable = false)
    private String code;
    /**
     * 主体名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 公司代码
     */
    @Column(name = "corporation_code", updatable = false)
    private String corporationCode;
    /**
     * 公司名称
     */
    @Column(name = "corporation_name", updatable = false)
    private String corporationName;
    /**
     * 预算分类
     */
    @Column(name = "classification", updatable = false)
    @Enumerated(EnumType.STRING)
    private Classification classification;
    /**
     * 币种代码
     */
    @Column(name = "currency_code", updatable = false)
    private String currencyCode;
    /**
     * 币种名称
     */
    @Column(name = "currency_name", updatable = false)
    private String currencyName;
    /**
     * 执行策略id
     */
    @Column(name = "strategy_id")
    private String strategyId;
    /**
     * 执行策略名称
     */
    @Column(name = "strategy_name")
    private String strategyName;
    /**
     * 排序
     */
    @Column(name = "rank")
    private Integer rank = 0;
    /**
     * 冻结
     */
    @Column(name = "frozen_")
    private Boolean frozen = Boolean.FALSE;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;
    /**
     * 临时字段
     */
    @Transient
    private Set<OrganizationDto> orgList;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
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

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
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

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public Boolean getFrozen() {
        return frozen;
    }

    @Override
    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Set<OrganizationDto> getOrgList() {
        return orgList;
    }

    public void setOrgList(Set<OrganizationDto> orgList) {
        this.orgList = orgList;
    }
}