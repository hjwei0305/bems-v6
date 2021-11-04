package com.changhong.bems.websocket;

import com.changhong.bems.commons.Constants;
import com.changhong.bems.dto.OrderStatistics;
import com.changhong.bems.service.OrderService;
import com.changhong.bems.websocket.config.MyEndpointConfigure;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.util.JsonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-06 17:18
 */
@ServerEndpoint(value = "/websocket/order/{orderId}", configurator = MyEndpointConfigure.class)
public class WebsocketServer {
    private final static Logger LOG = LoggerFactory.getLogger(WebsocketServer.class);

    /**
     * 连接集合
     */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private OrderService orderService;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("orderId") String orderId) {
        LOG.debug("预算申请单id[{}]", orderId);

        //添加到集合中
        SESSION_MAP.put(session.getId(), session);

        try {
            TimeUnit.SECONDS.sleep(1);

            BoundValueOperations<String, Object> operations = redisTemplate.boundValueOps(Constants.HANDLE_CACHE_KEY_PREFIX.concat(orderId));
            OrderStatistics statistics = (OrderStatistics) operations.get();
            while (Objects.nonNull(statistics)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("预算申请单id[{}]当前处理状态: {}", orderId, statistics);
                }
                if (statistics.getFinish()) {
                    break;
                }
                // 输出最新日志
                send(session, ResultData.success(statistics));
                TimeUnit.SECONDS.sleep(3);
                statistics = (OrderStatistics) operations.get();
            }
            // 更新订单是否正在异步处理行项数据.如果是,在编辑时进入socket状态显示页面
            orderService.setProcessStatus(orderId, Boolean.FALSE);

            statistics = new OrderStatistics();
            send(session, ResultData.success(statistics));
        } catch (Exception e) {
            LOG.error("websocket获取预算申请单处理日志异常:" + ExceptionUtils.getRootCauseMessage(e), e);
            // 输出最新日志
            send(session, ResultData.fail("websocket获取预算申请单处理日志异常:" + ExceptionUtils.getRootCauseMessage(e)));
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        //从集合中删除
        SESSION_MAP.remove(session.getId());
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("Websocket发生异常: ", error);
    }

    /**
     * 服务器接收到客户端消息时调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {

    }

    /**
     * 封装一个send方法，发送消息到前端
     */
    private void send(Session session, ResultData<OrderStatistics> resultData) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("发送消息: {}", resultData);
        }
        synchronized (session) {
            try {
                session.getBasicRemote().sendText(JsonUtils.toJson(resultData));
            } catch (Exception e) {
                LOG.error("Websocket发送异常", e);
            }
        }
    }
}
