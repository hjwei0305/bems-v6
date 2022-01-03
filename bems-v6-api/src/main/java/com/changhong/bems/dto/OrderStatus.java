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
    @Remark(value = "OrderStatus_PREFAB", comments = "预制")
    PREFAB,
    /**
     * 草稿
     */
    @Remark(value = "OrderStatus_DRAFT", comments = "草稿")
    DRAFT,
    // /**
    //  * 确认中
    //  */
    // @Remark(value = "OrderStatus_CONFIRMING", comments = "确认中")
    // CONFIRMING,
    // /**
    //  * 撤销中
    //  */
    // @Remark(value = "OrderStatus_CANCELING", comments = "撤销中")
    // CANCELING,
    // /**
    //  * 已确认
    //  */
    // @Remark(value = "OrderStatus_CONFIRMED", comments = "已确认")
    // CONFIRMED,
    /**
     * 审批中
     */
    @Remark(value = "OrderStatus_APPROVING", comments = "审批中")
    APPROVING,
    /**
     * 生效中
     */
    @Remark(value = "OrderStatus_EFFECTING", comments = "生效中")
    EFFECTING,
    /**
     * 已生效
     */
    @Remark(value = "OrderStatus_COMPLETED", comments = "已生效")
    COMPLETED
}
