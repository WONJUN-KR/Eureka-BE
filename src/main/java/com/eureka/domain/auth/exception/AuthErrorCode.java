package com.eureka.domain.auth.exception;

import com.eureka.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "A001", "로그인이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "권한이 없습니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "토큰이 만료되었습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 토큰입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
