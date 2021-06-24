package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
     * 主体代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "主体代码", required = true)
    private String code;
    /**
     * 主体名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "主体名称", required = true)
    private String name;
    /**
     * 公司代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "公司代码", required = true)
    private String corporationCode;
    /**
     * 公司名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "公司名称")
    private String corporationName;
    /**
     * 组织ID
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "组织ID", required = true)
    private String orgId;
    /**
     * 组织代码
     */
    @Size(max = 30)
    @ApiModelProperty(value = "组织代码")
    private String orgCode;
    /**
     * 组织名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "组织名称")
    private String orgName;
    /**
     * 币种代码
     */
    @Size(max = 30)
    @ApiModelProperty(value = "币种代码")
    private String currencyCode;
    /**
     * 币种名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "币种名称")
    private String currencyName;
    /**
     * 执行策略id
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "执行策略id", required = true)
    private String strategyId;
    /**
     * 执行策略名称
     */
    @Size(max = 50)
    @ApiModelProperty(value = "执行策略名称")
    private String strategyName;
    /**
     * 排序
     */
    @ApiModelProperty(value = "排序")
    private Integer rank = 0;

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

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}