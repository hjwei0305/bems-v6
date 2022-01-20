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
     * 组织级预算: 分公司级和部门级
     */
    @Remark(value = "classification_department", comments = "组织级预算")
    DEPARTMENT,
    /**
     * 项目级预算
     */
    @Remark(value = "classification_project", comments = "项目级预算")
    PROJECT,
    /**
     * 成本中心级预算
     */
    @Remark(value = "classification_cost_center", comments = "成本中心级预算")
    COST_CENTER
}
