package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 预算维度属性(OrderItem)实体类
 *
 * @author sei
 * @since 2021-04-25 15:13:58
 */
@Entity
@Table(name = "order_item")
@DynamicInsert
@DynamicUpdate
public class OrderItem extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -90286046160801596L;
    public static final String FIELD_ITEM_ID = "itemId";
    /**
     * 预算申请单id
     */
    @Column(name = "order_id")
    private String orderId;
    /**
     * 行号
     */
    @Column(name = "line_number")
    private String lineNumber;
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
     * 预算期间
     */
    @Column(name = "period_id")
    private String periodId;
    /**
     * 预算期间名称
     */
    @Column(name = "period_name")
    private String periodName;
    /**
     * 预算科目
     */
    @Column(name = "item_id")
    private String itemId;
    /**
     * 预算科目名称
     */
    @Column(name = "item_name")
    private String itemName;
    /**
     * 组织
     */
    @Column(name = "org")
    private String org;
    /**
     * 组织名称
     */
    @Column(name = "org_name")
    private String orgName;
    /**
     * 项目
     */
    @Column(name = "project")
    private String project;
    /**
     * 项目名称
     */
    @Column(name = "project_name")
    private String projectName;
    /**
     * 自定义1
     */
    @Column(name = "udf1")
    private String udf1;
    /**
     * 自定义1名称
     */
    @Column(name = "udf1_name")
    private String udf1Name;
    /**
     * 自定义2
     */
    @Column(name = "udf2")
    private String udf2;
    /**
     * 自定义2名称
     */
    @Column(name = "udf2_name")
    private String udf2Name;
    /**
     * 自定义3
     */
    @Column(name = "udf3")
    private String udf3;
    /**
     * 自定义3名称
     */
    @Column(name = "udf3_name")
    private String udf3Name;
    /**
     * 自定义4
     */
    @Column(name = "udf4")
    private String udf4;
    /**
     * 自定义4名称
     */
    @Column(name = "udf4_name")
    private String udf4Name;
    /**
     * 自定义5
     */
    @Column(name = "udf5")
    private String udf5;
    /**
     * 自定义5名称
     */
    @Column(name = "udf5_name")
    private String udf5Name;
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

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
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

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf1Name() {
        return udf1Name;
    }

    public void setUdf1Name(String udf1Name) {
        this.udf1Name = udf1Name;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf2Name() {
        return udf2Name;
    }

    public void setUdf2Name(String udf2Name) {
        this.udf2Name = udf2Name;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getUdf3Name() {
        return udf3Name;
    }

    public void setUdf3Name(String udf3Name) {
        this.udf3Name = udf3Name;
    }

    public String getUdf4() {
        return udf4;
    }

    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    public String getUdf4Name() {
        return udf4Name;
    }

    public void setUdf4Name(String udf4Name) {
        this.udf4Name = udf4Name;
    }

    public String getUdf5() {
        return udf5;
    }

    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }

    public String getUdf5Name() {
        return udf5Name;
    }

    public void setUdf5Name(String udf5Name) {
        this.udf5Name = udf5Name;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}