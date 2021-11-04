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
public class ExecutionAnalysisResponse implements Serializable {
    private static final long serialVersionUID = 927216826723414622L;

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
     * 组织
     */
    @ApiModelProperty(value = "组织")
    protected String org;
    /**
     * 组织名称
     */
    @ApiModelProperty(value = "组织名称")
    protected String orgName;
    /**
     * 项目
     */
    @ApiModelProperty(value = "项目")
    protected String project;
    /**
     * 项目名称
     */
    @ApiModelProperty(value = "项目名称")
    protected String projectName;
    /**
     * 自定义1
     */
    @ApiModelProperty(value = "自定义1")
    protected String udf1;
    /**
     * 自定义1名称
     */
    @ApiModelProperty(value = "自定义1名称")
    protected String udf1Name;
    /**
     * 自定义2
     */
    @ApiModelProperty(value = "自定义2")
    protected String udf2;
    /**
     * 自定义2名称
     */
    @ApiModelProperty(value = "自定义2名称")
    protected String udf2Name;
    /**
     * 自定义3
     */
    @ApiModelProperty(value = "自定义3")
    protected String udf3;
    /**
     * 自定义3名称
     */
    @ApiModelProperty(value = "自定义3名称")
    protected String udf3Name;
    /**
     * 自定义4
     */
    @ApiModelProperty(value = "自定义4")
    protected String udf4;
    /**
     * 自定义4名称
     */
    @ApiModelProperty(value = "自定义4名称")
    protected String udf4Name;
    /**
     * 自定义5
     */
    @ApiModelProperty(value = "自定义5")
    protected String udf5;
    /**
     * 自定义5名称
     */
    @ApiModelProperty(value = "自定义5名称")
    protected String udf5Name;

    /**
     * 初始注入
     * 通过注入且新产生预算池时的金额,作为初始注入金额,用于多维分析的差异计算
     */
    @ApiModelProperty(value = "初始注入")
    private BigDecimal initInjectAmount = BigDecimal.ZERO;
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

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf1Name() {
        return udf1Name;
    }

    public void setUdf1Name(String udf1Name) {
        this.udf1Name = udf1Name;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf2Name() {
        return udf2Name;
    }

    public void setUdf2Name(String udf2Name) {
        this.udf2Name = udf2Name;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getUdf3Name() {
        return udf3Name;
    }

    public void setUdf3Name(String udf3Name) {
        this.udf3Name = udf3Name;
    }

    public String getUdf4() {
        return udf4;
    }

    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    public String getUdf4Name() {
        return udf4Name;
    }

    public void setUdf4Name(String udf4Name) {
        this.udf4Name = udf4Name;
    }

    public String getUdf5() {
        return udf5;
    }

    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }

    public String getUdf5Name() {
        return udf5Name;
    }

    public void setUdf5Name(String udf5Name) {
        this.udf5Name = udf5Name;
    }

    public BigDecimal getInitInjectAmount() {
        return initInjectAmount;
    }

    public void setInitInjectAmount(BigDecimal initInjectAmount) {
        this.initInjectAmount = initInjectAmount;
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
