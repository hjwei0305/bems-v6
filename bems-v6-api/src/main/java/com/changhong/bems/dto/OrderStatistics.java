package com.changhong.bems.dto;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 13:51
 */
@ApiModel(description = "预算申请单处理统计信息DTO")
public class OrderStatistics extends OrderMessage implements Serializable {
    private static final long serialVersionUID = -321346739138800880L;

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

    public boolean getFinish() {
        return total - successes - failures == 0;
    }
}
