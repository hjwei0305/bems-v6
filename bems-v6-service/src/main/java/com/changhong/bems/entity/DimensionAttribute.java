package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 预算维度属性(DimensionAttribute)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:28
 */
@Entity
@Table(name = "dimension_attribute")
@DynamicInsert
@DynamicUpdate
public class DimensionAttribute extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -63903291983170191L;
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 预算类型id
     */
    @Column(name = "category_id")
    private String categoryId;
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


    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
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