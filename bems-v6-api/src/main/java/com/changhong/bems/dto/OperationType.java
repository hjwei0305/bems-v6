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
     * 注入
     */
    @Remark("注入")
    RELEASE,
    /**
     * 使用
     */
    @Remark("使用")
    USE,
    /**
     * 释放
     */
    @Remark("释放")
    FREED
}
