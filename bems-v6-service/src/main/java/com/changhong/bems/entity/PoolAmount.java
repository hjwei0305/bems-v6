package com.changhong.bems.entity;

import com.changhong.bems.dto.OperationType;
import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.core.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 预算池金额(PoolAmount)实体类
 *
 * @author sei
 * @since 2021-04-25 15:14:00
 */
@Entity
@Table(name = "pool_amount")
@DynamicInsert
@DynamicUpdate
public class PoolAmount extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 434318292053003122L;
    public static final String FIELD_POOL_ID = "poolId";
    public static final String FIELD_POOL_CODE = "poolCode";
    public static final String FIELD_OPERATION = "operation";
    /**
     * 预算池id
     */
    @Column(name = "pool_id")
    private String poolId;
    /**
     * 预算池编码
     */
    @Column(name = "pool_code")
    private String poolCode;
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


    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public void setPoolCode(String poolCode) {
        this.poolCode = poolCode;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operationType) {
        this.operation = operationType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}