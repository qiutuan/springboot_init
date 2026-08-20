package top.qtcc.qiutuanallpowerfulspringboot.domain.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 对话请求
 *
 * @author qiutuan
 */
@Data
public class AiChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息不能为空")
    @Size(max = 4000, message = "消息过长")
    private String message;

    /**
     * 会话 ID（不传则服务端生成，用于多轮上下文）
     */
    private String conversationId;
}
