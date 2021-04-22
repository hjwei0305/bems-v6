package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算主体(Subject)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:26
 */
@ApiModel(description = "预算主体DTO")
public class SubjectDto extends BaseEntityDto {
    private static final long serialVersionUID = -52007275362348933L;
    /**
     * 主体名称
     */
    @ApiModelProperty(value = "主体名称")
    private String name;
    /**
     * 公司代码
     */
    @ApiModelProperty(value = "公司代码")
    private String corporationCode;
    /**
     * 公司名称
     */
    @ApiModelProperty(value = "公司名称")
    private String corporationName;
    /**
     * 组织代码
     */
    @ApiModelProperty(value = "组织代码")
    private String orgCode;
    /**
     * 组织名称
     */
    @ApiModelProperty(value = "组织名称")
    private String orgName;
    /**
     * 币种代码
     */
    @ApiModelProperty(value = "币种代码")
    private String currencyCode;
    /**
     * 币种名称
     */
    @ApiModelProperty(value = "币种名称")
    private String currencyName;
    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCorporationCode() {
        return corporationCode;
    }

    public void setCorporationCode(String corporationCode) {
        this.corporationCode = corporationCode;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

}