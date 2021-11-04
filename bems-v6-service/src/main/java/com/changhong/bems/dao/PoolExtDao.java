package com.changhong.bems.dao;

import com.changhong.bems.dto.PoolAttributeDto;
import com.changhong.bems.dto.PoolQuickQueryParam;
import com.changhong.bems.dto.report.ExecutionAnalysisRequest;
import com.changhong.bems.dto.report.ExecutionAnalysisResponse;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;

import java.util.List;

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

    /**
     * 预算分析报表数据
     *
     * @param request 预算分析报表数据查询
     * @return 预算分析报表数据结果
     */
    List<ExecutionAnalysisResponse> executionAnalysis(ExecutionAnalysisRequest request);

}
