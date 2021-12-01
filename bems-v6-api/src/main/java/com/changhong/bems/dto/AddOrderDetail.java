package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 创建预算申请单(Order)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@ApiModel(description = "创建预算申请单DTO")
public class AddOrderDetail extends BaseEntityDto implements Serializable {
    private static final long serialVersionUID = 927785272928546873L;
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
     * 币种代码
     */
    @ApiModelProperty(value = "币种代码")
    private String currencyCode;
    /**
     * 币种名称
     */
    @ApiModelProperty(value = "币种名称")
    private String currencyName;
    /**
     * 预算类型id
     */
    @ApiModelProperty(value = "预算类型id")
    private String categoryId;
    /**
     * 预算类型名称
     */
    @ApiModelProperty(value = "预算类型名称")
    private String categoryName;
    /**
     * 期间分类
     */
    @ApiModelProperty(value = "期间分类")
    private PeriodType periodType;
    /**
     * 订单类型
     */
    @ApiModelProperty(value = "订单类型")
    private OrderCategory orderCategory;
    /**
     * 申请组织id
     */
    @ApiModelProperty(value = "申请组织id")
    private String applyOrgId;
    /**
     * 申请组织代码
     */
    @ApiModelProperty(value = "申请组织代码")
    private String applyOrgCode;
    /**
     * 申请组织名称
     */
    @ApiModelProperty(value = "申请组织名称")
    private String applyOrgName;
    /**
     * 归口管理组织Id
     */
    @ApiModelProperty(value = "归口管理组织id")
    private String managerOrgId;
    /**
     * 归口管理组织代码
     */
    @ApiModelProperty(value = "归口管理组织代码")
    private String managerOrgCode;
    /**
     * 归口管理组织名称
     */
    @ApiModelProperty(value = "归口管理组织名称")
    private String managerOrgName;
    /**
     * 备注说明
     */
    @ApiModelProperty(value = "备注说明")
    private String remark;
    /**
     * 附件id
     */
    @ApiModelProperty(value = "附件id")
    private List<String> docIds;
    /**
     * 期间
     */
    @NotEmpty
    @ApiModelProperty(value = "期间")
    private Set<OrderDimension> period;
    /**
     * 预算科目
     */
    @NotEmpty
    @ApiModelProperty(value = "预算科目")
    private Set<OrderDimension> item;
    /**
     * 组织
     */
    @ApiModelProperty(value = "组织")
    private Set<OrderDimension> org;
    /**
     * 项目
     */
    @ApiModelProperty(value = "项目")
    private Set<OrderDimension> project;
    /**
     * 成本中心
     */
    @ApiModelProperty(value = "成本中心")
    private Set<OrderDimension> costCenter;
    /**
     * 自定义1
     */
    @ApiModelProperty(value = "自定义1")
    private Set<OrderDimension> udf1;
    /**
     * 自定义2
     */
    @ApiModelProperty(value = "自定义2")
    private Set<OrderDimension> udf2;
    /**
     * 自定义3
     */
    @ApiModelProperty(value = "自定义3")
    private Set<OrderDimension> udf3;
    /**
     * 自定义4
     */
    @ApiModelProperty(value = "自定义4")
    private Set<OrderDimension> udf4;
    /**
     * 自定义5
     */
    @ApiModelProperty(value = "自定义5")
    private Set<OrderDimension> udf5;

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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public OrderCategory getOrderCategory() {
        return orderCategory;
    }

    public void setOrderCategory(OrderCategory orderCategory) {
        this.orderCategory = orderCategory;
    }

    public String getApplyOrgId() {
        return applyOrgId;
    }

    public void setApplyOrgId(String applyOrgId) {
        this.applyOrgId = applyOrgId;
    }

    public String getApplyOrgCode() {
        return applyOrgCode;
    }

    public void setApplyOrgCode(String applyOrgCode) {
        this.applyOrgCode = applyOrgCode;
    }

    public String getApplyOrgName() {
        return applyOrgName;
    }

    public void setApplyOrgName(String applyOrgName) {
        this.applyOrgName = applyOrgName;
    }

    public String getManagerOrgId() {
        return managerOrgId;
    }

    public void setManagerOrgId(String managerOrgId) {
        this.managerOrgId = managerOrgId;
    }

    public String getManagerOrgCode() {
        return managerOrgCode;
    }

    public void setManagerOrgCode(String managerOrgCode) {
        this.managerOrgCode = managerOrgCode;
    }

    public String getManagerOrgName() {
        return managerOrgName;
    }

    public void setManagerOrgName(String managerOrgName) {
        this.managerOrgName = managerOrgName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<String> getDocIds() {
        return docIds;
    }

    public void setDocIds(List<String> docIds) {
        this.docIds = docIds;
    }

    public Set<OrderDimension> getPeriod() {
        return period;
    }

    public void setPeriod(Set<OrderDimension> period) {
        this.period = period;
    }

    public Set<OrderDimension> getItem() {
        return item;
    }

    public void setItem(Set<OrderDimension> item) {
        this.item = item;
    }

    public Set<OrderDimension> getOrg() {
        return org;
    }

    public void setOrg(Set<OrderDimension> org) {
        this.org = org;
    }

    public Set<OrderDimension> getProject() {
        return project;
    }

    public void setProject(Set<OrderDimension> project) {
        this.project = project;
    }

    public Set<OrderDimension> getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(Set<OrderDimension> costCenter) {
        this.costCenter = costCenter;
    }

    public Set<OrderDimension> getUdf1() {
        return udf1;
    }

    public void setUdf1(Set<OrderDimension> udf1) {
        this.udf1 = udf1;
    }

    public Set<OrderDimension> getUdf2() {
        return udf2;
    }

    public void setUdf2(Set<OrderDimension> udf2) {
        this.udf2 = udf2;
    }

    public Set<OrderDimension> getUdf3() {
        return udf3;
    }

    public void setUdf3(Set<OrderDimension> udf3) {
        this.udf3 = udf3;
    }

    public Set<OrderDimension> getUdf4() {
        return udf4;
    }

    public void setUdf4(Set<OrderDimension> udf4) {
        this.udf4 = udf4;
    }

    public Set<OrderDimension> getUdf5() {
        return udf5;
    }

    public void setUdf5(Set<OrderDimension> udf5) {
        this.udf5 = udf5;
    }
}