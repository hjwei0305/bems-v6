package com.changhong.bems.service.mq;

import com.changhong.sei.core.cache.impl.LocalCacheProviderImpl;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 实现功能：
 * stringRedisTemplate.convertAndSend(getTopic(), "messages");
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-01 11:36
 */
public class OrderStateSubscribeListener implements MessageListener {
    // 发布/订阅 的 Topic
    public static final String TOPIC = "bems-v6:order:state";

    private final StringRedisTemplate stringRedisTemplate;

    public OrderStateSubscribeListener(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void send(String message) {
        stringRedisTemplate.convertAndSend(TOPIC, message);
    }

    /**
     * Callback for processing received objects through Redis.
     *
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        String channel = new String(message.getChannel());
        String pattern_ = new String(pattern);

        System.out.println(body);
        System.out.println(channel);
        // 如果是 ChannelTopic, 则 channel 字段与 pattern 字段值相同
        System.out.println(pattern_);
    }
}
