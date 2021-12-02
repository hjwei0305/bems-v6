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
 * 实现功能：预算主体组织
 * 预算主体未组织级关联的组织机构
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-01 13:12
 */
@Entity
@Table(name = "subject_organization")
@DynamicInsert
@DynamicUpdate
public class SubjectOrganization extends BaseAuditableEntity implements ITenant, Serializable {
    private static final long serialVersionUID = -3836655083039242219L;
    public static final String FIELD_SUBJECT_ID = "subjectId";
    public static final String FIELD_ORG_ID = "orgId";
    /**
     * 预算主体id
     */
    @Column(name = "subject_id")
    private String subjectId;
    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;
    /**
     * 组织id
     */
    @Column(name = "org_id")
    private String orgId;
    /**
     * 组织代码
     */
    @Column(name = "org_code")
    private String orgCode;
    /**
     * 组织名称
     */
    @Column(name = "org_name")
    private String orgName;
    /**
     * 组织名称路径
     */
    @Column(name = "org_name_path")
    private String orgNamePath;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgNamePath() {
        return orgNamePath;
    }

    public void setOrgNamePath(String orgNamePath) {
        this.orgNamePath = orgNamePath;
    }
}
