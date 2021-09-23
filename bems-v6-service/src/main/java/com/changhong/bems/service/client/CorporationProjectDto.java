package com.changhong.bems.service.client;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 公司项目(CorporationProject)DTO类
 *
 * @author sei
 * @since 2021-07-31 16:02:20
 */
@ApiModel(description = "公司项目DTO")
public class CorporationProjectDto extends BaseEntityDto {
    private static final long serialVersionUID = -59692976042104106L;
    public static final String FIELD_ERP_CORPORATION_CODE = "erpCorporationCode";
    public static final String FIELD_WBS_PROJECT_CODE = "wbsProjectCode";
    public static final String FIELD_WBS_PROJECT_NAME = "wbsProjectName";
    public static final String FIELD_INNER_ORDER_CODE = "innerOrderCode";
    public static final String FIELD_INNER_ORDER_NAME = "innerOrderName";
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "名称", required = true)
    private String name;
    /**
     * ERP公司代码
     */
    @NotBlank
    @Size(max = 10)
    @ApiModelProperty(value = "ERP公司代码", required = true)
    private String erpCorporationCode;
    /**
     * WBS项目Id
     */
    @ApiModelProperty(value = "WBS项目Id", notes = "WBS项目编号与内部订单编号两者必填一个")
    private String wbsProjectId;
    /**
     * 关联的WBS项目编号
     */
    @ApiModelProperty(value = "关联的WBS项目编号")
    private String wbsProjectCode;
    /**
     * 关联的WBS项目名称
     */
    @ApiModelProperty(value = "关联的WBS项目名称")
    private String wbsProjectName;
    /**
     * 内部订单Id
     */

    @ApiModelProperty(value = "内部订单Id", notes = "WBS项目编号与内部订单编号两者必填一个")
    private String innerOrderId;
    /**
     * 关联的内部订单编号
     */
    @ApiModelProperty(value = "关联的内部订单编号")
    private String innerOrderCode;
    /**
     * 关联的内部订单名称
     */
    @ApiModelProperty(value = "关联的内部订单名称")
    private String innerOrderName;
    /**
     * 冻结标识
     */
    @ApiModelProperty(value = "冻结标识")
    private Boolean frozen = Boolean.FALSE;
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

    public String getErpCorporationCode() {
        return erpCorporationCode;
    }

    public void setErpCorporationCode(String erpCorporationCode) {
        this.erpCorporationCode = erpCorporationCode;
    }

    public String getWbsProjectId() {
        return wbsProjectId;
    }

    public void setWbsProjectId(String wbsProjectId) {
        this.wbsProjectId = wbsProjectId;
    }

    public String getInnerOrderId() {
        return innerOrderId;
    }

    public void setInnerOrderId(String innerOrderId) {
        this.innerOrderId = innerOrderId;
    }

    public String getWbsProjectCode() {
        return wbsProjectCode;
    }

    public void setWbsProjectCode(String wbsProjectCode) {
        this.wbsProjectCode = wbsProjectCode;
    }

    public String getWbsProjectName() {
        return wbsProjectName;
    }

    public void setWbsProjectName(String wbsProjectName) {
        this.wbsProjectName = wbsProjectName;
    }

    public String getInnerOrderCode() {
        return innerOrderCode;
    }

    public void setInnerOrderCode(String innerOrderCode) {
        this.innerOrderCode = innerOrderCode;
    }

    public String getInnerOrderName() {
        return innerOrderName;
    }

    public void setInnerOrderName(String innerOrderName) {
        this.innerOrderName = innerOrderName;
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