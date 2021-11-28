package com.changhong.bems.entity;

import com.changhong.sei.core.dto.IRank;
import com.changhong.sei.core.entity.BaseEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 预算类型维度关系(CategoryDimension)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:24
 */
@Entity
@Table(name = "budget_type_dimension")
@DynamicInsert
@DynamicUpdate
public class BudgetTypeDimension extends BaseEntity implements ITenant, IRank, Serializable {
    private static final long serialVersionUID = -76720938483283048L;
    public static final String FIELD_DIMENSION_CODE = "dimensionCode";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    /**
     * 预算类型id
     */
    @Column(name = "category_id")
    private String categoryId;
    /**
     * 预算维度代码
     */
    @Column(name = "dimension_code")
    private String dimensionCode;
    /**
     * 排序
     */
    @Column(name = "rank")
    private Integer rank = 0;
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

    public String getDimensionCode() {
        return dimensionCode;
    }

    public void setDimensionCode(String dimensionCode) {
        this.dimensionCode = dimensionCode;
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
}