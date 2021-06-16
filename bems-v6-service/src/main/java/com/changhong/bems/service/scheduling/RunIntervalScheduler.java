package com.changhong.bems.service.scheduling;

import com.changhong.bems.service.PeriodService;
import com.changhong.bems.service.PoolService;
import com.changhong.sei.core.dto.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 实现功能：定时间隔任务
 * 1.预算期间定时关闭
 * 2.预算池自动结转
 * 3.预算维度属性名称更新
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-16 14:13
 */
@Component
public class RunIntervalScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(RunIntervalScheduler.class);

    @Autowired
    private PeriodService periodService;
    @Autowired
    private PoolService poolService;

    /**
     * 定时关闭过期的预算期间
     * 每月最后一日的上午23:59触发
     */
    @Scheduled(cron = "0 59 23 28-31 * ?")
    public void closingOverduePeriod() {
        // localDate.lengthOfMonth() 本月总天数. localDate.getDayOfMonth() 本月当前天数
        LocalDate localDate = LocalDate.now();
        if (localDate.lengthOfMonth() == localDate.getDayOfMonth()) {
            LOG.info("启动定时任务-关闭过期的预算期间");
            ResultData<Void> resultData = periodService.closingOverduePeriod();
            LOG.info("关闭过期的预算期间任务完成: {}", resultData);
        }
    }

    /**
     * 预算池自动结转
     * 每天上午1:3触发
     */
    @Scheduled(cron = "0 3 1 * * ?")
    public void trundle() {
        LOG.info("启动定时任务-预算池自动结转");
        ResultData<Void> resultData = poolService.trundlePool();
        LOG.info("预算池自动结转任务完成: {}", resultData);
    }
}
