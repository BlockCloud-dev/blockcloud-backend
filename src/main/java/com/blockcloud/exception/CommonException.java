package com.blockcloud.exception;

import com.blockcloud.exception.error.ErrorCode;
import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public CommonException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public CommonException(ErrorCode errorCode, String customMessage) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage() + " " + customMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static CommonException of(ErrorCode errorCode, String message) {
        return new CommonException(errorCode, message);
    }

    public static CommonException of(ErrorCode errorCode) {
        return new CommonException(errorCode);
    }
}
