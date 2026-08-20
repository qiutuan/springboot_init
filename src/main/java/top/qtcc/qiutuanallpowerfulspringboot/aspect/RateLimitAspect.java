package top.qtcc.qiutuanallpowerfulspringboot.aspect;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.qtcc.qiutuanallpowerfulspringboot.annotation.RateLimit;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.utils.NetUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 接口限流（Redis 计数器，key 增加用户/IP 维度，避免单用户拖垮全站）
 *
 * @author qiutuan
 * @date 2024/12/06
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // key = 类名:方法名:用户ID(或IP)
        String identity = resolveIdentity();
        String key = method.getDeclaringClass().getName() + ":" + method.getName() + ":" + identity;
        int time = rateLimit.time();
        int count = rateLimit.count();
        if (!tryAcquire(key, count, time)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "访问过于频繁，请稍后再试");
        }
        return point.proceed();
    }

    /**
     * 已登录用用户 ID，未登录用 IP
     */
    private String resolveIdentity() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId != null) {
            return "u" + loginId;
        }
        HttpServletRequest request = currentRequest();
        return "ip:" + NetUtils.getIpAddress(request);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private boolean tryAcquire(String key, int count, int time) {
        String script = "local current = redis.call('incr', KEYS[1]) " +
                "if current == 1 then " +
                "   redis.call('expire', KEYS[1], ARGV[1]) " +
                "end " +
                "if current <= tonumber(ARGV[2]) then " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";
        return Boolean.TRUE.equals(stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class),
                Collections.singletonList(key), String.valueOf(time), String.valueOf(count)));
    }
}
