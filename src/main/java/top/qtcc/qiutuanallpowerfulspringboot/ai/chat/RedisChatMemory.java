package top.qtcc.qiutuanallpowerfulspringboot.ai.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
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
        String key = KEY_PREFIX + conversationId;
        try {
            List<String> entries = redisTemplate.opsForList().range(key, 0, -1);
            if (entries != null && !entries.isEmpty()) {
                List<Message> messages = new ArrayList<>(entries.size());
                for (String entry : entries) {
                    messages.add(toMessage(objectMapper.readValue(entry, new TypeReference<Map<String, Object>>() {
                    })));
                }
                return messages;
            }
            // 兼容旧版本 String 值（JSON 数组）存储
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return List.of();
            }
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            List<Message> messages = new ArrayList<>(list.size());
            for (Map<String, Object> map : list) {
                messages.add(toMessage(map));
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
        if (messages == null || messages.isEmpty()) {
            return;
        }
        try {
            DataType dataType = redisTemplate.type(key);
            if (dataType != null && dataType != DataType.NONE && dataType != DataType.LIST) {
                redisTemplate.delete(key);
            }
            List<String> payloads = new ArrayList<>(messages.size());
            for (Message message : messages) {
                payloads.add(objectMapper.writeValueAsString(toMap(message)));
            }
            redisTemplate.opsForList().rightPushAll(key, payloads);
            redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
            redisTemplate.expire(key, TTL);
        } catch (Exception e) {
            log.error("保存聊天上下文失败, conversationId={}", conversationId, e);
            throw new IllegalStateException("保存聊天上下文失败", e);
        }
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(KEY_PREFIX + conversationId);
    }

    private Map<String, Object> toMap(Message message) {
        return Map.of(
                "role", message.getMessageType().getValue(),
                "content", message.getText()
        );
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
