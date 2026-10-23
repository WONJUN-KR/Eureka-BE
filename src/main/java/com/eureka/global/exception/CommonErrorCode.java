package com.eureka.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "C002", "필수 파라미터가 누락되었습니다"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "유효하지 않은 파라미터입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "요청한 리소스를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
