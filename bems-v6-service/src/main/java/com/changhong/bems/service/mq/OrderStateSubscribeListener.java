package com.changhong.bems.service.mq;

import com.changhong.bems.dto.OrderMessage;
import com.changhong.sei.core.util.JsonUtils;
import com.google.common.cache.Cache;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * 实现功能：
 * stringRedisTemplate.convertAndSend(getTopic(), "messages");
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-01 11:36
 */
public class OrderStateSubscribeListener implements MessageListener {

    private final Cache<String, String> memoryCache;

    public OrderStateSubscribeListener(Cache<String, String> memoryCache) {
        this.memoryCache = memoryCache;
    }

    /**
     * Callback for processing received objects through Redis.
     *
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String messageJson = new String(message.getBody());
        OrderMessage orderMessage = JsonUtils.fromJson(messageJson, OrderMessage.class);
        memoryCache.put(orderMessage.getOrderId(), messageJson);
    }
}
