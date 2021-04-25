package com.changhong.bems.entity;

import com.changhong.bems.dto.OrderCategory;
import com.changhong.bems.dto.OrderStatus;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算申请单(Order)实体类
 *
 * @author sei
 * @since 2021-04-25 15:13:56
 */
@Entity
@Table(name = "order")
@DynamicInsert
@DynamicUpdate
public class Order extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -36135917259025562L;
    /**
     * 申请单号
     */
    @Column(name = "code")
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
     * 订单类型
     */
    @Column(name = "order_category")
    @Enumerated(EnumType.STRING)
    private OrderCategory orderCategory;
    /**
     * 申请金额
     */
    @Column(name = "apply_amount")
    private Double applyAmount = 0d;
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
     * 申请人代码
     */
    @Column(name = "apply_user_account")
    private String applyUserAccount;
    /**
     * 申请人名称
     */
    @Column(name = "apply_user_name")
    private String applyUserName;
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
     * 状态
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;


    public String getCode() {
        return code;
    }

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

    public OrderCategory getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(OrderCategory orderCategory) {
        this.orderCategory = orderCategory;
    }

    public Double getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(Double applyAmount) {
        this.applyAmount = applyAmount;
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

    public String getApplyUserAccount() {
        return applyUserAccount;
    }

    public void setApplyUserAccount(String applyUserAccount) {
        this.applyUserAccount = applyUserAccount;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
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
}