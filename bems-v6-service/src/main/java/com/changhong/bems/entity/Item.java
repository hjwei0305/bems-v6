package com.changhong.bems.entity;

import com.changhong.bems.dto.CategoryType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.ICodeUnique;
import com.changhong.sei.core.entity.IFrozen;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算科目(Item)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@Entity
@Table(name = "item")
@DynamicInsert
@DynamicUpdate
public class Item extends BaseAuditableEntity implements ITenant, IFrozen, Serializable {
    private static final long serialVersionUID = -57036484686343107L;
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_STRATEGY_ID = "strategyId";
    public static final String FIELD_REFERENCE_ID = "referenceId";
    /**
     * 类型分类
     */
    @Column(name = "type_")
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 代码
     */
    @Column(name = "code")
    private String code;
    /**
     * 名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 执行策略id
     */
    @Column(name = "strategy_id")
    private String strategyId;
    /**
     * 执行策略名称
     */
    @Column(name = "strategy_name")
    private String strategyName;
    /**
     * 冻结
     */
    @Column(name = "frozen")
    private Boolean frozen = Boolean.FALSE;
    /**
     * 参考id
     */
    @Column(name = "reference_id")
    private String referenceId;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @Override
    public Boolean getFrozen() {
        return frozen;
    }

    @Override
    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
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