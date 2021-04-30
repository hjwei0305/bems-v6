package com.changhong.bems.entity;

import com.changhong.bems.commons.Constants;
import com.changhong.sei.core.entity.BaseEntity;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

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
public class DimensionAttribute extends BaseEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -63903291983170191L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_ATTRIBUTE = "attribute";
    public static final String FIELD_ATTRIBUTE_HASH = "attributeHash";
    public static final String FIELD_PERIOD = "period";
    public static final String FIELD_ITEM = "item";
    public static final String FIELD_ORG = "org";
    public static final String FIELD_PROJECT = "project";
    public static final String FIELD_UDF1 = "udf1";
    public static final String FIELD_UDF2 = "udf2";
    public static final String FIELD_UDF3 = "udf3";
    public static final String FIELD_UDF4 = "udf4";
    public static final String FIELD_UDF5 = "udf5";
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 属性
     * 使用到的维度字段名,按asci码排序,逗号(,)分隔
     */
    @Column(name = "attribute")
    private String attribute;
    /**
     * 属性值hash
     */
    @Column(name = "attribute_hash")
    private Long attributeHash = -1L;
    /**
     * 预算期间
     */
    @Column(name = "period_id")
    private String period = Constants.NONE;
    /**
     * 预算期间名称
     */
    @Column(name = "period_name")
    private String periodName;
    /**
     * 预算科目
     */
    @Column(name = "item_code")
    private String item = Constants.NONE;
    /**
     * 预算科目名称
     */
    @Column(name = "item_name")
    private String itemName;
    /**
     * 组织
     */
    @Column(name = "org")
    private String org = Constants.NONE;
    /**
     * 组织名称
     */
    @Column(name = "org_name")
    private String orgName;
    /**
     * 项目
     */
    @Column(name = "project")
    private String project = Constants.NONE;
    /**
     * 项目名称
     */
    @Column(name = "project_name")
    private String projectName;
    /**
     * 自定义1
     */
    @Column(name = "udf1")
    private String udf1 = Constants.NONE;
    /**
     * 自定义1名称
     */
    @Column(name = "udf1_name")
    private String udf1Name;
    /**
     * 自定义2
     */
    @Column(name = "udf2")
    private String udf2 = Constants.NONE;
    /**
     * 自定义2名称
     */
    @Column(name = "udf2_name")
    private String udf2Name;
    /**
     * 自定义3
     */
    @Column(name = "udf3")
    private String udf3 = Constants.NONE;
    /**
     * 自定义3名称
     */
    @Column(name = "udf3_name")
    private String udf3Name;
    /**
     * 自定义4
     */
    @Column(name = "udf4")
    private String udf4 = Constants.NONE;
    /**
     * 自定义4名称
     */
    @Column(name = "udf4_name")
    private String udf4Name;
    /**
     * 自定义5
     */
    @Column(name = "udf5")
    private String udf5 = Constants.NONE;
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

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Long getAttributeHash() {
        return attributeHash;
    }

    public void setAttributeHash(Long attributeHash) {
        this.attributeHash = attributeHash;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String periodId) {
        this.period = periodId;
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

    public void setItem(String itemId) {
        this.item = itemId;
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

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
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
        DimensionAttribute attribute = (DimensionAttribute) o;
        return Objects.equals(subjectId, attribute.subjectId) && Objects.equals(period, attribute.period) && Objects.equals(item, attribute.item) && Objects.equals(org, attribute.org) && Objects.equals(project, attribute.project) && Objects.equals(udf1, attribute.udf1) && Objects.equals(udf2, attribute.udf2) && Objects.equals(udf3, attribute.udf3) && Objects.equals(udf4, attribute.udf4) && Objects.equals(udf5, attribute.udf5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subjectId, period, item, org, project, udf1, udf2, udf3, udf4, udf5);
    }
}