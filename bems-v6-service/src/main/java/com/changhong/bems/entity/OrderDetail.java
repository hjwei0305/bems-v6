package com.changhong.bems.entity;

import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * 预算行项(OrderDetail)实体类
 *
 * @author sei
 * @since 2021-04-25 15:13:58
 */
@Entity
@Table(name = "order_detail")
@DynamicInsert
@DynamicUpdate
public class OrderDetail extends BaseAttribute implements ITenant, Serializable, Cloneable {
    private static final long serialVersionUID = -90286046160801596L;
    public static final String FIELD_ORDER_ID = "orderId";
    public static final String FIELD_ORIGIN_POOL_CODE = "originPoolCode";
    /**
     * 预算申请单id
     */
    @Column(name = "order_id")
    private String orderId;
    /**
     * 金额
     */
    @Column(name = "amount")
    private Double amount = 0d;
    /**
     * 预算池编码
     */
    @Column(name = "pool_code")
    private String poolCode;
    /**
     * 预算池金额
     */
    @Column(name = "pool_amount")
    private Double poolAmount = 0d;
    /**
     * 来源预算池编码
     */
    @Column(name = "origin_pool_code")
    private String originPoolCode;
    /**
     * 来源预算池金额
     */
    @Column(name = "origin_pool_amount")
    private Double originPoolAmount = 0d;
    /**
     * 是否错误
     */
    @Column(name = "has_err")
    private Boolean hasErr = Boolean.FALSE;
    /**
     * 错误信息
     */
    @Column(name = "err_msg")
    private String errMsg;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public Double getPoolAmount() {
        return poolAmount;
    }

    public void setPoolAmount(Double poolAmount) {
        this.poolAmount = poolAmount;
    }

    public String getOriginPoolCode() {
        return originPoolCode;
    }

    public void setOriginPoolCode(String originPoolCode) {
        this.originPoolCode = originPoolCode;
    }

    public Double getOriginPoolAmount() {
        return originPoolAmount;
    }

    public void setOriginPoolAmount(Double originPoolAmount) {
        this.originPoolAmount = originPoolAmount;
    }

    public Boolean getHasErr() {
        return hasErr;
    }

    public void setHasErr(Boolean hasErr) {
        this.hasErr = hasErr;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    @Override
    public OrderDetail clone() {
        try {
            return (OrderDetail) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        OrderDetail detail = (OrderDetail) o;
        return Objects.equals(orderId, detail.orderId) && Objects.equals(period, detail.period) && Objects.equals(item, detail.item) && Objects.equals(org, detail.org) && Objects.equals(project, detail.project) && Objects.equals(udf1, detail.udf1) && Objects.equals(udf2, detail.udf2) && Objects.equals(udf3, detail.udf3) && Objects.equals(udf4, detail.udf4) && Objects.equals(udf5, detail.udf5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orderId, period, item, org, project, udf1, udf2, udf3, udf4, udf5);
    }
}