package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算池日志记录(PoolLog)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:13:35
 */
@ApiModel(description = "预算池日志记录DTO")
public class PoolLogDto extends BaseEntityDto {
    private static final long serialVersionUID = -14976646322413614L;
    /**
     * 预算池编码
     */
    @ApiModelProperty(value = "预算池编码")
    private String poolCode;
    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    private OperationType operation;
    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
    private BigDecimal amount = BigDecimal.ZERO;
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
     * 业务事件
     */
    @ApiModelProperty(value = "业务事件代码")
    private String eventCode;
    /**
     * 业务事件
     */
    @ApiModelProperty(value = "业务事件名称")
    private String eventName;
    /**
     * 业务id
     */
    @ApiModelProperty(value = "业务id")
    private String bizId;
    /**
     * 业务编码
     */
    @ApiModelProperty(value = "业务编码")
    private String bizCode;
    /**
     * 业务描述
     */
    @ApiModelProperty(value = "业务描述")
    private String bizRemark;
    /**
     * 租户代码
     */
    @ApiModelProperty(value = "租户代码")
    private String tenantCode;

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
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

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getBizRemark() {
        return bizRemark;
    }

    public void setBizRemark(String bizRemark) {
        this.bizRemark = bizRemark;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}