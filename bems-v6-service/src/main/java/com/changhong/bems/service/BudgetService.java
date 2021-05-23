package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.*;
import com.changhong.bems.entity.*;
import com.changhong.bems.service.strategy.BudgetExecutionStrategy;
import com.changhong.bems.service.strategy.DimensionMatchStrategy;
import com.changhong.bems.service.vo.PoolLevel;
import com.changhong.bems.service.vo.SubjectStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.util.ArithUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-14 17:41
 */
@Service
public class BudgetService {
    private static final Logger LOG = LoggerFactory.getLogger(BudgetService.class);

    @Autowired
    private PoolService poolService;
    @Autowired
    private ExecutionRecordService executionRecordService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectItemService subjectItemService;
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
        // TODO 检查参数合法性
        // 事件代码
        String eventCode = useBudget.getBizCode();
        // 业务id
        String bizId = useBudget.getBizId();

        // 释放原先占用
        this.freeBudget(eventCode, bizId);

        // 再按新数据占用
        ResultData<Set<PoolLevel>> poolResult = this.getOptimalBudgetPools(useBudget);
        if (poolResult.successful()) {
            BudgetResponse response = new BudgetResponse();
            response.setBizId(useBudget.getBizId());
            // 占用总金额
            double amount = useBudget.getAmount();
            // 已占用金额
            double useAmount = 0;

            ExecutionRecord record;
            Set<PoolLevel> pools = poolResult.getData();
            for (PoolLevel poolLevel : pools) {
                // 预算池代码
                String poolCode = poolLevel.getPoolCode();
                // 需要占用的金额 = 占用总额 -已占额
                double needAmount = amount - useAmount;
                if (needAmount == 0) {
                    break;
                }
                // 当前预算池余额
                double poolAmount = poolService.getPoolBalanceByCode(poolCode);
                // 需要占用金额 >= 预算池余额
                if (needAmount >= poolAmount) {
                    // 占用全部预算池金额
                    record = new ExecutionRecord(poolCode, OperationType.USE, poolAmount, useBudget.getEventCode());

                    useAmount += poolAmount;
                } else {
                    // 占用部分预算池金额
                    record = new ExecutionRecord(poolCode, OperationType.USE, needAmount, useBudget.getEventCode());
                    useAmount += needAmount;
                }
                record.setSubjectId(poolLevel.getSubjectId());
                record.setAttributeCode(poolLevel.getAttributeCode());
                record.setBizId(useBudget.getBizId());
                record.setBizCode(useBudget.getBizCode());
                record.setBizRemark(useBudget.getBizRemark());

                // 占用记录
                poolService.recordLog(record);
                // 占用结果
                response.addUseResult(new BudgetUseResult(poolCode, record.getAmount()));
            }
            return ResultData.success(response);
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
                // 为保证占用幂等,避免重复释放,更新记录已释放标记
                executionRecordService.updateFreed(record.getId());

                newRecord = record.clone();
                newRecord.setOperation(OperationType.FREED);
                newRecord.setId(null);
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
        List<ExecutionRecord> records = executionRecordService.getUseRecords(eventCode, bizId);
        if (CollectionUtils.isNotEmpty(records)) {
            ExecutionRecord newRecord;
            // 剩余释放金额
            double balance = amount;
            for (ExecutionRecord record : records) {
                if (balance <= 0) {
                    continue;
                }
                // 为保证占用幂等,避免重复释放,更新记录已释放标记
                executionRecordService.updateFreed(record.getId());

                newRecord = record.clone();
                if (record.getAmount() > balance) {
                    // 释放当前记录部分金额
                    newRecord.setAmount(balance);
                    balance = 0;
                } else {
                    // 释放当前记录全部金额
                    balance = ArithUtils.sub(balance, record.getAmount());
                }
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
     * 获取最优匹配条件的预算池
     *
     * @param useBudget 预算占用数据
     * @return 返回最优预算池
     */
    private ResultData<Set<PoolLevel>> getOptimalBudgetPools(final BudgetUse useBudget) {
        /*
        1.按公司代码查询预算主体清单;
        2.维度匹配,匹配规则:
        a.获取所有维度及对应维度策略
        b.获取当前请求维度
        c.获取当前请求维度策略,若是一致性匹配的加入预算池查询条件;非一致性匹配的加入后续策略处理
        d.按预算主体,占用时间范围,一致性维度作为条件查询满足条件的预算池
        3.找出最优预算池
         */
        // 预算占用日期
        LocalDate useDate = LocalDate.parse(useBudget.getDate(), DateTimeFormatter.ISO_DATE);
        // 按占用数据获取维度
        Map<String, SearchFilter> otherDimensions = this.getOtherDimensionFilters(useBudget);
        // 组装所使用到的维度清单 -> 生成维度组合
        Set<String> codes = otherDimensions.keySet();
        codes.add(Constants.DIMENSION_CODE_ITEM);
        codes.add(Constants.DIMENSION_CODE_PERIOD);
        // 使用到的维度,按asci码排序,逗号(,)分隔
        StringJoiner joiner = new StringJoiner(",");
        codes.stream().sorted().forEach(joiner::add);
        final String attribute = joiner.toString();
        // 查询满足条件的预算池
        final Collection<SearchFilter> otherDimFilters = otherDimensions.values();
        // 按预算占用参数获取预算池大致范围
        final List<PoolAttributeView> poolAttributes = poolService.getBudgetPools(attribute, useDate, useBudget, otherDimFilters);
        if (CollectionUtils.isEmpty(poolAttributes)) {
            return ResultData.fail(ContextUtil.getMessage("pool_00009", JsonUtils.toJson(useBudget)));
        }
        // 预算科目代码
        String item = useBudget.getItem();
        // 通过预算主体清单和科目,确定预算执行策略.
        Set<SubjectStrategy> subjectStrategySet = new HashSet<>();
        // 通过预算池获取预算主体清单
        Set<String> subjectIds = poolAttributes.stream().map(PoolAttributeView::getSubjectId).collect(Collectors.toSet());
        List<Subject> subjects = subjectService.findByIds(subjectIds);
        for (Subject subject : subjects) {
            // 预算主体策略
            Strategy strategy = strategyService.findOne(subject.getStrategyId());
            // 预算主体科目
            SubjectItem subjectItem = subjectItemService.getSubjectItem(subject.getId(), item);
            if (Objects.nonNull(subjectItem)) {
                if (StringUtils.isNotBlank(subjectItem.getStrategyId())) {
                    // 预算主体科目策略
                    strategy = strategyService.findOne(subjectItem.getStrategyId());
                }
            } else {
                // 预算占用时,未找到预算主体[{0}]的预算科目[{1}]
                return ResultData.fail(ContextUtil.getMessage("pool_00010", subject.getCode(), item));
            }
            subjectStrategySet.add(new SubjectStrategy(subject.getId(), strategy));
        }

        Set<PoolLevel> poolLevelSet = null;
        // 按策略优先级执行
        subjectStrategySet.stream().sorted(Comparator.comparingInt(SubjectStrategy::getLevel).reversed());
        for (SubjectStrategy strategy : subjectStrategySet) {
            // 当存在同一个预算科目,多个预算主体下有不同的策略时,默认按强控>年度总额>弱控的顺序执行
            try {
                Class<?> clazz = Class.forName(strategy.getStrategyClass());
                if (BudgetExecutionStrategy.class.isAssignableFrom(clazz)) {
                    // 策略实例
                    BudgetExecutionStrategy executionStrategy = (BudgetExecutionStrategy) ContextUtil.getBean(clazz);
                    ResultData<Set<PoolLevel>> result = executionStrategy.execution(attribute, useBudget, poolAttributes, otherDimFilters);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("预算策略[{}]执行结果: {}", strategy.getStrategyName(), result);
                    }
                    if (result.successful()) {
                        poolLevelSet = result.getData();
                        if (CollectionUtils.isNotEmpty(poolLevelSet)) {
                            break;
                        }
                    } else {
                        return ResultData.fail("预算执行结果: " + result.getMessage());
                    }
                } else {
                    return ResultData.fail("预算执行策略[" + strategy.getStrategyName() + "]配置错误.");
                }
            } catch (ClassNotFoundException | BeansException e) {
                return ResultData.fail("预算执行策略执行异常: " + ExceptionUtils.getRootCauseMessage(e));
            }
        }

        if (CollectionUtils.isNotEmpty(poolLevelSet)) {
            return ResultData.success(poolLevelSet);
        } else {
            return ResultData.fail("预算占用时,未找到满足条件的预算池.");
        }
    }

    /**
     * 按占用参数获取其他维度条件
     * 期间和科目为预制默认维度匹配,不在本范围中
     */
    public Map<String, SearchFilter> getOtherDimensionFilters(BudgetUse use) {
        // 占用的维度代码
        Map<String, SearchFilter> dimFilterMap = new HashMap<>();
        // 组织机构
        String org = use.getOrg();
        if (Objects.nonNull(org)) {
            org = org.trim();
            if (StringUtils.isNotBlank(org) && !StringUtils.equalsIgnoreCase(Constants.NONE, org)) {
                dimFilterMap.put(DimensionAttribute.FIELD_ORG, this.doDimensionStrategy(use, DimensionAttribute.FIELD_ORG, org));
            }
        }
        // 预算项目
        String project = use.getProject();
        if (Objects.nonNull(project)) {
            project = project.trim();
            if (StringUtils.isNotBlank(project) && !StringUtils.equalsIgnoreCase(Constants.NONE, project)) {
                dimFilterMap.put(DimensionAttribute.FIELD_PROJECT, this.doDimensionStrategy(use, DimensionAttribute.FIELD_PROJECT, project));
            }
        }
        // 自定义1
        String udf1 = use.getUdf1();
        if (Objects.nonNull(udf1)) {
            udf1 = udf1.trim();
            if (StringUtils.isNotBlank(udf1) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf1)) {
                dimFilterMap.put(DimensionAttribute.FIELD_UDF1, this.doDimensionStrategy(use, DimensionAttribute.FIELD_UDF1, udf1));
            }
        }
        // 自定义2
        String udf2 = use.getUdf2();
        if (Objects.nonNull(udf2)) {
            udf2 = udf2.trim();
            if (StringUtils.isNotBlank(udf2) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf2)) {
                dimFilterMap.put(DimensionAttribute.FIELD_UDF2, this.doDimensionStrategy(use, DimensionAttribute.FIELD_UDF2, udf2));
            }
        }
        // 自定义3
        String udf3 = use.getUdf3();
        if (Objects.nonNull(udf3)) {
            udf3 = udf3.trim();
            if (StringUtils.isNotBlank(udf3) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf3)) {
                dimFilterMap.put(DimensionAttribute.FIELD_UDF3, this.doDimensionStrategy(use, DimensionAttribute.FIELD_UDF3, udf3));
            }
        }
        // 自定义4
        String udf4 = use.getUdf4();
        if (Objects.nonNull(udf4)) {
            udf4 = udf4.trim();
            if (StringUtils.isNotBlank(udf4) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf4)) {
                dimFilterMap.put(DimensionAttribute.FIELD_UDF4, this.doDimensionStrategy(use, DimensionAttribute.FIELD_UDF4, udf4));
            }
        }
        // 自定义5
        String udf5 = use.getUdf5();
        if (Objects.nonNull(udf5)) {
            udf5 = udf5.trim();
            if (StringUtils.isNotBlank(udf5) && !StringUtils.equalsIgnoreCase(Constants.NONE, udf5)) {
                dimFilterMap.put(DimensionAttribute.FIELD_UDF5, this.doDimensionStrategy(use, DimensionAttribute.FIELD_UDF5, udf5));
            }
        }
        return dimFilterMap;
    }

    /**
     * 按预算维度策略获取过滤条件
     *
     * @param dimCode  维度代码
     * @param dimValue 维度值
     * @return 返回维度策略获取过滤条件
     * @throws ServiceException 异常
     */
    private SearchFilter doDimensionStrategy(BudgetUse budgetUse, String dimCode, String dimValue) {
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

        ServiceException exception;
        String className = strategy.getClassPath();
        try {
            Class<?> clazz = Class.forName(className);
            if (DimensionMatchStrategy.class.isAssignableFrom(clazz)) {
                // 策略实例
                DimensionMatchStrategy matchStrategy = (DimensionMatchStrategy) ContextUtil.getBean(clazz);
                // 策略结果
                Object obj = matchStrategy.getMatchValue(budgetUse, dimension, dimValue);
                if (Objects.nonNull(obj)) {
                    SearchFilter filter;
                    if (obj instanceof Collection) {
                        filter = new SearchFilter(dimCode, dimValue, SearchFilter.Operator.IN);
                    } else if (obj.getClass().isArray()) {
                        filter = new SearchFilter(dimCode, dimValue, SearchFilter.Operator.IN);
                    } else {
                        filter = new SearchFilter(dimCode, dimValue);
                    }
                    return filter;
                } else {
                    exception = new ServiceException("预算维度[" + dimension.getName() + "]策略条件不能返回为Null.");
                }
            } else {
                exception = new ServiceException("预算维度[" + dimension.getName() + "]策略配置错误.");
            }
        } catch (ClassNotFoundException | BeansException e) {
            exception = new ServiceException("按预算维度策略获取过滤条件异常", e);
        }
        throw exception;
    }
}
