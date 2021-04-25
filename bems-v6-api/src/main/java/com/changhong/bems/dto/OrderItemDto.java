package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算维度属性(OrderItem)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:14:00
 */
@ApiModel(description = "预算维度属性DTO")
public class OrderItemDto extends BaseEntityDto {
    private static final long serialVersionUID = 466264526815699224L;
    /**
     * 预算申请单id
     */
    @ApiModelProperty(value = "预算申请单id")
    private String orderId;
    /**
     * 行号
     */
    @ApiModelProperty(value = "行号")
    private String lineNumber;
    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
    private Double amount = 0d;
    /**
     * 预算池编码
     */
    @ApiModelProperty(value = "预算池编码")
    private String poolCode;
    /**
     * 预算池金额
     */
    @ApiModelProperty(value = "预算池金额")
    private Double poolAmount = 0d;
    /**
     * 来源预算池编码
     */
    @ApiModelProperty(value = "来源预算池编码")
    private String originPoolCode;
    /**
     * 来源预算池金额
     */
    @ApiModelProperty(value = "来源预算池金额")
    private Double originPoolAmount = 0d;
    /**
     * 预算期间
     */
    @ApiModelProperty(value = "预算期间")
    private String periodId;
    /**
     * 预算期间名称
     */
    @ApiModelProperty(value = "预算期间名称")
    private String periodName;
    /**
     * 预算科目
     */
    @ApiModelProperty(value = "预算科目")
    private String itemId;
    /**
     * 预算科目名称
     */
    @ApiModelProperty(value = "预算科目名称")
    private String itemName;
    /**
     * 组织
     */
    @ApiModelProperty(value = "组织")
    private String org;
    /**
     * 组织名称
     */
    @ApiModelProperty(value = "组织名称")
    private String orgName;
    /**
     * 项目
     */
    @ApiModelProperty(value = "项目")
    private String project;
    /**
     * 项目名称
     */
    @ApiModelProperty(value = "项目名称")
    private String projectName;
    /**
     * 自定义1
     */
    @ApiModelProperty(value = "自定义1")
    private String udf1;
    /**
     * 自定义1名称
     */
    @ApiModelProperty(value = "自定义1名称")
    private String udf1Name;
    /**
     * 自定义2
     */
    @ApiModelProperty(value = "自定义2")
    private String udf2;
    /**
     * 自定义2名称
     */
    @ApiModelProperty(value = "自定义2名称")
    private String udf2Name;
    /**
     * 自定义3
     */
    @ApiModelProperty(value = "自定义3")
    private String udf3;
    /**
     * 自定义3名称
     */
    @ApiModelProperty(value = "自定义3名称")
    private String udf3Name;
    /**
     * 自定义4
     */
    @ApiModelProperty(value = "自定义4")
    private String udf4;
    /**
     * 自定义4名称
     */
    @ApiModelProperty(value = "自定义4名称")
    private String udf4Name;
    /**
     * 自定义5
     */
    @ApiModelProperty(value = "自定义5")
    private String udf5;
    /**
     * 自定义5名称
     */
    @ApiModelProperty(value = "自定义5名称")
    private String udf5Name;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public Double getPoolAmount() {
        return poolAmount;
    }

    public void setPoolAmount(Double poolAmount) {
        this.poolAmount = poolAmount;
    }

    public String getOriginPoolCode() {
        return originPoolCode;
    }

    public void setOriginPoolCode(String originPoolCode) {
        this.originPoolCode = originPoolCode;
    }

    public Double getOriginPoolAmount() {
        return originPoolAmount;
    }

    public void setOriginPoolAmount(Double originPoolAmount) {
        this.originPoolAmount = originPoolAmount;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

}