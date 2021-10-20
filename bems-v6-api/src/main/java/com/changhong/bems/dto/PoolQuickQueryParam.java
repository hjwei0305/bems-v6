package com.changhong.bems.dto;

import com.changhong.sei.core.dto.serach.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Set;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-19 21:53
 */
@ApiModel("预算池分页查询参数")
public class PoolQuickQueryParam implements Serializable {
    private static final long serialVersionUID = 1482015432192790921L;
    /**
     * 预算主体id
     */
    @NotBlank
    @ApiModelProperty(value = "预算主体id")
    private String subjectId;
    /**
     * 预算主体id
     */
    @Min(2000)
    @Max(2099)
    @ApiModelProperty(value = "预算主体id")
    private Integer year;
    /**
     * 期间类型
     */
    @ApiModelProperty(value = "期间类型")
    // @JsonSerialize(using = EnumJsonSerializer.class)
    private PeriodType periodType;
    /**
     * 预算科目代码清单
     */
    @ApiModelProperty(value = "预算科目代码清单")
    private Set<String> itemCodes;
    /**
     * 预算期间id清单
     */
    @ApiModelProperty(value = "预算期间id清单")
    private Set<String> periodIds;
    /**
     * 组织机构id清单
     */
    @ApiModelProperty(value = "组织机构id清单")
    private Set<String> orgIds;
    /**
     * 项目代码清单
     */
    @ApiModelProperty(value = "项目代码清单")
    private Set<String> projectCodes;
    /**
     * 自定义维度1清单
     */
    @ApiModelProperty(value = "自定义维度1清单")
    private Set<String> udf1s;
    /**
     * 自定义维度2清单
     */
    @ApiModelProperty(value = "自定义维度2清单")
    private Set<String> udf2s;
    /**
     * 自定义维度3清单
     */
    @ApiModelProperty(value = "自定义维度3清单")
    private Set<String> udf3s;
    /**
     * 自定义维度4清单
     */
    @ApiModelProperty(value = "自定义维度4清单")
    private Set<String> udf4s;
    /**
     * 自定义维度5清单
     */
    @ApiModelProperty(value = "自定义维度5清单")
    private Set<String> udf5s;

    /**
     * 快速搜索关键字
     */
    private String quickSearchValue;
    /**
     * 分页信息
     */
    private PageInfo pageInfo = new PageInfo();

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

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public Set<String> getItemCodes() {
        return itemCodes;
    }

    public void setItemCodes(Set<String> itemCodes) {
        this.itemCodes = itemCodes;
    }

    public Set<String> getPeriodIds() {
        return periodIds;
    }

    public void setPeriodIds(Set<String> periodIds) {
        this.periodIds = periodIds;
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

    public String getQuickSearchValue() {
        return quickSearchValue;
    }

    public void setQuickSearchValue(String quickSearchValue) {
        this.quickSearchValue = quickSearchValue;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
