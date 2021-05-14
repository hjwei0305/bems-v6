package com.changhong.bems.service;

import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:41
 */
@Service
public class BudgetService {

    /**
     * 使用预算
     * 包含占用和释放
     *
     * @param request 使用预算请求
     * @return 使用预算结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<List<BudgetResponse>> use(BudgetRequest request) {
        return null;
    }
}
