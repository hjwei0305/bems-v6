package com.changhong.bems.entity;

import com.changhong.bems.commons.Constants;
import com.changhong.sei.core.dto.IRank;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

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
public class OrderDetail extends BaseAttribute implements ITenant, IRank, Serializable, Cloneable {
    private static final long serialVersionUID = -90286046160801596L;
    public static final String FIELD_ORDER_ID = "orderId";
    public static final String FIELD_ORIGIN_POOL_CODE = "originPoolCode";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_ERRMSG = "errMsg";
    /**
     * 预算申请单id
     */
    @Column(name = "order_id", updatable = false)
    private String orderId;
    /**
     * 金额
     */
    @Column(name = "amount")
    private BigDecimal amount = BigDecimal.ZERO;
    /**
     * 预算池编码
     */
    @Column(name = "pool_code")
    private String poolCode;
    /**
     * 预算池金额
     */
    @Column(name = "pool_amount")
    private BigDecimal poolAmount = BigDecimal.ZERO;
    /**
     * 来源预算池编码
     */
    @Column(name = "origin_pool_code")
    private String originPoolCode = Constants.NONE;
    /**
     * 来源预算池金额
     */
    @Column(name = "origin_pool_amount")
    private BigDecimal originPoolAmount = BigDecimal.ZERO;
    /**
     * 是否正在异步处理行项数据
     * 如果是,在编辑时进入socket状态显示页面
     */
    @Column(name = "processing")
    private Boolean processing = Boolean.FALSE;
    /**
     * 状态:
     * -1: 初始
     * 0:已确认(预占用)
     * 1:已生效
     */
    @Column(name = "state")
    private Short state = -1;
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
     * 创建时间
     */
    @Column(name = "created_date", updatable = false)
    protected LocalDateTime createdDate;
    /**
     * 序号
     */
    @Column(name = "rank")
    private Integer rank = 0;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public BigDecimal getPoolAmount() {
        return poolAmount;
    }

    public void setPoolAmount(BigDecimal poolAmount) {
        this.poolAmount = poolAmount;
    }

    public String getOriginPoolCode() {
        return originPoolCode;
    }

    public void setOriginPoolCode(String originPoolCode) {
        this.originPoolCode = originPoolCode;
    }

    public BigDecimal getOriginPoolAmount() {
        return originPoolAmount;
    }

    public void setOriginPoolAmount(BigDecimal originPoolAmount) {
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

    public Boolean getProcessing() {
        return processing;
    }

    public void setProcessing(Boolean processing) {
        this.processing = processing;
    }

    public Short getState() {
        return state;
    }

    public void setState(Short state) {
        this.state = state;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
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
    public String toString() {
        return new StringJoiner(", ", OrderDetail.class.getSimpleName() + "[", "]")
                .add("orderId='" + orderId + "'")
                .add("amount=" + amount)
                .add("period='" + period + "'")
                .add("item='" + item + "'")
                .add("org='" + org + "'")
                .add("project='" + project + "'")
                .add("udf1='" + udf1 + "'")
                .add("udf2='" + udf2 + "'")
                .add("udf3='" + udf3 + "'")
                .add("udf4='" + udf4 + "'")
                .add("udf5='" + udf5 + "'")
                .toString();
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