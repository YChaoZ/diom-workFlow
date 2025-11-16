package com.diom.common.exception;

import com.diom.common.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BIZ_ERROR.getCode();
        this.message = message;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.BIZ_ERROR.getCode();
        this.message = message;
    }

    public BizException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

