package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * 预算申请单(Order)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:13:57
 */
@ApiModel(description = "预算申请单DTO")
public class OrderDto extends BaseEntityDto {
    private static final long serialVersionUID = 927785272928546873L;
    /**
     * 申请单号
     */
    @Length(max = 10)
    @ApiModelProperty(value = "申请单号")
    private String code;
    /**
     * 预算主体id
     */
    @Size(max = 36)
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
    @Size(max = 36)
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
    @NotNull
    @ApiModelProperty(value = "期间分类")
    private PeriodType periodType;
    /**
     * 订单类型
     */
    @NotNull
    @ApiModelProperty(value = "订单类型")
    private OrderCategory orderCategory;
    /**
     * 申请金额
     */
    @Digits(integer = 10, fraction = 2)
    @ApiModelProperty(value = "申请金额")
    private Double applyAmount = 0d;
    /**
     * 申请组织id
     */
    @Size(max = 36)
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
     * 是否手动生效
     */
    @ApiModelProperty(value = "是否手动生效")
    private Boolean manuallyEffective = Boolean.FALSE;
    /**
     * 备注说明
     */
    @ApiModelProperty(value = "备注说明")
    private String remark;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private OrderStatus status = OrderStatus.PREFAB;

    @ApiModelProperty(value = "创建人账号")
    protected String creatorAccount;
    @ApiModelProperty(value = "创建人名称")
    protected String creatorName;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2021-04-22 12:01:10")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createdDate;

    @ApiModelProperty(value = "维度")
    private List<DimensionDto> dimensions;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public Double getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(Double applyAmount) {
        this.applyAmount = applyAmount;
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

    public Boolean getManuallyEffective() {
        return manuallyEffective;
    }

    public OrderDto setManuallyEffective(Boolean manuallyEffective) {
        this.manuallyEffective = manuallyEffective;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCreatorAccount() {
        return creatorAccount;
    }

    public void setCreatorAccount(String creatorAccount) {
        this.creatorAccount = creatorAccount;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public List<DimensionDto> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<DimensionDto> dimensions) {
        this.dimensions = dimensions;
    }
}