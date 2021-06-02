package com.changhong.bems.config;

import com.changhong.bems.service.PoolService;
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
//    private final RedisConnectionFactory redisConnectionFactory;
//
//    //////////////////redis mq config start/////////////////////
//
//    @Autowired
//    public AutoConfig(RedisConnectionFactory redisConnectionFactory) {
//        this.redisConnectionFactory = redisConnectionFactory;
//    }
//
//    /**
//     * 配置消息监听器
//     */
//    @Bean
//    public OrderStateSubscribeListener orderStateListener(StringRedisTemplate stringRedisTemplate) {
//        return new OrderStateSubscribeListener(stringRedisTemplate);
//    }
//
//    /**
//     * 将消息监听器绑定到消息容器
//     */
//    @Bean
//    public RedisMessageListenerContainer messageListenerContainer(StringRedisTemplate stringRedisTemplate) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(redisConnectionFactory);
//
//        //  MessageListener 监听数据
//        container.addMessageListener(orderStateListener(stringRedisTemplate), ChannelTopic.of(OrderStateSubscribeListener.TOPIC));
//        return container;
//    }
//
//    //////////////////redis mq config end/////////////////////

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
    public LimitExecutionStrategy limitExecutionStrategy(PoolService poolService) {
        return new DefaultLimitExecutionStrategy(poolService);
    }

    /**
     * 允许超额使用控制策略
     */
    @Bean
    @ConditionalOnMissingBean
    public ExcessExecutionStrategy excessExecutionStrategy(PoolService poolService) {
        return new DefaultExcessExecutionStrategy(poolService);
    }

    /**
     * 年度总额范围内控制策略
     */
    @Bean
    @ConditionalOnMissingBean
    public AnnualTotalExecutionStrategy annualTotalExecutionStrategy(PoolService poolService) {
        return new DefaultAnnualTotalExecutionStrategy(poolService);
    }

}
