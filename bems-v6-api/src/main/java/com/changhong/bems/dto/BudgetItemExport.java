package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-02-16 11:19
 */
@ApiModel(description = "预算科目导出对象")
public class BudgetItemExport implements Serializable {
    private static final long serialVersionUID = 4782169722959717382L;

    @ApiModelProperty(value = "代码")
    private String code;
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
