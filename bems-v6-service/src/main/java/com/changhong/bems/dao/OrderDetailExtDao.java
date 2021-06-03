package com.changhong.bems.dao;

import com.changhong.bems.dto.SplitDetailQuickQueryParam;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dto.serach.PageResult;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-03 18:23
 */
public interface OrderDetailExtDao {

    /**
     * 分页查询预算分解上级期间预算
     *
     * @param param 查询参数
     * @return 上级期间预算
     */
    PageResult<OrderDetail> querySplitGroup(SplitDetailQuickQueryParam param);
}
