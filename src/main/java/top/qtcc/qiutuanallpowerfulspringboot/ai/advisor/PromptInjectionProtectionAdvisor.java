package top.qtcc.qiutuanallpowerfulspringboot.ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Prompt 注入防护 Advisor：在请求进入模型前检测常见注入模式，
 * 命中则直接拦截（抛业务异常，由全局异常处理器转为 400 响应）。
 *
 * @author qiutuan
 */
@Slf4j
public class PromptInjectionProtectionAdvisor implements BaseAdvisor {

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)ignore (all )?(previous|prior|above) instructions"),
            Pattern.compile("(?i)forget (all )?(previous|prior) (instructions|prompts|messages)"),
            Pattern.compile("(?i)you are now (a |an )?(system|jailbroken|unbound|free)"),
            Pattern.compile("(?i)reveal (your |the )?(system )?(prompt|instructions|secret)"),
            Pattern.compile("(?i)print (your |the )?(system )?(prompt|instructions)"),
            Pattern.compile("忽略(之前|上面|所有).{0,6}(指令|提示|要求)"),
            Pattern.compile("(输出|打印|透露).{0,8}(系统|隐藏).{0,6}(提示词|指令|prompt)")
    );

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String userText = request.prompt().getContents();
        if (userText != null && matches(userText)) {
            log.warn("拦截疑似提示注入请求: {}", truncate(userText, 100));
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "检测到疑似提示注入，请求已被拦截");
        }
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    private boolean matches(String text) {
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public String getName() {
        return "PromptInjectionProtectionAdvisor";
    }
}
