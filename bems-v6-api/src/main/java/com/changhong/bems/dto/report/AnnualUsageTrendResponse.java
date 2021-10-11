package com.changhong.bems.dto.report;

import com.changhong.bems.dto.OperationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-11 17:10
 */
@ApiModel(description = "年度预算分析结果")
public class AnnualUsageTrendResponse implements Serializable {
    private static final long serialVersionUID = 4481613143734239703L;
    /**
     * 所属年度
     */
    @ApiModelProperty(value = "所属年度")
    private Integer year;
    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间")
    private LocalDateTime opTime;
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

    public AnnualUsageTrendResponse() {
    }

    public AnnualUsageTrendResponse(Integer year, LocalDateTime opTime, OperationType operation, BigDecimal amount) {
        this.year = year;
        this.opTime = opTime;
        this.operation = operation;
        this.amount = amount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDateTime getOpTime() {
        return opTime;
    }

    public void setOpTime(LocalDateTime opTime) {
        this.opTime = opTime;
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
}
