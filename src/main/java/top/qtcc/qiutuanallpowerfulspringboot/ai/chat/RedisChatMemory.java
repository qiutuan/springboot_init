package top.qtcc.qiutuanallpowerfulspringboot.ai.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Redis 的 ChatMemory 实现：多轮对话上下文持久化到 Redis（JSON 数组），
 * 默认 2 小时过期、最多保留 30 条消息。
 *
 * @author qiutuan
 */
@Slf4j
@Component
public class RedisChatMemory implements ChatMemory {

    private static final String KEY_PREFIX = "ai:chat:memory:";
    private static final int MAX_MESSAGES = 30;
    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisChatMemory(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Message> get(String conversationId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + conversationId);
        if (json == null) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            List<Message> messages = new ArrayList<>(list.size());
            for (Map<String, Object> m : list) {
                messages.add(toMessage(m));
            }
            return messages;
        } catch (Exception e) {
            log.warn("读取聊天上下文失败, conversationId={}", conversationId, e);
            return List.of();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = KEY_PREFIX + conversationId;
        List<Map<String, Object>> existing = new ArrayList<>();
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            try {
                existing = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
                });
            } catch (Exception e) {
                log.warn("解析聊天上下文失败，重置会话, conversationId={}", conversationId, e);
                existing = new ArrayList<>();
            }
        }
        for (Message message : messages) {
            existing.add(toMap(message));
        }
        if (existing.size() > MAX_MESSAGES) {
            existing = new ArrayList<>(existing.subList(existing.size() - MAX_MESSAGES, existing.size()));
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(existing), TTL);
        } catch (Exception e) {
            log.error("保存聊天上下文失败, conversationId={}", conversationId, e);
        }
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(KEY_PREFIX + conversationId);
    }

    private Map<String, Object> toMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("role", message.getMessageType().getValue());
        map.put("content", message.getText());
        return map;
    }

    private Message toMessage(Map<String, Object> map) {
        String role = String.valueOf(map.get("role"));
        String content = String.valueOf(map.get("content"));
        return switch (role) {
            case "user" -> new UserMessage(content);
            case "assistant" -> new AssistantMessage(content);
            case "system" -> new SystemMessage(content);
            default -> throw new IllegalArgumentException("unknown message role: " + role);
        };
    }
}
