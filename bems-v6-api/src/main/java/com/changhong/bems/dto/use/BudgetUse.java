package com.changhong.bems.dto.use;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * 实现功能：预算占用
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 16:36
 */
@ApiModel(description = "预算占用DTO")
public class BudgetUse implements Serializable {
    private static final long serialVersionUID = -855723956309550319L;

    /**
     * 公司代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "公司代码", required = true)
    private String corpCode;
    /**
     * 占用金额
     */
    @Digits(integer = 36, fraction = 2)
    @ApiModelProperty(value = "占用金额")
    private BigDecimal amount = BigDecimal.ZERO;
    /**
     * 业务事件
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "业务事件", required = true)
    private String eventCode;
    /**
     * 业务id
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "业务id", required = true)
    private String bizId;
    /**
     * 业务代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "业务代码", required = true)
    private String bizCode;
    /**
     * 业务描述
     */
    @Size(max = 200)
    @ApiModelProperty(value = "业务描述")
    private String bizRemark;
    /**
     * 占用日期
     */
    @NotBlank
    @Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$", message = "占用日期格式yyyy-MM-dd不正确")
    @ApiModelProperty(value = "占用日期", example = "2021-05-13", required = true)
    private String date;
    /**
     * 预算科目代码
     */
    @NotBlank
    @Size(max = 30)
    @ApiModelProperty(value = "预算科目代码", required = true)
    private String item;
    /**
     * 组织ID
     */
    @Size(max = 36)
    @ApiModelProperty(value = "组织ID")
    private String org;
    /**
     * 项目
     */
    @Size(max = 36)
    @ApiModelProperty(value = "项目")
    private String project;
    /**
     * 自定义1
     */
    @Size(max = 50)
    @ApiModelProperty(value = "自定义1")
    private String udf1;
    /**
     * 自定义2
     */
    @Size(max = 50)
    @ApiModelProperty(value = "自定义2")
    private String udf2;
    /**
     * 自定义3
     */
    @Size(max = 50)
    @ApiModelProperty(value = "自定义3")
    private String udf3;
    /**
     * 自定义4
     */
    @Size(max = 50)
    @ApiModelProperty(value = "自定义4")
    private String udf4;
    /**
     * 自定义5
     */
    @Size(max = 50)
    @ApiModelProperty(value = "自定义5")
    private String udf5;

    public BudgetUse() {
    }

    public BudgetUse(String corpCode, String eventCode, String bizId, String date, String item, BigDecimal amount) {
        this.corpCode = corpCode;
        this.amount = amount;
        this.eventCode = eventCode;
        this.bizId = bizId;
        this.date = date;
        this.item = item;
    }

    public String getCorpCode() {
        return corpCode;
    }

    public BudgetUse setCorpCode(String corpCode) {
        this.corpCode = corpCode;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BudgetUse setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getEventCode() {
        return eventCode;
    }

    public BudgetUse setEventCode(String eventCode) {
        this.eventCode = eventCode;
        return this;
    }

    public String getBizId() {
        return bizId;
    }

    public BudgetUse setBizId(String bizId) {
        this.bizId = bizId;
        return this;
    }

    public String getBizCode() {
        return bizCode;
    }

    public BudgetUse setBizCode(String bizCode) {
        this.bizCode = bizCode;
        return this;
    }

    public String getBizRemark() {
        return bizRemark;
    }

    public BudgetUse setBizRemark(String bizRemark) {
        this.bizRemark = bizRemark;
        return this;
    }

    public String getDate() {
        return date;
    }

    public BudgetUse setDate(String date) {
        this.date = date;
        return this;
    }

    public String getItem() {
        return item;
    }

    public BudgetUse setItem(String item) {
        this.item = item;
        return this;
    }

    public String getOrg() {
        return org;
    }

    public BudgetUse setOrg(String org) {
        this.org = org;
        return this;
    }

    public String getProject() {
        return project;
    }

    public BudgetUse setProject(String project) {
        this.project = project;
        return this;
    }

    public String getUdf1() {
        return udf1;
    }

    public BudgetUse setUdf1(String udf1) {
        this.udf1 = udf1;
        return this;
    }

    public String getUdf2() {
        return udf2;
    }

    public BudgetUse setUdf2(String udf2) {
        this.udf2 = udf2;
        return this;
    }

    public String getUdf3() {
        return udf3;
    }

    public BudgetUse setUdf3(String udf3) {
        this.udf3 = udf3;
        return this;
    }

    public String getUdf4() {
        return udf4;
    }

    public BudgetUse setUdf4(String udf4) {
        this.udf4 = udf4;
        return this;
    }

    public String getUdf5() {
        return udf5;
    }

    public BudgetUse setUdf5(String udf5) {
        this.udf5 = udf5;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BudgetUse.class.getSimpleName() + "[", "]")
                .add("corpCode='" + corpCode + "'")
                .add("eventCode='" + eventCode + "'")
                .add("bizId='" + bizId + "'")
                .add("date='" + date + "'")
                .add("item='" + item + "'")
                .add("amount=" + amount)
                .toString();
    }
}
