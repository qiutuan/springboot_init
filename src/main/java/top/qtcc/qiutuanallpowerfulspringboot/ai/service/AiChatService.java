package top.qtcc.qiutuanallpowerfulspringboot.ai.service;

import reactor.core.publisher.Flux;

/**
 * AI 对话服务
 *
 * @author qiutuan
 */
public interface AiChatService {

    /**
     * 非流式对话（带多轮上下文）
     *
     * @param conversationId 会话 ID
     * @param message        用户消息
     * @return 模型回复
     */
    String chat(String conversationId, String message);

    /**
     * 流式对话（SSE）
     *
     * @param conversationId 会话 ID
     * @param message        用户消息
     * @return 逐 token 回复流
     */
    Flux<String> chatStream(String conversationId, String message);
}
