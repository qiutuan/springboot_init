package top.qtcc.qiutuanallpowerfulspringboot.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import top.qtcc.qiutuanallpowerfulspringboot.common.BaseResponse;
import top.qtcc.qiutuanallpowerfulspringboot.common.ResultUtils;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常（warn 级别，避免预期内异常刷错误日志）
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常 code={}, message={}", e.getCode(), e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * Sa-Token 未登录
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> handleNotLoginException(NotLoginException e) {
        log.warn("未登录: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR.getCode(), "未登录或登录已过期");
    }

    /**
     * Sa-Token 无角色
     */
    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> handleNotRoleException(NotRoleException e) {
        log.warn("无角色权限: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR.getCode(), "无权限访问");
    }

    /**
     * Sa-Token 无权限
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("无接口权限: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR.getCode(), "无权限访问");
    }

    /**
     * 上传文件超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseResponse<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "文件大小超出限制");
    }

    /**
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 处理参数校验异常（方法参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
    }
}
