package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.IFrozen;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 执行策略(SubjectItem)实体类
 * 预算管控/执行策略:强控,弱控
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@Entity
@Table(name = "strategy_item")
@DynamicInsert
@DynamicUpdate
public class StrategyItem extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -57036484686343107L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_CODE = "code";

    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 代码
     */
    @Column(name = "code")
    private String code;
    /**
     * 名称
     */
    @Column(name = "name")
    private String name;
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
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

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