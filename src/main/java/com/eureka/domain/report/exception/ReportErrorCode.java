package com.eureka.domain.report.exception;

import com.eureka.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

    ALREADY_REPORTED(HttpStatus.CONFLICT, "RP001", "이미 신고한 게시글입니다"),
    CANNOT_REPORT_OWN_POST(HttpStatus.BAD_REQUEST, "RP002", "본인의 게시글은 신고할 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
