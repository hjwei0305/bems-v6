package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：操作类型
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-22 13:16
 */
public enum OperationType {

    /**
     * 预下达
     */
    @Remark("预下达")
    PRE_RELEASE,

    /**
     * 下达
     */
    @Remark("下达")
    RELEASE,
    /**
     * 使用
     */
    @Remark("使用")
    USE
}
