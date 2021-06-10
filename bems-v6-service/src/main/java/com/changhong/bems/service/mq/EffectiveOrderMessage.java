package com.changhong.bems.service.mq;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 23:19
 */
public class EffectiveOrderMessage implements Serializable {
    private static final long serialVersionUID = -6753321214394856181L;

    private String orderId;
    private String orderDetailId;
    private String operation;
    private String userId;
    private String account;
    private String userName;
    private String tenantCode;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(String orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EffectiveOrderMessage.class.getSimpleName() + "[", "]")
                .add("orderId='" + orderId + "'")
                .add("orderDetailId='" + orderDetailId + "'")
                .add("operation='" + operation + "'")
                .add("tenantCode='" + tenantCode + "'")
                .add("account='" + account + "'")
                .toString();
    }
}
