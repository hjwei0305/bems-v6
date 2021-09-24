package com.changhong.bems.entity;

import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算配置(OrderConfig)实体类
 *
 * @author sei
 * @since 2021-09-24 09:12:59
 */
@Entity
@Table(name = "order_config")
@DynamicInsert
@DynamicUpdate
public class OrderConfig extends BaseAuditableEntity implements Serializable {
    private static final long serialVersionUID = 202364499187955339L;
    /**
     * 订单类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_category")
    private OrderCategory orderCategory;
    /**
     * 期间类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type")
    private PeriodType periodType;
    /**
     * 是否启用
     */
    @Column(name = "enable")
    private Boolean enable = Boolean.TRUE;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public OrderCategory getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(OrderCategory orderCategory) {
        this.orderCategory = orderCategory;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}