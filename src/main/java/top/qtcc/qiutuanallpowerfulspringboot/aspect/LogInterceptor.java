package top.qtcc.qiutuanallpowerfulspringboot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 请求响应日志 AOP（traceId 由 logback pattern 中的 %X{traceId} 输出）
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Aspect
@Component
@Slf4j
public class LogInterceptor {

    /**
     * 执行拦截
     */
    @Around("execution(* top.qtcc.qiutuanallpowerfulspringboot..controller..*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = null;
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            httpServletRequest = servletRequestAttributes.getRequest();
        }
        String requestId = UUID.randomUUID().toString();
        String url = httpServletRequest != null ? httpServletRequest.getRequestURI() : "N/A";
        String ip = httpServletRequest != null ? httpServletRequest.getRemoteHost() : "127.0.0.1";
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        log.info("request start, id: {}, path: {}, ip: {}, params: {}", requestId, url, ip, reqParam);
        Object result = point.proceed();
        stopWatch.stop();
        log.info("request end, id: {}, cost: {}ms", requestId, stopWatch.getTotalTimeMillis());
        return result;
    }
}
