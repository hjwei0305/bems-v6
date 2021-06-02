package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 实现功能: 公司DTO
 *
 * @author 马超(Vision.Mac)
 */
@ApiModel(description = "公司DTO")
public class CorporationDto implements Serializable {
    private static final long serialVersionUID = -1449135624378442219L;
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private String id;
    /**
     * 代码
     */
    @ApiModelProperty(value = "代码")
    private String code;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * ERP公司代码
     */
    @ApiModelProperty(value = "ERP公司代码")
    private String erpCode;

    /**
     * 本位币货币代码
     */
    @ApiModelProperty(value = "本位币货币代码")
    private String baseCurrencyCode;

    /**
     * 本位币货币名称
     */
    @ApiModelProperty(value = "本位币货币名称")
    private String baseCurrencyName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getErpCode() {
        return erpCode;
    }

    public void setErpCode(String erpCode) {
        this.erpCode = erpCode;
    }

    public String getBaseCurrencyCode() {
        return baseCurrencyCode;
    }

    public void setBaseCurrencyCode(String baseCurrencyCode) {
        this.baseCurrencyCode = baseCurrencyCode;
    }

    public String getBaseCurrencyName() {
        return baseCurrencyName;
    }

    public void setBaseCurrencyName(String baseCurrencyName) {
        this.baseCurrencyName = baseCurrencyName;
    }
}
