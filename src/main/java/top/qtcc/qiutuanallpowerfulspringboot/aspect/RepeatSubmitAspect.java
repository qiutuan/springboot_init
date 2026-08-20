package top.qtcc.qiutuanallpowerfulspringboot.aspect;

import cn.dev33.satoken.stp.StpUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.qtcc.qiutuanallpowerfulspringboot.annotation.RepeatSubmit;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.utils.NetUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面（按 用户ID/IP + 接口 维度，登录/注销等匿名场景互不阻塞）
 *
 * @author qiutuan
 * @date 2024/12/10
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                RequestContextHolder.getRequestAttributes())).getRequest();

        String identity;
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId != null) {
            identity = "u" + loginId;
        } else {
            identity = "ip:" + NetUtils.getIpAddress(request);
        }

        String repeatKey = "repeat_submit:" + request.getRequestURI() + ":" + identity;

        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(repeatKey, 1,
                repeatSubmit.interval(), TimeUnit.MILLISECONDS))) {
            return point.proceed();
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, repeatSubmit.message());
    }
}
