package com.changhong.bems.service.mq;

import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.log.LogUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-15 17:34
 */
@Component
public class EffectiveOrderConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(EffectiveOrderConsumer.class);

    private final OrderService orderService;

    public EffectiveOrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 处理收到的监听消息
     *
     * @param record 消息纪录
     */
    @KafkaListener(topics = "${sei.mq.topic}")
    public void processMessage(ConsumerRecord<String, String> record) {
//        if (LOG.isInfoEnabled()) {
        LogUtil.bizLog("接收TOPIC[{}]的消息", ContextUtil.getProperty("sei.mq.topic"));
//        }
        if (Objects.isNull(record)) {
            return;
        }
//        if (LOG.isInfoEnabled()) {
        LogUtil.bizLog("received key='{}' message = '{}'", record.key(), record.value());
//        }
        // 执行业务处理逻辑
        try {
            String orderId = record.value();
            ResultData<Void> resultData = orderService.effective(orderId);
//            if (LOG.isInfoEnabled()) {
            LogUtil.bizLog("预算申请单生效处理结果: {}", resultData);
//            }
        } catch (Exception e) {
            LOG.error("MqConsumer process message error!", e);
        }
    }
}
