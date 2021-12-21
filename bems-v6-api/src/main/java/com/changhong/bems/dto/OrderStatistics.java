package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 13:51
 */
@ApiModel(description = "预算申请单处理统计信息DTO")
public class OrderStatistics implements Serializable {
    private static final long serialVersionUID = -321346739138800880L;
    /**
     * 订单id
     */
    @ApiModelProperty(value = "订单id")
    private String orderId;
    /**
     * 总数
     */
    @ApiModelProperty(value = "总数")
    private int total = 0;
    /**
     * 成功数
     */
    @ApiModelProperty(value = "成功数")
    private int successes = 0;
    /**
     * 失败数
     */
    @ApiModelProperty(value = "失败数")
    private int failures = 0;

    public OrderStatistics() {
    }

    public OrderStatistics(String orderId, int total) {
        this.orderId = orderId;
        this.total = total;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getTotal() {
        return total;
    }

    public int getSuccesses() {
        return successes;
    }

    public void setSuccesses(int successes) {
        this.successes = successes;
    }

    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    public void addSuccesses() {
        this.successes++;
    }

    public void addFailures() {
        this.failures++;
    }

    public boolean getFinish() {
        return total - successes - failures == 0;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrderStatistics.class.getSimpleName() + "[", "]")
                .add("orderId=" + orderId)
                .add("total=" + total)
                .add("successes=" + successes)
                .add("failures=" + failures)
                .toString();
    }
}
