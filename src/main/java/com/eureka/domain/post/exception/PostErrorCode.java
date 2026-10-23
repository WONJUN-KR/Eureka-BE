package com.eureka.domain.post.exception;

import com.eureka.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시글을 찾을 수 없습니다"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "P002", "게시글에 대한 권한이 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
