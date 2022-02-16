package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2022-02-16 11:31
 */
@ApiModel(description = "预算科目操作对象")
public class BudgetItemDisableRequest implements Serializable {
    private static final long serialVersionUID = 7294968551674181833L;

    @ApiModelProperty(value = "公司代码.为通用科目时,可为空")
    private String corpCode;

    @ApiModelProperty(value = "禁用科目")
    private boolean disabled = Boolean.TRUE;

    @NotEmpty
    @ApiModelProperty(value = "科目id清单")
    private Set<String> ids;

    public String getCorpCode() {
        return corpCode;
    }

    public void setCorpCode(String corpCode) {
        this.corpCode = corpCode;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }
}
