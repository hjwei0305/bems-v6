package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.StrategyCategory;
import com.changhong.bems.dto.StrategyDto;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.entity.SubjectItem;
import com.changhong.bems.service.strategy.BaseStrategy;
import com.changhong.bems.service.strategy.BudgetExecutionStrategy;
import com.changhong.bems.service.strategy.DimensionMatchStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.bind.TypeConstraintException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 预算策略(Strategy)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 11:12:04
 */
@Service
public class StrategyService {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectItemService subjectItemService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, BaseStrategy> strategyMap;

    public StrategyService(Map<String, BaseStrategy> map) {
        this.strategyMap = map;
    }

    /**
     * 获取预算维度匹配策略
     */
    public DimensionMatchStrategy getMatchStrategy(String code) {
        BaseStrategy strategy = strategyMap.get(code);
        if (strategy instanceof DimensionMatchStrategy) {
            return (DimensionMatchStrategy) strategy;
        } else {
            throw new TypeConstraintException("[" + code + "]不是预算维度匹配策略");
        }
    }

    /**
     * 获取预算执行策略
     */
    public BudgetExecutionStrategy getExecutionStrategy(String code) {
        BaseStrategy strategy = strategyMap.get(code);
        if (strategy instanceof BudgetExecutionStrategy) {
            return (BudgetExecutionStrategy) strategy;
        } else {
            throw new TypeConstraintException("[" + code + "]不是预算执行策略");
        }
    }

    /**
     * 按策略代码获取预算策略
     */
    public StrategyDto getByCode(String code) {
        StrategyDto dto = null;
        BaseStrategy strategy = strategyMap.get(code);
        if (Objects.nonNull(strategy)) {
            dto = new StrategyDto();
            dto.setCategory(strategy.category());
            dto.setId(code);
            dto.setCode(code);
            dto.setName(strategy.name());
            dto.setRemark(strategy.remark());
            dto.setClassPath(strategy.getClass().getName().split("[$]")[0]);
        }
        return dto;
    }

    public String getNameByCode(String code) {
        BaseStrategy strategy = strategyMap.get(code);
        if (Objects.nonNull(strategy)) {
            return strategy.name();
        }
        return code;
    }

    /**
     * 基于主键集合查询集合数据对象
     */
    public List<StrategyDto> findAll() {
        List<StrategyDto> strategyList = new ArrayList<>();
        BaseStrategy strategy;
        StrategyDto dto;
        for (Map.Entry<String, BaseStrategy> entry : strategyMap.entrySet()) {
            strategy = entry.getValue();
            dto = new StrategyDto();
            dto.setCategory(strategy.category());
            dto.setId(entry.getKey());
            dto.setCode(entry.getKey());
            dto.setName(strategy.name());
            dto.setRemark(strategy.remark());
            dto.setClassPath(strategy.getClass().getName().split("[$]")[0]);
            strategyList.add(dto);
        }
        return strategyList;
    }

    /**
     * 按分类查询策略
     *
     * @param category 分类
     * @return 策略清单
     */
    public List<StrategyDto> findByCategory(StrategyCategory category) {
        List<StrategyDto> strategyList = new ArrayList<>();
        BaseStrategy strategy;
        StrategyDto dto;
        for (Map.Entry<String, BaseStrategy> entry : strategyMap.entrySet()) {
            strategy = entry.getValue();
            if (category == strategy.category()) {
                dto = new StrategyDto();
                dto.setCategory(strategy.category());
                dto.setId(entry.getKey());
                dto.setCode(entry.getKey());
                dto.setName(strategy.name());
                dto.setRemark(strategy.remark());
                dto.setClassPath(strategy.getClass().getName().split("[$]")[0]);
                strategyList.add(dto);
            }
        }
        return strategyList;
    }

    /**
     * 清除策略缓存
     *
     * @param subjectId 预算主体id
     * @param itemCode  预算科目代码
     */
    public void cleanStrategyCache(String subjectId, String itemCode) {
        String pattern = Constants.STRATEGY_CACHE_KEY_PREFIX + subjectId;
        if (StringUtils.isNotBlank(itemCode)) {
            pattern = pattern + ":" + itemCode;
        } else {
            pattern = pattern + ":*";
        }
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 获取预算执行控制策略
     *
     * @param subjectId 预算主体id
     * @param itemCode  预算科目代码
     * @return 预算执行控制策略
     */
    public ResultData<StrategyDto> getStrategy(String subjectId, String itemCode) {
        BoundValueOperations<String, Object> operations =
                redisTemplate.boundValueOps(Constants.STRATEGY_CACHE_KEY_PREFIX + subjectId + ":" + itemCode);
        // 预算主体策略
        StrategyDto strategy = (StrategyDto) operations.get();
        if (Objects.isNull(strategy)) {
            // 预算主体科目
            SubjectItem subjectItem = subjectItemService.getSubjectItem(subjectId, itemCode);
            if (Objects.nonNull(subjectItem)) {
                if (StringUtils.isNotBlank(subjectItem.getStrategyId())) {
                    // 预算主体科目策略
                    strategy = this.getByCode(subjectItem.getStrategyId());
                }
            }
            if (Objects.isNull(strategy)) {
                Subject subject = subjectService.getSubject(subjectId);
                if (Objects.nonNull(subject)) {
                    strategy = this.getByCode(subject.getStrategyId());
                    if (Objects.isNull(strategy)) {
                        // 预算占用时,未找到预算主体[{0}]的预算科目[{1}]
                        return ResultData.fail(ContextUtil.getMessage("pool_00010", subjectId, itemCode));
                    }
                } else {
                    // 预算主体[{0}]不存在!
                    return ResultData.fail(ContextUtil.getMessage("subject_00003", subjectId));
                }
            }
            // 写入缓存
            operations.set(strategy, 3, TimeUnit.DAYS);
        }
        return ResultData.success(strategy);
    }
}