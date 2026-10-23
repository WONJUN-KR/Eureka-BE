package com.eureka.domain.auth.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class AuthException extends BusinessException {

    private AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static AuthException loginRequired() {
        return new AuthException(AuthErrorCode.LOGIN_REQUIRED);
    }

    public static AuthException forbidden() {
        return new AuthException(AuthErrorCode.FORBIDDEN);
    }

    public static AuthException expiredToken() {
        return new AuthException(AuthErrorCode.EXPIRED_TOKEN);
    }

    public static AuthException invalidToken() {
        return new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
}
