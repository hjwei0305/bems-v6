package com.changhong.bems.sdk.manager;

import com.changhong.bems.sdk.client.BudgetApiClient;
import com.changhong.bems.sdk.dto.BudgetRequest;
import com.changhong.bems.sdk.dto.BudgetResponse;
import com.changhong.sei.core.dto.ResultData;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-09-08 18:14
 */
public class BudgetUseManager {

    private final BudgetApiClient budgetApi;

    public BudgetUseManager(BudgetApiClient budgetApi) {
        this.budgetApi = budgetApi;
    }

    /**
     * 使用预算
     * 包含占用和释放
     *
     * @param request 使用预算请求
     * @return 使用预算结果
     */
    public ResultData<List<BudgetResponse>> use(@RequestBody @Validated BudgetRequest request) {
        return budgetApi.use(request);
    }


}
