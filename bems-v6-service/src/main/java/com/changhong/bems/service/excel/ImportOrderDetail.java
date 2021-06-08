package com.changhong.bems.service.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.changhong.bems.dto.BaseAttributeDto;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Digits;

/**
 * 预算维度属性(OrderItem)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:14:00
 */
@ApiModel(description = "预算申请单行项DTO")
public class ImportOrderDetail extends BaseAttributeDto {
    private static final long serialVersionUID = 466264526815699224L;
    /**
     * 预算期间
     */
    @ExcelProperty(value = "预算期间", order = 1)
    private String period;
    /**
     * 预算期间名称
     */
    @ExcelProperty(value = "预算期间名称", order = 1)
    private String periodName;
    /**
     * 预算科目
     */
    @ExcelProperty(value = "预算科目", order = 1)
    private String item;
    /**
     * 预算科目名称
     */
    @ExcelProperty(value = "预算科目名称", order = 1)
    private String itemName;
    /**
     * 组织
     */
    @ExcelProperty(value = "组织", order = 1)
    private String org;
    /**
     * 组织名称
     */
    @ExcelProperty(value = "组织名称", order = 1)
    private String orgName;
    /**
     * 项目
     */
    @ExcelProperty(value = "项目", order = 1)
    private String project;
    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称", order = 1)
    private String projectName;
    /**
     * 自定义1
     */
    @ExcelProperty(value = "自定义1", order = 1)
    private String udf1;
    /**
     * 自定义1名称
     */
    @ExcelProperty(value = "自定义1名称", order = 1)
    private String udf1Name;
    /**
     * 自定义2
     */
    @ExcelProperty(value = "自定义2", order = 1)
    private String udf2;
    /**
     * 自定义2名称
     */
    @ExcelProperty(value = "自定义2名称", order = 1)
    private String udf2Name;
    /**
     * 自定义3
     */
    @ExcelProperty(value = "自定义3", order = 1)
    private String udf3;
    /**
     * 自定义3名称
     */
    @ExcelProperty(value = "自定义3名称", order = 1)
    private String udf3Name;
    /**
     * 自定义4
     */
    @ExcelProperty(value = "自定义4", order = 1)
    private String udf4;
    /**
     * 自定义4名称
     */
    @ExcelProperty(value = "自定义4名称", order = 1)
    private String udf4Name;
    /**
     * 自定义5
     */
    @ExcelProperty(value = "自定义5", order = 1)
    private String udf5;
    /**
     * 自定义5名称
     */
    @ExcelProperty(value = "自定义5名称", order = 1)
    private String udf5Name;
    /**
     * 金额
     */
    @Digits(integer = 18, fraction = 2)
    @ExcelProperty(value = "金额", order = 1)
    private Double amount = 0d;

    @Override
    public String getPeriod() {
        return period;
    }

    @Override
    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public String getPeriodName() {
        return periodName;
    }

    @Override
    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public void setItem(String item) {
        this.item = item;
    }

    @Override
    public String getItemName() {
        return itemName;
    }

    @Override
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String getOrg() {
        return org;
    }

    @Override
    public void setOrg(String org) {
        this.org = org;
    }

    @Override
    public String getOrgName() {
        return orgName;
    }

    @Override
    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    @Override
    public String getProject() {
        return project;
    }

    @Override
    public void setProject(String project) {
        this.project = project;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String getUdf1() {
        return udf1;
    }

    @Override
    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    @Override
    public String getUdf1Name() {
        return udf1Name;
    }

    @Override
    public void setUdf1Name(String udf1Name) {
        this.udf1Name = udf1Name;
    }

    @Override
    public String getUdf2() {
        return udf2;
    }

    @Override
    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    @Override
    public String getUdf2Name() {
        return udf2Name;
    }

    @Override
    public void setUdf2Name(String udf2Name) {
        this.udf2Name = udf2Name;
    }

    @Override
    public String getUdf3() {
        return udf3;
    }

    @Override
    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    @Override
    public String getUdf3Name() {
        return udf3Name;
    }

    @Override
    public void setUdf3Name(String udf3Name) {
        this.udf3Name = udf3Name;
    }

    @Override
    public String getUdf4() {
        return udf4;
    }

    @Override
    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    @Override
    public String getUdf4Name() {
        return udf4Name;
    }

    @Override
    public void setUdf4Name(String udf4Name) {
        this.udf4Name = udf4Name;
    }

    @Override
    public String getUdf5() {
        return udf5;
    }

    @Override
    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }

    @Override
    public String getUdf5Name() {
        return udf5Name;
    }

    @Override
    public void setUdf5Name(String udf5Name) {
        this.udf5Name = udf5Name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}