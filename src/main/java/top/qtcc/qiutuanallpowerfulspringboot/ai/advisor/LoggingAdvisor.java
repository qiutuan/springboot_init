package top.qtcc.qiutuanallpowerfulspringboot.ai.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;

/**
 * AI 调用日志 Advisor：记录请求（会话+用户消息）与响应（截断），
 * 便于审计与成本追踪；日志摘要不落全量内容。
 *
 * @author qiutuan
 */
public class LoggingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String conversationId = String.valueOf(request.context().getOrDefault("conversationId", ""));
        log.info("[AI] request | conversationId={} | userText={}", conversationId, truncate(request.prompt().getContents(), 200));
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        try {
            String content = response.chatResponse().getResult().getOutput().getText();
            log.info("[AI] response | content={}", truncate(content, 300));
        } catch (Exception e) {
            log.warn("[AI] 记录响应日志失败", e);
        }
        return response;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }
}
