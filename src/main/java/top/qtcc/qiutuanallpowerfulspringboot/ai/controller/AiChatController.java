package top.qtcc.qiutuanallpowerfulspringboot.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import top.qtcc.qiutuanallpowerfulspringboot.ai.service.AiChatService;
import top.qtcc.qiutuanallpowerfulspringboot.common.BaseResponse;
import top.qtcc.qiutuanallpowerfulspringboot.common.ResultUtils;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.ai.AiChatRequest;

import java.util.UUID;

/**
 * AI 对话接口（登录可见）
 *
 * @author qiutuan
 */
@RestController
@RequestMapping("/api/ai")
@SaCheckLogin
public class AiChatController {

    @Resource
    private AiChatService aiChatService;

    /**
     * 非流式对话
     */
    @PostMapping("/chat")
    public BaseResponse<String> chat(@RequestBody @Valid AiChatRequest request) {
        String conversationId = StringUtils.defaultIfBlank(request.getConversationId(), UUID.randomUUID().toString());
        return ResultUtils.success(aiChatService.chat(conversationId, request.getMessage()));
    }

    /**
     * 流式对话（SSE：text/event-stream）
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody @Valid AiChatRequest request) {
        String conversationId = StringUtils.defaultIfBlank(request.getConversationId(), UUID.randomUUID().toString());
        return aiChatService.chatStream(conversationId, request.getMessage());
    }
}
