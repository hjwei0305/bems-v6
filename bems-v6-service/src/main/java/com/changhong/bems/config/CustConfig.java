package com.changhong.bems.config;

import com.changhong.bems.service.cust.BudgetDimensionCustManager;
import com.changhong.bems.service.cust.DefaultBudgetDimensionCustManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-12-20 12:25
 */
@Configuration
public class CustConfig {

    @Bean
    @ConditionalOnMissingBean(BudgetDimensionCustManager.class)
    public BudgetDimensionCustManager budgetDimensionCustManager(RedisTemplate<String, Object> redisTemplate) {
        return new DefaultBudgetDimensionCustManager(redisTemplate);
    }
}
