package top.qtcc.qiutuanallpowerfulspringboot.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.qtcc.qiutuanallpowerfulspringboot.ai.advisor.LoggingAdvisor;
import top.qtcc.qiutuanallpowerfulspringboot.ai.advisor.PromptInjectionProtectionAdvisor;
import top.qtcc.qiutuanallpowerfulspringboot.ai.service.AiChatService;

/**
 * AI 对话服务实现：
 * - MessageChatMemoryAdvisor：多轮上下文（Redis 持久化）
 * - PromptInjectionProtectionAdvisor：提示注入防护
 * - LoggingAdvisor：调用日志
 *
 * @author qiutuan
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;

    public AiChatServiceImpl(ChatModel chatModel, ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        new PromptInjectionProtectionAdvisor(),
                        new LoggingAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Override
    public String chat(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @Override
    public Flux<String> chatStream(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
