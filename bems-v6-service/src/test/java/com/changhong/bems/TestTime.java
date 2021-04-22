package com.changhong.bems;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-23 00:41
 */
public class TestTime {

    @Test
    public void test() {
        int year = 2021;
        LocalDate localDate = LocalDate.of(year, 1, 1);
        System.out.println(localDate);
        LocalDate localDate1 = localDate.withMonth(2);
        System.out.println(localDate1.withDayOfMonth(localDate1.lengthOfMonth()));
    }
}
