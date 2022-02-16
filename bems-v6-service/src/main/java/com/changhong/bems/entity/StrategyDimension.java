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
 * 实现功能：维度策略/匹配策略
 * 主要管理预算主体维度自定义的维度策略
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-01 13:12
 */
@Entity
@Table(name = "strategy_dimension")
@DynamicInsert
@DynamicUpdate
public class StrategyDimension extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -3836655083039242219L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_CODE = "code";
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;
    /**
     * 维度代码
     */
    @Column(name = "code")
    private String code;
    /**
     * 维度策略id
     */
    @Column(name = "strategy_id")
    private String strategyId;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }
}
