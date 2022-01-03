package com.changhong.bems.entity;

import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderStatus;
import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ICodeUnique;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 预算申请单(Order)实体类
 *
 * @author sei
 * @since 2021-04-25 15:13:56
 */
@Entity
@Table(name = "order_head")
@DynamicInsert
@DynamicUpdate
public class Order extends BaseAuditableEntity implements ITenant, ICodeUnique, Serializable {
    private static final long serialVersionUID = -36135917259025562L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_ORDER_CATEGORY = "orderCategory";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_CREATOR_ID = "creatorId";
    /**
     * 申请单号
     */
    @Column(name = "code", nullable = false)
    private String code;
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 预算主体名称
     */
    @Column(name = "subject_name")
    private String subjectName;
    /**
     * 币种代码
     */
    @Column(name = "currency_code")
    private String currencyCode;
    /**
     * 币种名称
     */
    @Column(name = "currency_name")
    private String currencyName;
    /**
     * 预算类型id
     */
    @Column(name = "category_id")
    private String categoryId;
    /**
     * 预算类型名称
     */
    @Column(name = "category_name")
    private String categoryName;
    /**
     * 期间分类
     */
    @Column(name = "period_category")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
    /**
     * 订单类型
     */
    @Column(name = "order_category")
    @Enumerated(EnumType.STRING)
    private OrderCategory orderCategory;
    /**
     * 申请金额
     */
    @Column(name = "apply_amount")
    private BigDecimal applyAmount = BigDecimal.ZERO;
    /**
     * 申请组织id
     */
    @Column(name = "apply_org_id")
    private String applyOrgId;
    /**
     * 申请组织代码
     */
    @Column(name = "apply_org_code")
    private String applyOrgCode;
    /**
     * 申请组织名称
     */
    @Column(name = "apply_org_name")
    private String applyOrgName;
    /**
     * 归口管理组织id
     */
    @Column(name = "manager_org_id")
    private String managerOrgId;
    /**
     * 归口管理组织代码
     */
    @Column(name = "manager_org_code")
    private String managerOrgCode;
    /**
     * 归口管理组织名称
     */
    @Column(name = "manager_org_name")
    private String managerOrgName;
    /**
     * 备注说明
     */
    @Column(name = "remark")
    private String remark;
    /**
     * 是否手动生效
     */
    @Column(name = "manually_effective")
    private Boolean manuallyEffective = Boolean.FALSE;
    /**
     * 是否正在异步处理行项数据
     * 如果是,在编辑时进入socket状态显示页面
     */
    @Column(name = "processing")
    private Boolean processing = Boolean.FALSE;
    /**
     * 状态
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PREFAB;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;
    @Transient
    private List<String> docIds;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public OrderCategory getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(OrderCategory orderCategory) {
        this.orderCategory = orderCategory;
    }

    public BigDecimal getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(BigDecimal applyAmount) {
        this.applyAmount = applyAmount;
    }

    public String getApplyOrgId() {
        return applyOrgId;
    }

    public void setApplyOrgId(String applyOrgId) {
        this.applyOrgId = applyOrgId;
    }

    public String getApplyOrgCode() {
        return applyOrgCode;
    }

    public void setApplyOrgCode(String applyOrgCode) {
        this.applyOrgCode = applyOrgCode;
    }

    public String getApplyOrgName() {
        return applyOrgName;
    }

    public void setApplyOrgName(String applyOrgName) {
        this.applyOrgName = applyOrgName;
    }

    public String getManagerOrgId() {
        return managerOrgId;
    }

    public void setManagerOrgId(String managerOrgId) {
        this.managerOrgId = managerOrgId;
    }

    public String getManagerOrgCode() {
        return managerOrgCode;
    }

    public void setManagerOrgCode(String managerOrgCode) {
        this.managerOrgCode = managerOrgCode;
    }

    public String getManagerOrgName() {
        return managerOrgName;
    }

    public void setManagerOrgName(String managerOrgName) {
        this.managerOrgName = managerOrgName;
    }

    public Boolean getManuallyEffective() {
        return manuallyEffective;
    }

    public Order setManuallyEffective(Boolean manuallyEffective) {
        this.manuallyEffective = manuallyEffective;
        return this;
    }

    public Boolean getProcessing() {
        return processing;
    }

    public void setProcessing(Boolean processing) {
        this.processing = processing;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public List<String> getDocIds() {
        return docIds;
    }

    public void setDocIds(List<String> docIds) {
        this.docIds = docIds;
    }
}