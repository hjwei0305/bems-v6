package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-26 15:47
 */
@ApiModel(description = "分配预算科目DTO")
public class AssigneItemRequest implements Serializable {
    private static final long serialVersionUID = -2652295325558282931L;
    /**
     * 预算主体id
     */
    @NotBlank
    @ApiModelProperty(value = "预算主体id", required = true)
    private String subjectId;
    /**
     * 维度代码清单
     */
    @NotNull
    @ApiModelProperty(value = "科目代码清单", required = true)
    private Set<String> itemCodes;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Set<String> getItemCodes() {
        return itemCodes;
    }

    public void setItemCodes(Set<String> itemCodes) {
        this.itemCodes = itemCodes;
    }
}
