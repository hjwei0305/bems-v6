package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-04-25 15:40
 */
public enum OrderStatus {
    /**
     * 预制
     */
    @Remark("预制")
    PREFAB,
    /**
     * 草稿
     */
    @Remark("草稿")
    DRAFT,
    /**
     * 流程中
     */
    @Remark("流程中")
    PROCESSING,
    /**
     * 已完成
     */
    @Remark("已完成")
    COMPLETED
}
