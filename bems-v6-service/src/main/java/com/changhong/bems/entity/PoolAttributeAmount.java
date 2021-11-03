package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预算池维度属性金额(PoolSummaryAmount)实体类
 *
 * @author sei
 * @since 2021-09-30 10:27:03
 */
@Entity
@Table(name = "pool_attribute_amount")
@DynamicInsert
@DynamicUpdate
public class PoolAttributeAmount extends BaseEntity implements ITenant, Serializable {
    private static final long serialVersionUID = 921560050269283338L;
    public static final String FIELD_POOL_ID = "poolId";

    /**
     * 预算主体id
     */
    @Column(name = "subject_id", updatable = false)
    private String subjectId;
    /**
     * 预算维度属性code
     */
    @Column(name = "attribute_code", updatable = false)
    private Long attributeCode;
    /**
     * 预算池id
     */
    @Column(name = "pool_id", updatable = false)
    private String poolId;
    /**
     * 所属年度
     */
    @Column(name = "year", updatable = false)
    private Integer year;
    /**
     * 所属月度
     */
    @Column(name = "month", updatable = false)
    private Integer month;
    /**
     * 初始注入
     * 通过注入且新产生预算池时的金额,作为初始注入金额,用于多维分析的差异计算
     */
    @Column(name = "init_inject_amount", updatable = false)
    private BigDecimal initInjectAmount = BigDecimal.ZERO;
    /**
     * 初始调入
     * 新产生预算池时的金额,作为初始注入金额,用于预算池分析的差异计算
     */
    @Column(name = "init_revise_in_amount", updatable = false)
    private BigDecimal initReviseInAmount = BigDecimal.ZERO;
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
    @Column(name = "tenant_code", updatable = false)
    private String tenantCode;

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getInitInjectAmount() {
        return initInjectAmount;
    }

    public void setInitInjectAmount(BigDecimal initInjectAmount) {
        this.initInjectAmount = initInjectAmount;
    }

    public BigDecimal getInitReviseInAmount() {
        return initReviseInAmount;
    }

    public void setInitReviseInAmount(BigDecimal initReviseInAmount) {
        this.initReviseInAmount = initReviseInAmount;
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