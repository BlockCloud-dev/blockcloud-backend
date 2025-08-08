package com.blockcloud.exception;

import com.blockcloud.exception.error.ErrorCode;
import io.jsonwebtoken.JwtException;
import lombok.Getter;

@Getter
public class HttpSecurityException extends JwtException {

    private final ErrorCode errorCode;

    public HttpSecurityException(String message, ErrorCode errorCode) {
        super(message);

        this.errorCode = errorCode;
    }
}