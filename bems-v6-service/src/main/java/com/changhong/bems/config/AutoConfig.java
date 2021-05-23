package com.changhong.bems.config;

import com.changhong.bems.service.strategy.*;
import com.changhong.bems.service.strategy.impl.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-05 22:50
 */
@EnableAsync
@Configuration
public class AutoConfig {

    /**
     * 一致性维度匹配策略
     */
    @Bean
    @ConditionalOnMissingBean
    public EqualMatchStrategy equalStrategy() {
        return new DefaultEqualMatchStrategy();
    }

    /**
     * 组织树路径维度匹配策略
     */
    @Bean
    @ConditionalOnMissingBean
    public OrgTreeMatchStrategy treeMatchStrategy() {
        return new DefaultOrgTreeMatchStrategy();
    }

    /**
     * 期间关系维度匹配策略
     */
    @Bean
    @ConditionalOnMissingBean
    public PeriodMatchStrategy periodMatchStrategy() {
        return new DefaultPeriodMatchStrategy();
    }

    /**
     * 余额范围内强制控制策略
     */
    @Bean
    @ConditionalOnMissingBean
    public LimitExecutionStrategy limitExecutionStrategy() {
        return new DefaultLimitExecutionStrategy();
    }

    /**
     * 允许超额使用控制策略
     */
    @Bean
    @ConditionalOnMissingBean
    public ExcessExecutionStrategy excessExecutionStrategy() {
        return new DefaultExcessExecutionStrategy();
    }

    /**
     * 年度总额范围内控制策略
     */
    @Bean
    @ConditionalOnMissingBean
    public AnnualTotalExecutionStrategy annualTotalExecutionStrategy() {
        return new DefaultAnnualTotalExecutionStrategy();
    }

}
