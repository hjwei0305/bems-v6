package com.changhong.bems.service;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.StrategyCategory;
import com.changhong.bems.dto.StrategyDto;
import com.changhong.bems.entity.StrategyItem;
import com.changhong.bems.entity.Subject;
import com.changhong.bems.service.strategy.AbstractStrategy;
import com.changhong.bems.service.strategy.BaseBudgetExecutionStrategy;
import com.changhong.bems.service.strategy.BaseDimensionMatchStrategy;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.bind.TypeConstraintException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 预算策略(Strategy)业务逻辑实现类
 *
 * @author sei
 * @since 2021-04-22 11:12:04
 */
@Service
public class StrategyService {

    private final Map<String, AbstractStrategy> strategyMap;

    public StrategyService(Map<String, AbstractStrategy> map) {
        this.strategyMap = map;
    }

    /**
     * 获取预算维度匹配策略
     */
    public BaseDimensionMatchStrategy getMatchStrategy(String code) {
        AbstractStrategy strategy = strategyMap.get(code);
        if (strategy instanceof BaseDimensionMatchStrategy) {
            return (BaseDimensionMatchStrategy) strategy;
        } else {
            throw new TypeConstraintException("[" + code + "]不是预算维度匹配策略");
        }
    }

    /**
     * 获取预算执行策略
     */
    public BaseBudgetExecutionStrategy getExecutionStrategy(String code) {
        AbstractStrategy strategy = strategyMap.get(code);
        if (strategy instanceof BaseBudgetExecutionStrategy) {
            return (BaseBudgetExecutionStrategy) strategy;
        } else {
            throw new TypeConstraintException("[" + code + "]不是预算执行策略");
        }
    }

    /**
     * 按策略代码获取预算策略
     */
    public StrategyDto getByCode(String code) {
        StrategyDto dto = null;
        AbstractStrategy strategy = strategyMap.get(code);
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

    /**
     * 通过策略id获取策略名称
     *
     * @param code 策略id
     * @return 策略名称
     */
    public String getNameByCode(String code) {
        AbstractStrategy strategy = strategyMap.get(code);
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
        AbstractStrategy strategy;
        StrategyDto dto;
        for (Map.Entry<String, AbstractStrategy> entry : strategyMap.entrySet()) {
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
        AbstractStrategy strategy;
        StrategyDto dto;
        for (Map.Entry<String, AbstractStrategy> entry : strategyMap.entrySet()) {
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
     * 按预算维度查询维度策略
     *
     * @param dimensionCode 预算维度代码
     * @return 策略清单
     */
    public List<StrategyDto> findByDimensionCode(String dimensionCode) {
        List<StrategyDto> strategyList = new ArrayList<>();
        AbstractStrategy strategy;
        StrategyDto dto;
        for (Map.Entry<String, AbstractStrategy> entry : strategyMap.entrySet()) {
            strategy = entry.getValue();
            if (StrategyCategory.DIMENSION == strategy.category()) {
                if (strategy instanceof BaseDimensionMatchStrategy) {
                    BaseDimensionMatchStrategy matchStrategy = (BaseDimensionMatchStrategy) strategy;
                    if (matchStrategy.checkScope(dimensionCode)) {
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
            }
        }
        return strategyList;
    }
}