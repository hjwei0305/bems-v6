package com.changhong.bems.service.mq;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.OrderStatistics;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.context.SessionUser;
import com.changhong.sei.core.context.mock.LocalMockUser;
import com.changhong.sei.core.context.mock.MockUser;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.util.thread.ThreadLocalHolder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
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
public class BudgetOrderConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(BudgetOrderConsumer.class);

    private final OrderService orderService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public BudgetOrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 处理收到的监听消息
     *
     * @param record 消息纪录
     */
    @KafkaListener(topics = "${sei.mq.bemsv6.topic}")
    public void processMessage(ConsumerRecord<String, String> record) {
        if (Objects.isNull(record)) {
            return;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("received key='{}' message = '{}'", record.key(), record.value());
        }

        String message = record.value();
        EffectiveOrderMessage orderMessage = JsonUtils.fromJson(message, EffectiveOrderMessage.class);

        // 执行业务处理逻辑
        String orderId = orderMessage.getOrderId();
        String orderDetailId = orderMessage.getOrderDetailId();

        // 操作类型
        String operation = orderMessage.getOperation();
        ResultData<Void> resultData;
        try {
            ThreadLocalHolder.begin();
            // 模拟用户
            MockUser mockUser = new LocalMockUser();
            SessionUser sessionUser = new SessionUser();
            sessionUser.setTenantCode(orderMessage.getTenantCode());
            sessionUser.setUserId(orderMessage.getUserId());
            sessionUser.setAccount(orderMessage.getAccount());
            sessionUser.setUserName(orderMessage.getUserName());
            mockUser.mock(sessionUser);

            if (Constants.ORDER_OPERATION_CONFIRM.equals(operation)) {
                // 订单确认
                resultData = orderService.confirmUseBudget(orderDetailId);
                if (LOG.isInfoEnabled()) {
                    LOG.info("预算申请单生效结果: {}", resultData);
                }
            } else if (Constants.ORDER_OPERATION_CANCEL.equals(operation)) {
                // 订单取消确认
                resultData = orderService.cancelConfirmUseBudget(orderDetailId);
                if (LOG.isInfoEnabled()) {
                    LOG.info("预算申请单流程完成处理结果: {}", resultData);
                }
            } else if (Constants.ORDER_OPERATION_EFFECTIVE.equals(operation)) {
                // 订单生效
                resultData = orderService.effectiveUseBudget(orderDetailId);
                if (LOG.isInfoEnabled()) {
                    LOG.info("预算申请单生效结果: {}", resultData);
                }
            } else {
                resultData = ResultData.fail("不支持的消息处理类型");
            }
            OrderStatistics statistics;
            BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
            statistics = (OrderStatistics) operations.get();
            if (Objects.nonNull(statistics)) {
                if (resultData.successful()) {
                    statistics.addSuccesses();
                } else {
                    statistics.addFailures();
                }
                // 设置默认过期时间:1天
                operations.set(statistics);
            }
        } catch (Exception e) {
            LOG.error("预算申请单生效处理异常.", e);
        } finally {
            // 释放资源
            ThreadLocalHolder.end();
        }
    }
}
