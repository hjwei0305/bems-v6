package com.changhong.bems.service.mq;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.exception.ServiceException;
import com.changhong.sei.util.IdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-10 15:10
 */
@Component
public class BudgetOrderProducer {
    private static final Logger LOG = LoggerFactory.getLogger(BudgetOrderProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${sei.mq.bemsv6.topic}")
    private String topic;

    /**
     * 发送消息
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void sendConfirmMessage(String orderId, List<OrderDetail> details, SessionUser sessionUser) {
        this.sendMessage(orderId, details, sessionUser, Constants.EVENT_BUDGET_CONFIRM);
    }

    /**
     * 发送消息
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void sendCancelMessage(String orderId, List<OrderDetail> details, SessionUser sessionUser) {
        this.sendMessage(orderId, details, sessionUser, Constants.EVENT_BUDGET_CANCEL);
    }

    /**
     * 发送消息
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void sendEffectiveMessage(String orderId, List<OrderDetail> details, SessionUser sessionUser) {
        this.sendMessage(orderId, details, sessionUser, Constants.EVENT_BUDGET_EFFECTIVE);
    }

    private void sendMessage(String orderId, List<OrderDetail> details, SessionUser sessionUser, String operation) {
        try {
            // 为避免事务冲突,延时发送消息
            TimeUnit.SECONDS.sleep(1);

            EffectiveOrderMessage message;
            for (OrderDetail detail : details) {
                // 发送队列消息
                message = new EffectiveOrderMessage();
                message.setOrderId(orderId);
                message.setOrderDetailId(detail.getId());
                message.setOperation(operation);
                message.setUserId(sessionUser.getUserId());
                message.setAccount(sessionUser.getAccount());
                message.setUserName(sessionUser.getUserName());
                message.setTenantCode(sessionUser.getTenantCode());

                if (StringUtils.isBlank(topic)) {
                    throw new ServiceException("应用配置中没有消息队列的主题【sei.mq.topic】！");
                }
                kafkaTemplate.send(topic, IdGenerator.uuid(), JsonUtils.toJson(message));
                if (LOG.isInfoEnabled()) {
                    LOG.info("预算申请单[{}]-直接生效消息发送队列成功.", message);
                }
            }
        } catch (Exception e) {
            LOG.error("MQ队列消息发送异常.", e);
        }
    }
}
