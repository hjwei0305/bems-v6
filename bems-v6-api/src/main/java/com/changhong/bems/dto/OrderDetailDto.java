package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 预算维度属性(OrderItem)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:14:00
 */
@ApiModel(description = "预算申请单行项DTO")
public class OrderDetailDto extends BaseAttributeDto {
    private static final long serialVersionUID = 466264526815699224L;
    /**
     * 预算申请单id
     */
    @ApiModelProperty(value = "预算申请单id")
    private String orderId;
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
     * 是否错误
     */
    @ApiModelProperty(value = "是否错误")
    private Boolean hasErr = Boolean.FALSE;
    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    private String errMsg;
    /**
     * 预算分解时的目标预算
     */
    private List<OrderDetailDto> children;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public Boolean getHasErr() {
        return hasErr;
    }

    public void setHasErr(Boolean hasErr) {
        this.hasErr = hasErr;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public List<OrderDetailDto> getChildren() {
        return children;
    }

    public void setChildren(List<OrderDetailDto> children) {
        this.children = children;
    }
}