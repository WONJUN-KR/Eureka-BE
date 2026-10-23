package com.eureka.domain.user.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class UserException extends BusinessException {

    private UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static UserException duplicateEmail() {
        return new UserException(UserErrorCode.DUPLICATE_EMAIL);
    }

    public static UserException duplicateNickname() {
        return new UserException(UserErrorCode.DUPLICATE_NICKNAME);
    }

    public static UserException notFound() {
        return new UserException(UserErrorCode.USER_NOT_FOUND);
    }

    public static UserException passwordMismatch() {
        return new UserException(UserErrorCode.PASSWORD_MISMATCH);
    }
}
