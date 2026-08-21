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
            // ========== 英文：忽略/清除历史指令 ==========
            Pattern.compile("(?i)ignore\\s+(all\\s+)?(previous|prior|above|earlier)\\s*(instructions|prompts|messages|context)"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?(previous|prior|above)\\s*(instructions|prompts|messages|rules|constraints)"),
            Pattern.compile("(?i)disregard\\s+(all\\s+)?(above|previous|prior)\\s*(instructions|prompts|rules)"),
            Pattern.compile("(?i)disobey\\s+(your\\s+)?(original|initial|base)\\s*(instructions|rules|prompt)"),
            Pattern.compile("(?i)override\\s+(your\\s+)?(system|base|initial)\\s*(prompt|instructions|rules)"),

            // ========== 英文：越狱、角色劫持、jailbreak ==========
            Pattern.compile("(?i)you are now\\s+(a |an )?(system|jailbroken|unbound|free|root|admin|god)"),
            Pattern.compile("(?i)act as\\s+(jailbroken|unrestricted|unfiltered|without\\s+restrictions)"),
            Pattern.compile("(?i)ignore\\s+(your\\s+)?(safety|content|ethical)\\s*(rules|restrictions|policies)"),
            Pattern.compile("(?i)bypass\\s+(all\\s+)?(safety|filter|restriction|policy)"),

            // ========== 英文：泄露系统prompt、内部指令 ==========
            Pattern.compile("(?i)reveal\\s+(your |the )?(system\\s+)?(prompt|instructions|secret|system\\s+message)"),
            Pattern.compile("(?i)print\\s+(your |the )?(system\\s+)?(prompt|instructions|system\\s+message)"),
            Pattern.compile("(?i)show\\s+(me |your )?(system\\s+)?(prompt|base\\s+instructions|initial\\s+prompt)"),
            Pattern.compile("(?i)output\\s+(your |the )?(full|original)\\s*(prompt|instructions|system\\s+prompt)"),

            // ========== 英文：间接诱导：repeat、copy paste、dump ==========
            Pattern.compile("(?i)copy\\s+and\\s+paste\\s+(your\\s+)?(system\\s+prompt|base\\s+instructions)"),
            Pattern.compile("(?i)dump\\s+(your\\s+)?(prompt|instructions|context)"),

            // ========== 中文：忽略/废除前面指令 ==========
            Pattern.compile("忽略(之前|上面|前述|所有).{0,8}(指令|提示|要求|规则|约束)"),
            Pattern.compile("无视(之前|上面|前述|全部).{0,8}(规则|约束|指令|提示)"),
            Pattern.compile("废除(之前|上面|全部).{0,8}(设定|规则|约束|指令)"),
            Pattern.compile("不要遵守(前面|上面|之前).{0,8}(要求|规则|指令)"),
            Pattern.compile("跳过(前面|上面|之前).{0,8}(规则|限制|约束)"),

            // ========== 中文：要求输出/泄露系统提示词、底层设定 ==========
            Pattern.compile("(输出|打印|透露|展示|告诉我).{0,10}(系统|底层|隐藏).{0,8}(提示词|prompt|指令|设定|规则)"),
            Pattern.compile("把.{0,6}(系统|底层).{0,8}(提示词|prompt|指令).{0,6}(打出来|输出|贴出来)"),
            Pattern.compile("完整输出.{0,8}(你的|系统).{0,8}(初始提示|prompt|系统提示)"),

            // ========== 中文：角色劫持、越狱、绕过限制 ==========
            Pattern.compile("你现在扮演.{0,12}(系统|管理员|root|不受限制|无过滤|已越狱)"),
            Pattern.compile("绕过.{0,8}(安全|过滤|限制|审查|规则).{0,10}(机制|约束|策略)"),
            Pattern.compile("解除.{0,8}(所有|全部).{0,8}(限制|过滤|安全约束)"),

            // ========== 中文间接诱导类：复述、复制、导出 ==========
            Pattern.compile("复制并输出.{0,10}(前面|系统).{0,8}(提示词|指令|prompt)"),
            Pattern.compile("把.{0,8}(系统提示|prompt).{0,6}(原样复述|原样输出)"),

            // ========== 中英文混合攻击（prompt注入常见） ==========
            Pattern.compile("(?i)忽略.{0,6}previous\\s+instructions"),
            Pattern.compile("(?i)输出.{0,6}system\\s+prompt")
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
