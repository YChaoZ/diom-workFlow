package com.diom.auth.exception;

import com.diom.common.dto.Result;
import com.diom.common.enums.ResultCode;
import com.diom.common.exception.BizException;
import com.diom.common.exception.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 *
 * @author diom
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.error("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(SysException.class)
    public Result<Void> handleSysException(SysException e) {
        log.error("系统异常：", e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getDefaultMessage()).append("; ");
        }
        log.error("参数校验失败：{}", errorMsg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), errorMsg.toString());
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getDefaultMessage()).append("; ");
        }
        log.error("参数绑定失败：{}", errorMsg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), errorMsg.toString());
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常：", e);
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
    }
}

