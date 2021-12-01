package com.changhong.bems.entity;

import com.changhong.bems.commons.Constants;
import com.changhong.sei.core.entity.BaseEntity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 15:46
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class BaseAttribute extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 8867286168446471538L;
    public static final String FIELD_PERIOD = Constants.DIMENSION_CODE_PERIOD;
    public static final String FIELD_ITEM = Constants.DIMENSION_CODE_ITEM;
    public static final String FIELD_ORG = Constants.DIMENSION_CODE_ORG;
    public static final String FIELD_PROJECT = Constants.DIMENSION_CODE_PROJECT;
    public static final String FIELD_COST_CENTER = Constants.DIMENSION_CODE_COST_CENTER;
    public static final String FIELD_UDF1 = Constants.DIMENSION_CODE_UDF1;
    public static final String FIELD_UDF2 = Constants.DIMENSION_CODE_UDF2;
    public static final String FIELD_UDF3 = Constants.DIMENSION_CODE_UDF3;
    public static final String FIELD_UDF4 = Constants.DIMENSION_CODE_UDF4;
    public static final String FIELD_UDF5 = Constants.DIMENSION_CODE_UDF5;
    public static final String FIELD_ATTRIBUTE = "attribute";
    public static final String FIELD_ATTRIBUTE_CODE = "attributeCode";

    /**
     * 属性
     * 使用到的维度字段名,按asci码排序,逗号(,)分隔
     */
    @Column(name = "attribute")
    protected String attribute;
    /**
     * 属性值hash
     */
    @Column(name = "attribute_code", updatable = false)
    private Long attributeCode = -1L;
    /**
     * 预算期间
     */
    @Column(name = "period_id", updatable = false)
    protected String period = Constants.NONE;
    /**
     * 预算期间名称
     */
    @Column(name = "period_name")
    protected String periodName;
    /**
     * 预算科目
     */
    @Column(name = "item_code", updatable = false)
    protected String item = Constants.NONE;
    /**
     * 预算科目名称
     */
    @Column(name = "item_name")
    protected String itemName;
    /**
     * 组织
     */
    @Column(name = "org", updatable = false)
    protected String org = Constants.NONE;
    /**
     * 组织名称
     */
    @Column(name = "org_name")
    protected String orgName;
    /**
     * 项目
     */
    @Column(name = "project", updatable = false)
    protected String project = Constants.NONE;
    /**
     * 项目名称
     */
    @Column(name = "project_name")
    protected String projectName;
    /**
     * 成本中心
     */
    @Column(name = "cost_center", updatable = false)
    protected String costCenter = Constants.NONE;
    /**
     * 成本中心名称
     */
    @Column(name = "cost_center_name")
    protected String costCenterName;
    /**
     * 自定义1
     */
    @Column(name = "udf1", updatable = false)
    protected String udf1 = Constants.NONE;
    /**
     * 自定义1名称
     */
    @Column(name = "udf1_name")
    protected String udf1Name;
    /**
     * 自定义2
     */
    @Column(name = "udf2", updatable = false)
    protected String udf2 = Constants.NONE;
    /**
     * 自定义2名称
     */
    @Column(name = "udf2_name")
    protected String udf2Name;
    /**
     * 自定义3
     */
    @Column(name = "udf3", updatable = false)
    protected String udf3 = Constants.NONE;
    /**
     * 自定义3名称
     */
    @Column(name = "udf3_name")
    protected String udf3Name;
    /**
     * 自定义4
     */
    @Column(name = "udf4", updatable = false)
    protected String udf4 = Constants.NONE;
    /**
     * 自定义4名称
     */
    @Column(name = "udf4_name")
    protected String udf4Name;
    /**
     * 自定义5
     */
    @Column(name = "udf5", updatable = false)
    protected String udf5 = Constants.NONE;
    /**
     * 自定义5名称
     */
    @Column(name = "udf5_name")
    protected String udf5Name;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
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

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
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

    public Long getAttributeCode() {
        long hash = 1L;
        hash = 31 * hash + this.getItem().hashCode();
        hash = 31 * hash + this.getPeriod().hashCode();
        hash = 31 * hash + this.getOrg().hashCode();
        hash = 31 * hash + this.getProject().hashCode();
        hash = 31 * hash + this.getCostCenter().hashCode();

        hash = 31 * hash + this.getUdf1().hashCode();
        hash = 31 * hash + this.getUdf2().hashCode();
        hash = 31 * hash + this.getUdf3().hashCode();
        hash = 31 * hash + this.getUdf4().hashCode();
        hash = 31 * hash + this.getUdf5().hashCode();
        attributeCode = hash;
        return attributeCode;
    }

    public void setAttributeCode(Long code) {
        this.attributeCode = code;
    }

}
