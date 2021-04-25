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
    private OperationType operationType;
    /**
     * 金额
     */
    @Column(name = "amount")
    private Double amount = 0d;
    /**
     * 操作时间
     */
    @Column(name = "operation_time")
    private LocalDateTime operationTime;
    /**
     * 操作人账号
     */
    @Column(name = "operation_user_account")
    private String operationUserAccount;
    /**
     * 操作人名称
     */
    @Column(name = "operation_user_name")
    private String operationUserName;
    /**
     * 业务来源
     */
    @Column(name = "biz_from")
    private String bizFrom;
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

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperationUserAccount() {
        return operationUserAccount;
    }

    public void setOperationUserAccount(String operationUserAccount) {
        this.operationUserAccount = operationUserAccount;
    }

    public String getOperationUserName() {
        return operationUserName;
    }

    public void setOperationUserName(String operationUserName) {
        this.operationUserName = operationUserName;
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