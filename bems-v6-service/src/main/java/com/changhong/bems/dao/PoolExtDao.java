package com.changhong.bems.dao;

import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.PoolQuickQueryParam;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-10-19 16:02
 */
public interface PoolExtDao {
    /**
     * 分页查询预算池
     *
     * @param search 查询对象
     * @return 分页结果
     */
    PageResult<PoolAttributeDto> queryPoolPaging(PoolQuickQueryParam search);
}
