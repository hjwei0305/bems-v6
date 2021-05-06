package com.changhong.bems.entity;

import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 预算行项(OrderDetail)实体类
 *
 * @author sei
 * @since 2021-04-25 15:13:58
 */
@Entity
@Table(name = "order_detail_err")
@DynamicInsert
@DynamicUpdate
public class OrderDetailErr extends BaseAttribute implements ITenant, Serializable, Cloneable {
    private static final long serialVersionUID = -90286046160801596L;
    public static final String FIELD_ORDER_ID = "orderId";
    public static final String FIELD_ATTRIBUTE_HASH = "attributeHash";
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
     * 错误信息
     */
    @Column(name = "err_msg")
    private String errMsg;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public OrderDetailErr() {
    }

    public OrderDetailErr(OrderDetail detail) {
        this.orderId = detail.getOrderId();
        this.amount = detail.getAmount();
        this.poolCode = detail.getPoolCode();
        this.poolAmount = detail.getPoolAmount();
        this.originPoolCode = detail.getOriginPoolCode();
        this.originPoolAmount = detail.getOriginPoolAmount();
        this.tenantCode = detail.getTenantCode();
        this.period = detail.getPeriod();
        this.periodName = detail.getPeriodName();
        this.item = detail.getItem();
        this.itemName = detail.getItemName();
        this.org = detail.getOrg();
        this.orgName = detail.getOrgName();
        this.project = detail.getProject();
        this.projectName = detail.getProjectName();
        this.udf1 = detail.getUdf1();
        this.udf1Name = detail.getUdf1Name();
        this.udf2 = detail.getUdf2();
        this.udf2Name = detail.getUdf2Name();
        this.udf3 = detail.getUdf3();
        this.udf3Name = detail.getUdf3Name();
        this.udf4 = detail.getUdf4();
        this.udf4Name = detail.getUdf4Name();
        this.udf5 = detail.getUdf5();
        this.udf5Name = detail.getUdf5Name();
    }

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
    public OrderDetailErr clone() {
        try {
            return (OrderDetailErr) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}