package com.changhong.bems.entity;

import com.changhong.bems.dto.CategoryType;
import com.changhong.bems.dto.Classification;
import com.changhong.bems.dto.PeriodType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.IFrozen;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算类型(Category)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@Entity
@Table(name = "category")
@DynamicInsert
@DynamicUpdate
public class Category extends BaseAuditableEntity implements ITenant, IFrozen, Serializable {
    private static final long serialVersionUID = -73245932408668629L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_CLASSIFICATION = "classification";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_NAME = "name";
    /**
     * 名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 预算分类
     */
    @Column(name = "classification", updatable = false)
    @Enumerated(EnumType.STRING)
    private Classification classification;
    /**
     * 类型分类
     */
    @Column(name = "category_type", updatable = false)
    @Enumerated(EnumType.STRING)
    private CategoryType type;
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
     * 期间类型
     */
    @Column(name = "period_type")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
    /**
     * 是否冻结
     */
    @Column(name = "frozen")
    private Boolean frozen = Boolean.FALSE;
    /**
     * 参考id
     */
    @Column(name = "reference_id")
    private String referenceId = "none";
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Classification getClassification() {
        return classification;
    }

    public void setClassification(Classification classification) {
        this.classification = classification;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
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

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    @Override
    public Boolean getFrozen() {
        return frozen;
    }

    @Override
    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
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