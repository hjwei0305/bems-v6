package com.changhong.bems.entity;

import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算配置(CategoryConfig)实体类
 *
 * @author sei
 * @since 2021-09-24 09:12:59
 */
@Entity
@Table(name = "budget_type_config")
@DynamicInsert
@DynamicUpdate
public class BudgetTypeConfig extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = 202364499187955339L;
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_ORDER_CATEGORY = "orderCategory";

    /**
     * 预算类型id
     */
    @Column(name = "category_id")
    private String categoryId;
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
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

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

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}