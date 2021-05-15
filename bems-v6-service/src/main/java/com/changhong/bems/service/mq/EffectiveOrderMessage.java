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
    private String tenantCode;
    private String account;

    public String getOrderId() {
        return orderId;
    }

    public EffectiveOrderMessage setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public EffectiveOrderMessage setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
        return this;
    }

    public String getAccount() {
        return account;
    }

    public EffectiveOrderMessage setAccount(String account) {
        this.account = account;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EffectiveOrderMessage.class.getSimpleName() + "[", "]")
                .add("orderId='" + orderId + "'")
                .add("tenantCode='" + tenantCode + "'")
                .add("account='" + account + "'")
                .toString();
    }
}
