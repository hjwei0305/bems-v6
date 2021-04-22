package com.changhong.bems.dto;

/**
 * 实现功能：非自定义期间类型代码
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 13:11
 */
public enum PeriodCode {

    /**
     * 年
     */
    Y,

    /**
     * 上半年
     */
    H1,
    /**
     * 下半年
     */
    H2,

    /**
     * 一季度
     */
    Q1,
    /**
     * 二季度
     */
    Q2,
    /**
     * 三季度
     */
    Q3,
    /**
     * 四季度
     */
    Q4,

    /**
     * 一月
     */
    M1,
    /**
     * 二月
     */
    M2,
    /**
     * 三月
     */
    M3,
    /**
     * 四月
     */
    M4,
    /**
     * 五月
     */
    M5,
    /**
     * 六月
     */
    M6,
    /**
     * 七月
     */
    M7,
    /**
     * 八月
     */
    M8,
    /**
     * 九月
     */
    M9,
    /**
     * 十月
     */
    M10,
    /**
     * 十一月
     */
    M11,
    /**
     * 十二月
     */
    M12;

    @Override
    public String toString() {
        return this.name();
    }
}
