package com.changhong.bems.entity;

import com.changhong.bems.dto.OperationType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算执行记录(ExecutionRecord)实体类
 *
 * @author sei
 * @since 2021-04-25 15:10:03
 */
@Entity
@Table(name = "execution_record")
@DynamicInsert
@DynamicUpdate
public class ExecutionRecord extends BaseAuditableEntity implements Serializable {
    private static final long serialVersionUID = -28943145565423431L;
    /**
     * 预算池编码
     */
    @Column(name = "pool_code")
    private String poolCode;
    /**
     * 预算维度属性id
     */
    @Column(name = "attribute_id")
    private String attributeId;
    /**
     * 操作类型
     */
    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private OperationType operation;
    /**
     * 金额
     */
    @Column(name = "amount")
    private Double amount = 0d;
    /**
     * 操作时间
     */
    @Column(name = "operation_time")
    private LocalDateTime opTime;
    /**
     * 操作人账号
     */
    @Column(name = "operation_user_account")
    private String opUserAccount;
    /**
     * 操作人名称
     */
    @Column(name = "operation_user_name")
    private String opUserName;
    /**
     * 业务单id
     */
    @Column(name = "biz_order_id")
    private String bizOrderId;
    /**
     * 业务单编码
     */
    @Column(name = "biz_order_code")
    private String bizOrderCode;
    /**
     * 业务单行项id
     */
    @Column(name = "biz_item_id")
    private String bizItemId;
    /**
     * 业务单行项编码
     */
    @Column(name = "biz_item_code")
    private String bizItemCode;
    /**
     * 业务事件
     */
    @Column(name = "biz_event")
    private String bizEvent;
    /**
     * 业务描述
     */
    @Column(name = "biz_remark")
    private String bizRemark;

    public ExecutionRecord() {
    }

    public ExecutionRecord(String poolCode, OperationType operation, Double amount, String bizEvent) {
        this.poolCode = poolCode;
        this.operation = operation;
        this.amount = amount;
        this.bizEvent = bizEvent;
    }

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