package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 预算科目(Item)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
@ApiModel(description = "预算科目DTO")
public class BudgetItemDto extends BaseEntityDto {
    private static final long serialVersionUID = -85112390830826629L;
    /**
     * 代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "代码", required = true)
    private String code;
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    /**
     * 公司禁用科目
     */
    @ApiModelProperty(value = "是公司禁用科目")
    private Boolean corpFrozen = Boolean.FALSE;
    /**
     * 是否禁用
     */
    @ApiModelProperty(value = "是否禁用.为true时禁用,反之启用")
    private Boolean frozen = Boolean.FALSE;
    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;

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

    public Boolean getCorpFrozen() {
        return corpFrozen;
    }

    public void setCorpFrozen(Boolean corpFrozen) {
        this.corpFrozen = corpFrozen;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}