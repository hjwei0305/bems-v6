package com.changhong.bems.entity;

import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.IFrozen;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 实现功能：预算期间类型控制策略
 * 预算结转,业务可用
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-02-08 16:04
 */
@Entity
@Table(name = "strategy_period")
@DynamicInsert
@DynamicUpdate
public class StrategyPeriod extends BaseAuditableEntity implements ITenant, IFrozen, Serializable {
    private static final long serialVersionUID = -802675961919146775L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_PERIOD_TYPE = "periodType";
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 期间分类
     */
    @Column(name = "period_type")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
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
     * 冻结
     */
    @Column(name = "frozen")
    private Boolean frozen = Boolean.FALSE;
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

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
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
}
