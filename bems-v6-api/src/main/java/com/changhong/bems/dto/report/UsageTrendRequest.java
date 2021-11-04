package com.changhong.bems.dto.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-04 13:33
 */
@ApiModel(description = "预算执行趋势分析查询")
public class UsageTrendRequest implements Serializable {
    private static final long serialVersionUID = 1292224557325418654L;
    /**
     * 预算主体id
     */
    @NotBlank
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 所属年度
     */
    @NotEmpty
    @ApiModelProperty(value = "所属年度")
    private Integer[] year;
    /**
     * 科目清单
     */
    @NotBlank
    @ApiModelProperty(value = "科目代码")
    private String item;
    /**
     * 组织id
     */
    @ApiModelProperty(value = "组织id")
    private String org;
    /**
     * 项目号
     */
    @ApiModelProperty(value = "项目号")
    private String project;
    /**
     * udf1
     */
    @ApiModelProperty(value = "udf1")
    private String udf1;
    /**
     * udf2
     */
    @ApiModelProperty(value = "udf2")
    private String udf2;
    /**
     * udf3
     */
    @ApiModelProperty(value = "udf3")
    private String udf3;
    /**
     * udf4
     */
    @ApiModelProperty(value = "udf4")
    private String udf4;
    /**
     * udf5
     */
    @ApiModelProperty(value = "udf5")
    private String udf5;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer[] getYear() {
        return year;
    }

    public void setYear(Integer[] year) {
        this.year = year;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUdf1() {
        return udf1;
    }

    public void setUdf1(String udf1) {
        this.udf1 = udf1;
    }

    public String getUdf2() {
        return udf2;
    }

    public void setUdf2(String udf2) {
        this.udf2 = udf2;
    }

    public String getUdf3() {
        return udf3;
    }

    public void setUdf3(String udf3) {
        this.udf3 = udf3;
    }

    public String getUdf4() {
        return udf4;
    }

    public void setUdf4(String udf4) {
        this.udf4 = udf4;
    }

    public String getUdf5() {
        return udf5;
    }

    public void setUdf5(String udf5) {
        this.udf5 = udf5;
    }
}
