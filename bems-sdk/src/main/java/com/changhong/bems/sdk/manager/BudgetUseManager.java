package com.changhong.bems.sdk.manager;

import com.changhong.bems.sdk.client.BudgetApiClient;
import com.changhong.bems.sdk.client.BudgetItemApiClient;
import com.changhong.bems.sdk.dto.BudgetItemDto;
import com.changhong.bems.sdk.dto.BudgetRequest;
import com.changhong.bems.sdk.dto.BudgetResponse;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-08 18:14
 */
public class BudgetUseManager {

    private final BudgetApiClient budgetApi;

    private final BudgetItemApiClient itemApi;

    public BudgetUseManager(BudgetApiClient budgetApi, BudgetItemApiClient itemApi) {
        this.itemApi = itemApi;
        this.budgetApi = budgetApi;
    }

    /**
     * 使用预算
     * 包含占用和释放
     *
     * @param request 使用预算请求
     * @return 使用预算结果
     */
    public ResultData<List<BudgetResponse>> use(BudgetRequest request) {
        return budgetApi.use(request);
    }

    /**
     * 分页获取预算科目(外部系统集成专用)
     *
     * @param search 查询参数
     * @return 分页查询结果
     */
    public ResultData<PageResult<BudgetItemDto>> getBudgetItems(Search search) {
        return itemApi.getBudgetItems(search);
    }
}
