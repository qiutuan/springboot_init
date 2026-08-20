package top.qtcc.qiutuanallpowerfulspringboot.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.RequestLog;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.service.RequestLogService;
import top.qtcc.qiutuanallpowerfulspringboot.utils.NetUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;

/**
 * 请求日志切面（异步落库，避免阻塞业务线程）
 *
 * @author qiutuan
 * @date 2024/12/07
 */
@Aspect
@Component
@Slf4j
public class RequestLogAspect {

    @Resource
    private RequestLogService requestLogService;

    @Around("execution(* top.qtcc.qiutuanallpowerfulspringboot..controller..*.*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(UUID.randomUUID().toString().replace("-", ""));

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            request = servletRequestAttributes.getRequest();
        }
        if (request != null) {
            requestLog.setUrl(request.getRequestURI());
            requestLog.setMethod(request.getMethod());
            requestLog.setIp(NetUtils.getIpAddress(request));
            requestLog.setParams(serializeArgs(joinPoint.getArgs()));
        }
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId != null) {
            requestLog.setUserId(Long.valueOf(loginId.toString()));
        }

        Object result;
        try {
            result = joinPoint.proceed();
            requestLog.setStatus(200);
        } catch (BusinessException e) {
            requestLog.setStatus(e.getCode());
            requestLog.setErrorMsg(e.getMessage());
            throw e;
        } catch (Exception e) {
            requestLog.setStatus(500);
            requestLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            requestLog.setCostTime(System.currentTimeMillis() - startTime);
            // 异步保存，避免每请求一次同步 DB 写
            requestLogService.asyncSave(requestLog);
        }
        return result;
    }

    /**
     * 序列化请求参数（过滤 Servlet/MultipartFile，截断避免超长）
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            String json = JSONUtil.toJsonStr(Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest)
                            && !(arg instanceof HttpServletResponse)
                            && !(arg instanceof MultipartFile))
                    .toArray());
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (Exception e) {
            log.warn("请求参数序列化失败", e);
            return "[]";
        }
    }
}
