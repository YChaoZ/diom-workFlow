package com.diom.common.exception;

import com.diom.common.enums.ResultCode;
import lombok.Getter;

/**
 * 系统异常
 */
@Getter
public class SysException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public SysException() {
        super();
    }

    public SysException(String message) {
        super(message);
        this.code = ResultCode.SYS_ERROR.getCode();
        this.message = message;
    }

    public SysException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public SysException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public SysException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.SYS_ERROR.getCode();
        this.message = message;
    }

    public SysException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

