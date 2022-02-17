package com.changhong.bems.dto;

import com.changhong.sei.core.dto.serach.Search;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-02-16 11:19
 */
@ApiModel(description = "预算科目查询对象")
public class BudgetItemSearch extends Search implements Serializable {
    private static final long serialVersionUID = 4782169722959717382L;

    @ApiModelProperty(value = "公司代码")
    private String corpCode;
    @ApiModelProperty(value = "禁用标示")
    private Boolean disabled;

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
