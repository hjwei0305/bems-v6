package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 预算执行记录(ExecutionRecord)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:13:35
 */
@ApiModel(description = "预算执行记录DTO")
public class ExecutionRecordDto extends BaseEntityDto {
    private static final long serialVersionUID = -14976646322413614L;
    /**
     * 预算池编码
     */
    @ApiModelProperty(value = "预算池编码")
    private String poolCode;
    /**
     * 预算维度属性id
     */
    @ApiModelProperty(value = "预算维度属性id")
    private String attributeId;
    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    private OperationType operation;
    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
    private Double amount = 0d;
    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间", example = "2021-04-22")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime opTime;
    /**
     * 操作人账号
     */
    @ApiModelProperty(value = "操作人账号")
    private String opUserAccount;
    /**
     * 操作人名称
     */
    @ApiModelProperty(value = "操作人名称")
    private String opUserName;
    /**
     * 业务来源
     */
    @ApiModelProperty(value = "业务来源")
    private String bizFrom;
    /**
     * 业务单id
     */
    @ApiModelProperty(value = "业务单id")
    private String bizOrderId;
    /**
     * 业务单编码
     */
    @ApiModelProperty(value = "业务单编码")
    private String bizOrderCode;
    /**
     * 业务单行项id
     */
    @ApiModelProperty(value = "业务单行项id")
    private String bizItemId;
    /**
     * 业务单行项编码
     */
    @ApiModelProperty(value = "业务单行项编码")
    private String bizItemCode;
    /**
     * 业务事件
     */
    @ApiModelProperty(value = "业务事件")
    private String bizEvent;
    /**
     * 业务描述
     */
    @ApiModelProperty(value = "业务描述")
    private String bizRemark;

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getOpTime() {
        return opTime;
    }

    public void setOpTime(LocalDateTime opTime) {
        this.opTime = opTime;
    }

    public String getOpUserAccount() {
        return opUserAccount;
    }

    public void setOpUserAccount(String opUserAccount) {
        this.opUserAccount = opUserAccount;
    }

    public String getOpUserName() {
        return opUserName;
    }

    public void setOpUserName(String opUserName) {
        this.opUserName = opUserName;
    }

    public String getBizFrom() {
        return bizFrom;
    }

    public void setBizFrom(String bizFrom) {
        this.bizFrom = bizFrom;
    }

    public String getBizOrderId() {
        return bizOrderId;
    }

    public void setBizOrderId(String bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    public String getBizOrderCode() {
        return bizOrderCode;
    }

    public void setBizOrderCode(String bizOrderCode) {
        this.bizOrderCode = bizOrderCode;
    }

    public String getBizItemId() {
        return bizItemId;
    }

    public void setBizItemId(String bizItemId) {
        this.bizItemId = bizItemId;
    }

    public String getBizItemCode() {
        return bizItemCode;
    }

    public void setBizItemCode(String bizItemCode) {
        this.bizItemCode = bizItemCode;
    }

    public String getBizEvent() {
        return bizEvent;
    }

    public void setBizEvent(String bizEvent) {
        this.bizEvent = bizEvent;
    }

    public String getBizRemark() {
        return bizRemark;
    }

    public void setBizRemark(String bizRemark) {
        this.bizRemark = bizRemark;
    }

}