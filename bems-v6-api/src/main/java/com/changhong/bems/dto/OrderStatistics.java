package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
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
    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;

    public OrderStatistics() {
    }

    public OrderStatistics(int total, LocalDateTime startTime) {
        this.total = total;
        this.startTime = startTime;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public long getDuration() {
        if (null == startTime) {
            return 0;
        } else {
            return Duration.between(startTime, LocalDateTime.now()).toMillis();
        }
    }

    public long getEstimatedTime() {
        /*
        共100个
        已处理80个
        耗时60秒
        每个耗时 60/80 = 0.75 秒/个
        预计完成时间 = (100 - 80) * 0.75
         */
        int processed = getSuccesses() + getFailures();
        if (processed > 0) {
            return (getTotal() - processed) * (getDuration() / processed);
        } else {
            return -1;
        }
    }

    public void addSuccesses() {
        this.successes++;
    }

    public void addFailures() {
        this.failures++;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", OrderStatistics.class.getSimpleName() + "[", "]")
                .add("total=" + total)
                .add("successes=" + successes)
                .add("failures=" + failures)
                .add("startTime=" + startTime)
                .toString();
    }
}
