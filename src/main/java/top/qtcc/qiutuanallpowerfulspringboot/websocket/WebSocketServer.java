package top.qtcc.qiutuanallpowerfulspringboot.websocket;

import cn.hutool.json.JSONUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务（单点/广播推送 + 心跳）
 *
 * @author qiutuan
 * @date 2024/11/22
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/{key}")
public class WebSocketServer {

    /**
     * 在线连接：key -> Session
     */
    private static final ConcurrentHashMap<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 连接标识
     */
    private String key;

    /**
     * 链接成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("key") String key) {
        this.key = key;
        SESSION_POOL.put(key, session);
        log.info("【websocket】新连接 key={}, 当前在线: {}", key, SESSION_POOL.size());
    }

    /**
     * 链接关闭
     */
    @OnClose
    public void onClose() {
        SESSION_POOL.remove(this.key, SESSION_POOL.get(this.key));
        log.info("【websocket】连接断开 key={}, 当前在线: {}", this.key, SESSION_POOL.size());
    }

    /**
     * 收到客户端消息
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("【websocket】收到消息 key={}, message={}", this.key, message);
    }

    /**
     * 连接错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【websocket】连接错误 key={}", this.key, error);
    }

    /**
     * 广播消息
     */
    public void sendAllMessage(String message) {
        SESSION_POOL.forEach((k, session) -> {
            try {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                log.error("【websocket】广播消息失败 key={}", k, e);
            }
        });
    }

    /**
     * 单点消息
     */
    public void sendOneMessage(String key, String message) {
        Session session = SESSION_POOL.get(key);
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("【websocket】单点消息失败 key={}", key, e);
            }
        }
    }

    /**
     * 心跳检测（JSON 消息，客户端可据此区分业务消息）
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        sendAllMessage(JSONUtil.toJsonStr(Map.of("type", "heartbeat", "timestamp", System.currentTimeMillis())));
    }
}
