package com.changhong.bems.service;

import com.changhong.bems.dto.*;
import com.changhong.bems.entity.ExecutionRecord;
import com.changhong.bems.entity.Pool;
import com.changhong.sei.core.dto.ResultData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:41
 */
@Service
public class BudgetService {

    @Autowired
    private PoolService poolService;
    @Autowired
    private ExecutionRecordService executionRecordService;

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
        for (BudgetFree free : freeList) {
            if (0 == free.getAmount()) {
                // 按原先占用记录释放全部金额
                this.freeBudget(free.getEventCode(), free.getBizId());
            } else {
                // 按原先占用记录释放指定金额
                this.freeBudget(free.getEventCode(), free.getBizId(), free.getAmount());
            }
        }
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
     * 编辑情况:释放原先占用,再按新数据占用
     *
     * @param useBudget 占用数据
     * @return 返回占用结果
     */
    private ResultData<BudgetResponse> useBudget(BudgetUse useBudget) {
        // 公司代码
        String corpCode = useBudget.getCorpCode();
        // 事件代码
        String eventCode = useBudget.getBizCode();
        // 业务id
        String bizId = useBudget.getBizId();

        // 释放原先占用
        this.freeBudget(eventCode, bizId);

        // 再按新数据占用
        // TODO 获取预算池
        Pool pool = null;
        // 占用记录
        ExecutionRecord record = new ExecutionRecord(pool.getCode(), OperationType.USE, useBudget.getAmount(), useBudget.getEventCode());
        record.setSubjectId(pool.getSubjectId());
        record.setAttributeCode(pool.getAttributeCode());
        record.setBizId(useBudget.getBizId());
        record.setBizCode(useBudget.getBizCode());
        record.setBizRemark(useBudget.getBizRemark());
        poolService.recordLog(record);
        return ResultData.success();
    }

    /**
     * 释放全部占用金额
     *
     * @param eventCode 业务事件代码
     * @param bizId     业务id
     */
    private void freeBudget(String eventCode, String bizId) {
        // 检查占用是否需要释放
        List<ExecutionRecord> records = executionRecordService.getUseRecords(eventCode, bizId);
        if (CollectionUtils.isNotEmpty(records)) {
            ExecutionRecord newRecord;
            for (ExecutionRecord record : records) {
                newRecord = record.clone();
                newRecord.setId(null);
                newRecord.setOperation(OperationType.FREED);
                newRecord.setOpUserAccount(null);
                newRecord.setOpUserName(null);
                newRecord.setBizRemark("释放: " + newRecord.getBizRemark());
                // 释放记录
                poolService.recordLog(newRecord);
            }
        }
    }

    /**
     * 释放指定金额
     *
     * @param eventCode 业务事件代码
     * @param bizId     业务id
     * @param amount    释放金额
     */
    private void freeBudget(String eventCode, String bizId, double amount) {
        // 检查占用是否需要释放
        ExecutionRecord record = executionRecordService.getUseRecord(eventCode, bizId);
        if (Objects.nonNull(record)) {
            ExecutionRecord newRecord = record.clone();
            newRecord.setAmount(amount);
            newRecord.setId(null);
            newRecord.setOperation(OperationType.FREED);
            newRecord.setOpUserAccount(null);
            newRecord.setOpUserName(null);
            newRecord.setBizRemark("释放: " + newRecord.getBizRemark());
            // 释放记录
            poolService.recordLog(newRecord);
        }
    }
}
