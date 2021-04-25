package com.changhong.bems.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 预算池金额(PoolAmount)DTO类
 *
 * @author sei
 * @since 2021-04-25 15:14:02
 */
@ApiModel(description = "预算池金额DTO")
public class PoolAmountDto extends BaseEntityDto {
    private static final long serialVersionUID = 922766860318891409L;
    /**
     * 预算池id
     */
    @ApiModelProperty(value = "预算池id")
    private String poolId;
    /**
     * 预算池编码
     */
    @ApiModelProperty(value = "预算池编码")
    private String poolCode;
    /**
     * 操作类型
     */
    @ApiModelProperty(value = "操作类型")
    private OperationType operationType;
    /**
     * 金额
     */
    @ApiModelProperty(value = "金额")
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

}