package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：订单分类
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 13:20
 */
public enum OrderCategory {
    /**
     * 下达注入
     */
    @Remark("下达注入")
    INJECTION,
    /**
     * 下达调整
     */
    @Remark("下达调整")
    ADJUSTMENT
}
