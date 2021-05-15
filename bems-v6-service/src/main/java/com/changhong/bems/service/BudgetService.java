package com.changhong.bems.service;

import com.changhong.bems.dto.BudgetFree;
import com.changhong.bems.dto.BudgetRequest;
import com.changhong.bems.dto.BudgetResponse;
import com.changhong.bems.dto.BudgetUse;
import com.changhong.sei.core.dto.ResultData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
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
     * @return 使用预算结果(只对占用预算结果进行返回, 释放不返回结果)
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultData<List<BudgetResponse>> use(BudgetRequest request) {
        ResultData<List<BudgetResponse>> result = ResultData.success();
        /*
        1.处理释放数据
        2.检查占用是否需要释放处理
        2.1.若需要释放处理,则进行是否处理
        2.2.若不需要释放处理,则进行占用处理
        3.占用处理
         */
        // 预算释放数据
        List<BudgetFree> freeList = request.getFreeList();
        if (CollectionUtils.isNotEmpty(freeList)) {
            ResultData<Void> freeResult = this.freeBudget(freeList);
            if (freeResult.failed()) {
                // 回滚事务
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResultData.fail(freeResult.getMessage());
            }
        }

        // 预算占用数据
        List<BudgetUse> useList = request.getUseList();
        if (CollectionUtils.isNotEmpty(useList)) {
            result = this.useBudget(useList);
            if (result.failed()) {
                // 回滚事务
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResultData.fail(result.getMessage());
            }
        }
        return result;
    }

    /**
     * 释放预算
     *
     * @param freeList 需要释放的清单
     * @return 返回释放结果
     */
    private ResultData<Void> freeBudget(List<BudgetFree> freeList) {
        // TODO 释放
        return ResultData.success();
    }

    /**
     * 占用预算
     *
     * @param useList 占用清单
     * @return 返回占用结果
     */
    private ResultData<List<BudgetResponse>> useBudget(List<BudgetUse> useList) {
        List<BudgetResponse> responses = new ArrayList<>();
        ResultData<BudgetResponse> resultData;
        for (BudgetUse budgetUse : useList) {
            resultData = this.useBudget(budgetUse);
            if (resultData.failed()) {
                return ResultData.fail(resultData.getMessage());
            } else {
                responses.add(resultData.getData());
            }
        }
        return ResultData.success(responses);
    }

    /**
     * 占用预算
     * @param useBudget 占用数据
     * @return 返回占用结果
     */
    private ResultData<BudgetResponse> useBudget(BudgetUse useBudget) {
        // 检查占用是否需要释放(编辑情况:释放原先占用,再按新数据占用)


        // TODO 占用
        return ResultData.success();
    }
}
