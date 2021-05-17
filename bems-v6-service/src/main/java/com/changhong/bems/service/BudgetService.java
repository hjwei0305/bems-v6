package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.*;
import com.changhong.bems.service.strategy.DimensionMatchStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private DimensionService dimensionService;
    @Autowired
    private StrategyService strategyService;

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
        // 事件代码
        String eventCode = useBudget.getBizCode();
        // 业务id
        String bizId = useBudget.getBizId();

        // 释放原先占用
        this.freeBudget(eventCode, bizId);

        // 再按新数据占用
        ResultData<List<Pool>> poolResult = this.getOptimalBudgetPools(useBudget);
        if (poolResult.successful()) {
            BudgetResponse response = new BudgetResponse();
            response.setBizId(useBudget.getBizId());
            BudgetUseResult useResult;
            // 占用总金额
            double amount = useBudget.getAmount();
            // 已占用金额
            double useAmount = 0;

            ExecutionRecord record;
            List<Pool> pools = poolResult.getData();
            for (Pool pool : pools) {
                // 需要占用的金额 = 占用总额 -已占额
                double needAmount = amount - useAmount;
                if (needAmount == 0) {
                    break;
                }
                // 当前预算池余额
                double poolAmount = poolService.getPoolBalanceById(pool.getId());
                // 需要占用金额 >= 预算池余额
                if (needAmount >= poolAmount) {
                    // 占用全部预算池金额
                    record = new ExecutionRecord(pool.getCode(), OperationType.USE, poolAmount, useBudget.getEventCode());

                    useAmount += poolAmount;
                } else {
                    // 占用部分预算池金额
                    record = new ExecutionRecord(pool.getCode(), OperationType.USE, needAmount, useBudget.getEventCode());
                    useAmount += needAmount;
                }
                record.setSubjectId(pool.getSubjectId());
                record.setAttributeCode(pool.getAttributeCode());
                record.setBizId(useBudget.getBizId());
                record.setBizCode(useBudget.getBizCode());
                record.setBizRemark(useBudget.getBizRemark());

                // 占用记录
                poolService.recordLog(record);
                // 占用结果
                useResult = new BudgetUseResult(pool.getCode(), pool.getBalance(), record.getAmount());
                response.addUseResult(useResult);
            }
            return ResultData.success();
        } else {
            return ResultData.fail(poolResult.getMessage());
        }
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

    private ResultData<List<Pool>> getOptimalBudgetPools(BudgetUse useBudget) {
        ResultData<List<Pool>> pools = this.getBudgetPools(useBudget);
        // 按执行策略排序预算池使用优先顺序

        return null;
    }

    private ResultData<List<Pool>> getBudgetPools(BudgetUse useBudget) {
        // TODO 获取预算池
        /*
        1.按公司代码查询预算主体清单;
        2.维度匹配,匹配规则:
        a.获取所有维度及对应维度策略
        b.获取当前请求维度
        c.获取当前请求维度策略,若是一致性匹配的加入预算池查询条件;非一致性匹配的加入后续策略处理
        d.按预算主体,占用时间范围,一致性维度作为条件查询满足条件的预算池
        3.找出最优预算池
         */
        // 公司代码
        String corpCode = useBudget.getCorpCode();
        // 预算占用日期
        LocalDate happenDate = LocalDate.parse(useBudget.getDate(), DateTimeFormatter.ISO_DATE);

        Search search = Search.createSearch();
        // 公司代码
        search.addFilter(new SearchFilter(PoolAttribute.FIELD_CORP_CODE, corpCode));
        //有效期
        search.addFilter(new SearchFilter(PoolAttribute.FIELD_START_DATE, happenDate, SearchFilter.Operator.LE));
        search.addFilter(new SearchFilter(PoolAttribute.FIELD_END_DATE, happenDate, SearchFilter.Operator.GE));
        // 启用
        search.addFilter(new SearchFilter(PoolAttribute.FIELD_ACTIVE, Boolean.TRUE));
        // 允许使用(业务可用)
        search.addFilter(new SearchFilter(PoolAttribute.FIELD_USE, Boolean.TRUE));

        // 按维度策略生成过滤条件
        Map<String, String> dimensionAttributes = this.getDimensionAttributes(useBudget);
        for (Map.Entry<String, String> entry : dimensionAttributes.entrySet()) {
            search.addFilter(this.doDimensionStrategy(entry.getKey(), entry.getValue()));
        }

        // 按条件查询满足的预算池
        List<Pool> pools = poolService.findByFilters(search);
        return ResultData.success(pools);
    }

    /**
     * 按属性维度获取
     * 主要用于预算使用时,无期间维度查找预算
     */
    public Map<String, String> getDimensionAttributes(BudgetUse use) {
        // 占用的维度代码
        Map<String, String> dimensionMap = new HashMap<>();
        // 预算科目
        String item = use.getItem();
        if (Objects.nonNull(item)) {
            item = item.trim();
            if (StringUtils.isNotBlank(item) && !StringUtils.equalsIgnoreCase(Constants.NONE, item)) {
                dimensionMap.put(DimensionAttribute.FIELD_ITEM, item);
            }
        }
        // 组织机构
        String org = use.getOrg();
        if (Objects.nonNull(org)) {
            org = org.trim();
            if (StringUtils.isNotBlank(org) && !StringUtils.equalsIgnoreCase(Constants.NONE, org)) {
                dimensionMap.put(DimensionAttribute.FIELD_ORG, org);
            }
        }
        // 预算项目
        String project = use.getProject();
        if (Objects.nonNull(project)) {
            project = project.trim();
            if (StringUtils.isNotBlank(project) && !StringUtils.equalsIgnoreCase(Constants.NONE, project)) {
                dimensionMap.put(DimensionAttribute.FIELD_PROJECT, project);
            }
        }
        // 自定义1
        String udf1 = use.getUdf1();
        if (Objects.nonNull(udf1)) {
            udf1 = udf1.trim();
            if (StringUtils.isNotBlank(udf1) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf1)) {
                dimensionMap.put(DimensionAttribute.FIELD_UDF1, udf1);
            }
        }
        // 自定义2
        String udf2 = use.getUdf2();
        if (Objects.nonNull(udf2)) {
            udf2 = udf2.trim();
            if (StringUtils.isNotBlank(udf2) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf2)) {
                dimensionMap.put(DimensionAttribute.FIELD_UDF2, udf2);
            }
        }
        // 自定义3
        String udf3 = use.getUdf3();
        if (Objects.nonNull(udf3)) {
            udf3 = udf3.trim();
            if (StringUtils.isNotBlank(udf3) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf3)) {
                dimensionMap.put(DimensionAttribute.FIELD_UDF3, udf3);
            }
        }
        // 自定义4
        String udf4 = use.getUdf4();
        if (Objects.nonNull(udf4)) {
            udf4 = udf4.trim();
            if (StringUtils.isNotBlank(udf4) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf4)) {
                dimensionMap.put(DimensionAttribute.FIELD_UDF4, udf4);
            }
        }
        // 自定义5
        String udf5 = use.getUdf5();
        if (Objects.nonNull(udf5)) {
            udf5 = udf5.trim();
            if (StringUtils.isNotBlank(udf5) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf5)) {
                dimensionMap.put(DimensionAttribute.FIELD_UDF5, udf5);
            }
        }
        return dimensionMap;
    }

    private SearchFilter doDimensionStrategy(String dimCode, String dimValue) {
        Dimension dimension = dimensionService.findByCode(dimCode);
        if (Objects.isNull(dimension)) {
            // 维度[{0}]不存在
            throw new ServiceException(ContextUtil.getMessage("dimension_00002", dimCode));
        }
        Strategy strategy = strategyService.findOne(dimension.getStrategyId());
        if (Objects.isNull(strategy)) {
            // 策略[{0}]不存在!
            throw new ServiceException(ContextUtil.getMessage("strategy_00004", dimension.getStrategyId()));
        }

        String className = strategy.getClassPath();
        try {
            Class<?> clazz = Class.forName(className);
            if (DimensionMatchStrategy.class.isAssignableFrom(clazz)) {
                DimensionMatchStrategy matchStrategy = (DimensionMatchStrategy) clazz.newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return new SearchFilter(dimCode, dimValue);
    }
}
