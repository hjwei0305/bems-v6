package com.changhong.bems.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 实现功能：年度预算分析
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-08 16:10
 */
@ApiModel(description = "年度预算分析结果")
public class AnnualBudgetResponse implements Serializable {
    private static final long serialVersionUID = 927216826723414622L;
    /**
     * 预算主体id
     */
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 预算主体名称
     */
    @ApiModelProperty(value = "预算主体名称")
    private String subjectName;
    /**
     * 预算科目
     */
    @ApiModelProperty(value = "预算科目")
    private String item;
    /**
     * 预算科目名称
     */
    @ApiModelProperty(value = "预算科目名称")
    private String itemName;
    /**
     * 所属年度
     */
    @ApiModelProperty(value = "所属年度")
    private Integer year;
    /**
     * 总注入(外部)
     */
    @ApiModelProperty(value = "总注入")
    private BigDecimal injectAmount = BigDecimal.ZERO;
    /**
     * 总使用(外部)
     */
    @ApiModelProperty(value = "总使用")
    private BigDecimal usedAmount = BigDecimal.ZERO;

    public AnnualBudgetResponse() {
    }

    public AnnualBudgetResponse(String subjectId, String subjectName, Integer year, String item, String itemName, BigDecimal injectAmount, BigDecimal usedAmount) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.item = item;
        this.itemName = itemName;
        this.year = year;
        this.injectAmount = injectAmount;
        this.usedAmount = usedAmount;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getInjectAmount() {
        return injectAmount;
    }

    public void setInjectAmount(BigDecimal injectAmount) {
        this.injectAmount = injectAmount;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }
}
