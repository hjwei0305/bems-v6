package com.changhong.bems.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-09 14:47
 */
@ApiModel(description = "年度预算分析查询")
public class AnnualBudgetRequest implements Serializable {
    private static final long serialVersionUID = 1292224557325418654L;
    /**
     * 预算主体id
     */
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 所属年度
     */
    @ApiModelProperty(value = "所属年度")
    private Integer year;
    /**
     * 科目清单
     */
    @ApiModelProperty(value = "科目清单")
    private Set<String> itemCodes;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Set<String> getItemCodes() {
        return itemCodes;
    }

    public void setItemCodes(Set<String> itemCodes) {
        this.itemCodes = itemCodes;
    }
}
