package com.changhong.bems.sdk.dto;

import com.changhong.sei.core.dto.BaseEntityDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 预算科目(Item)DTO类
 *
 * @author sei
 * @since 2021-04-22 12:54:30
 */
public class BudgetItemDto extends BaseEntityDto {
    private static final long serialVersionUID = -85112390830826629L;
    /**
     * 代码
     */
    @NotBlank
    @Size(max = 30)
    private String code;
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 50)
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