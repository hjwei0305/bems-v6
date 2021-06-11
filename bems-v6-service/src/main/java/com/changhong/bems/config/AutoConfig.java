package com.changhong.bems.config;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.service.PoolService;
import com.changhong.bems.service.mq.OrderStateSubscribeListener;
import com.changhong.bems.service.strategy.*;
import com.changhong.bems.service.strategy.impl.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.TimeUnit;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-05 22:50
 */
@EnableAsync
@Configuration
public class AutoConfig {
    private final RedisConnectionFactory redisConnectionFactory;

    //////////////////redis mq config start/////////////////////

    @Autowired
    public AutoConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * 配置消息监听器
     */
    @Bean
    public OrderStateSubscribeListener orderStateListener(Cache<String, String> memoryCache) {
        return new OrderStateSubscribeListener(memoryCache);
    }

    /**
     * 将消息监听器绑定到消息容器
     */
    @Bean
    public RedisMessageListenerContainer messageListenerContainer(Cache<String, String> memoryCache) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        //  MessageListener 监听数据
        container.addMessageListener(orderStateListener(memoryCache), ChannelTopic.of(Constants.TOPIC));
        return container;
    }

    //////////////////redis mq config end/////////////////////

    @Bean
    public Cache<String, String> memoryCache() {
        return CacheBuilder.newBuilder()
                // 设置缓存最大容量为100，超过100之后就会按照LRU最近最少使用算法来移除缓存项
                .maximumSize(512)
                // 设置写缓存后8秒钟过期  最后一次写入后的一段时间移出
                .expireAfterWrite(60, TimeUnit.SECONDS)

                // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
                .concurrencyLevel(16)
                // 设置缓存容器的初始容量为10
                .initialCapacity(10)
                .build();
    }

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
