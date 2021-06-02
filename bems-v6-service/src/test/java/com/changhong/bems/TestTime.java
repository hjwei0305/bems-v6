package com.changhong.bems;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-23 00:41
 */
public class TestTime {

    @Test
    public void testDays() {
        System.out.println(TimeUnit.DAYS.toMillis(1));
    }

    @Test
    public void test() {
        int year = 2021;
        LocalDate localDate = LocalDate.of(year, 1, 1);
        System.out.println(localDate);
        LocalDate localDate1 = localDate.withMonth(2);
        System.out.println(localDate1.withDayOfMonth(localDate1.lengthOfMonth()));
    }

    @Test
    public void testDuration() {
        LocalDateTime startTime = LocalDateTime.of(2021, 5, 8, 8, 50, 0);
        LocalDateTime endTime = LocalDateTime.now();
        System.out.println(startTime);
        System.out.println(endTime);
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("耗时(ms): " + duration.toMillis());
        System.out.println("耗时(s)" + duration.toMillis()/1000);
        System.out.println("耗时(s)" + duration.toMinutes());
    }
}
