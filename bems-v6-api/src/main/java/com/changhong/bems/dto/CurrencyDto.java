package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 币种(Currency)DTO类
 *
 * @author sei
 * @since 2020-08-17 14:03:22
 */
@ApiModel(description = "币种")
public class CurrencyDto implements Serializable {
    private static final long serialVersionUID = 402624026080441813L;
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
}