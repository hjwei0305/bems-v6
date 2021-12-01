package com.changhong.bems.dto;

import com.changhong.sei.annotation.Remark;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-11-28 12:08
 */
public enum Classification {
    /**
     * 组织级
     */
    @Remark("组织级")
    DEPARTMENT,
    /**
     * 项目级
     */
    @Remark("项目级")
    PROJECT,
    /**
     * 项目级
     */
    @Remark("项目级")
    COST_CENTER
}
