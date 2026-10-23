package com.eureka.domain.image.exception;

import com.eureka.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "이미지를 찾을 수 없습니다"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I002", "이미지 업로드에 실패했습니다"),
    MAX_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "I003", "게시글당 최대 10장까지 업로드 가능합니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "I004", "허용되지 않는 파일 형식입니다"),
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "I005", "이미지에 대한 권한이 없습니다"),
    INVALID_URL(HttpStatus.INTERNAL_SERVER_ERROR, "I006", "잘못된 이미지 URL 형식입니다"),
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I007", "이미지 삭제에 실패했습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
