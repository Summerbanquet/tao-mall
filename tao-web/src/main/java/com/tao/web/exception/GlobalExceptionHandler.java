package com.tao.web.exception;

import com.tao.common.entity.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理
 * 说明：异常信息统一走 logback 输出（受 logging.charset 控制），
 * 不使用 e.printStackTrace()，避免 Windows 控制台编码不一致导致中文乱码
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public AjaxResult handleBusiness(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return AjaxResult.of(e.getCode(), e.getMessage());
    }

    /**
     * 请求地址不存在（404）：如浏览器直接访问了不存在的路径、favicon.ico 等
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public AjaxResult handleNotFound(NoResourceFoundException e) {
        log.warn("请求地址不存在：{}", e.getResourcePath());
        return AjaxResult.of(404, "请求地址不存在：" + e.getResourcePath());
    }

    /**
     * 请求方式不支持（405）：如用 GET 访问了 POST 接口
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方式不支持：{} {}", e.getMethod(), e.getMessage());
        return AjaxResult.of(405, "请求方式不支持：" + e.getMethod() + "，请检查接口要求的 GET/POST/PUT/DELETE");
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AjaxResult handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? "参数错误"
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败：{}", msg);
        return AjaxResult.error(msg);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("系统异常", e);
        return AjaxResult.error("系统繁忙，请稍后再试");
    }
}