package com.changhong.bems.entity;

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
public class DimensionAttribute extends BaseAttribute implements ITenant, Serializable {
    private static final long serialVersionUID = -63903291983170191L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_ATTRIBUTE = "attribute";

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
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public DimensionAttribute() {
    }

    public DimensionAttribute(BaseAttribute attribute) {
        this.period = attribute.getPeriod();
        this.periodName = attribute.getPeriodName();
        this.item = attribute.getItem();
        this.itemName = attribute.getItemName();
        this.org = attribute.getOrg();
        this.orgName = attribute.getOrgName();
        this.project = attribute.getProject();
        this.projectName = attribute.getProjectName();
        this.udf1 = attribute.getUdf1();
        this.udf1Name = attribute.getUdf1Name();
        this.udf2 = attribute.getUdf2();
        this.udf2Name = attribute.getUdf2Name();
        this.udf3 = attribute.getUdf3();
        this.udf3Name = attribute.getUdf3Name();
        this.udf4 = attribute.getUdf4();
        this.udf4Name = attribute.getUdf4Name();
        this.udf5 = attribute.getUdf5();
        this.udf5Name = attribute.getUdf5Name();
    }

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