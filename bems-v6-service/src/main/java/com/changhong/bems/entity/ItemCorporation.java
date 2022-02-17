package com.changhong.bems.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.IFrozen;
import com.changhong.sei.core.entity.ITenant;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 公司预算科目(ItemCorporation)实体类
 *
 * @author sei
 * @since 2021-04-22 12:54:29
 */
@Entity
@Table(name = "item_corporation")
@DynamicInsert
@DynamicUpdate
public class ItemCorporation extends BaseAuditableEntity implements ITenant, IFrozen, Serializable {
    private static final long serialVersionUID = -57036484686343107L;
    public static final String FIELD_CORP_CODE = "corpCode";

    /**
     * 参考id
     */
    @Column(name = "item_id", updatable = false)
    private String itemId;
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
     * 公司代码
     */
    @Column(name = "corporation_code", updatable = false)
    private String corpCode;
    /**
     * 是否冻结
     */
    @Column(name = "frozen")
    private Boolean frozen = Boolean.FALSE;

    /**
     * 租户代码
     */
    @Column(name = "tenant_code")
    private String tenantCode;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode;
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