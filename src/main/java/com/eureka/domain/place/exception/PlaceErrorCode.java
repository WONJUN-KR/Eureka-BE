package com.eureka.domain.place.exception;

import com.eureka.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PL001", "장소를 찾을 수 없습니다"),
    PLACE_REQUIRED(HttpStatus.BAD_REQUEST, "PL002", "장소를 1곳 이상 넣어 주세요"),
    MAX_PLACE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "PL003", "장소는 게시글당 최대 20곳까지 넣을 수 있습니다"),
    MAX_PLACE_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "PL004", "장소당 사진은 최대 3장까지 올릴 수 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
