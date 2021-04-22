package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 预算维度(Dimension)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:23
 */
@Entity
@Table(name = "dimension")
@DynamicInsert
@DynamicUpdate
public class Dimension extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -99493949797458960L;
    public static final String FIELD_STRATEGY_ID = "strategyId";
    /**
     * 维度代码
     */
    @Column(name = "code")
    private String code;
    /**
     * 维度名称
     */
    @Column(name = "name")
    private String name;
    /**
     * UI组件名
     */
    @Column(name = "ui_component")
    private String uiComponent;
    /**
     * 维度策略id
     */
    @Column(name = "strategy_id")
    private String strategyId;
    /**
     * 维度策略名称
     */
    @Column(name = "strategy_name")
    private String strategyName;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUiComponent() {
        return uiComponent;
    }

    public void setUiComponent(String uiComponent) {
        this.uiComponent = uiComponent;
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

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}