package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

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
     * 预算分类
     */
    @NotNull
    @ApiModelProperty(value = "预算分类", required = true)
    private Classification classification;
    /**
     * 是组织级预算中的部门级预算
     */
    @ApiModelProperty(value = "是组织级预算中的部门级预算")
    private Boolean isDepartment = Boolean.FALSE;
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
    @Size(max = 50)
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
    /**
     * 冻结
     */
    @ApiModelProperty(value = "冻结")
    private Boolean frozen = Boolean.FALSE;
    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;
    /**
     * 组织级预算主体关联的组织机构
     */
    @ApiModelProperty(value = "组织级预算主体关联的组织机构")
    private Set<OrganizationDto> orgList;

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

    public Boolean getIsDepartment() {
        return isDepartment;
    }

    public void setIsDepartment(Boolean isDepartment) {
        this.isDepartment = isDepartment;
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

    public Set<OrganizationDto> getOrgList() {
        return orgList;
    }

    public void setOrgList(Set<OrganizationDto> orgList) {
        this.orgList = orgList;
    }
}