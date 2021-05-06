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
    public static final String FIELD_ATTRIBUTE_HASH = "attributeHash";

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