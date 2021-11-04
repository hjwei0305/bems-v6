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
public class ExecutionAnalysisRequest implements Serializable {
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
    /**
     * 组织id清单
     */
    @ApiModelProperty(value = "组织id清单")
    private Set<String> orgIds;
    /**
     * 项目号清单
     */
    @ApiModelProperty(value = "项目号清单")
    private Set<String> projectCodes;
    /**
     * udf1清单
     */
    @ApiModelProperty(value = "udf1清单")
    private Set<String> udf1s;
    /**
     * udf2清单
     */
    @ApiModelProperty(value = "udf2清单")
    private Set<String> udf2s;
    /**
     * udf3清单
     */
    @ApiModelProperty(value = "udf3清单")
    private Set<String> udf3s;
    /**
     * udf4清单
     */
    @ApiModelProperty(value = "udf4清单")
    private Set<String> udf4s;
    /**
     * udf5清单
     */
    @ApiModelProperty(value = "udf5清单")
    private Set<String> udf5s;

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

    public Set<String> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(Set<String> orgIds) {
        this.orgIds = orgIds;
    }

    public Set<String> getProjectCodes() {
        return projectCodes;
    }

    public void setProjectCodes(Set<String> projectCodes) {
        this.projectCodes = projectCodes;
    }

    public Set<String> getUdf1s() {
        return udf1s;
    }

    public void setUdf1s(Set<String> udf1s) {
        this.udf1s = udf1s;
    }

    public Set<String> getUdf2s() {
        return udf2s;
    }

    public void setUdf2s(Set<String> udf2s) {
        this.udf2s = udf2s;
    }

    public Set<String> getUdf3s() {
        return udf3s;
    }

    public void setUdf3s(Set<String> udf3s) {
        this.udf3s = udf3s;
    }

    public Set<String> getUdf4s() {
        return udf4s;
    }

    public void setUdf4s(Set<String> udf4s) {
        this.udf4s = udf4s;
    }

    public Set<String> getUdf5s() {
        return udf5s;
    }

    public void setUdf5s(Set<String> udf5s) {
        this.udf5s = udf5s;
    }
}
