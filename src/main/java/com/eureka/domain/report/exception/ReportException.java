package com.eureka.domain.report.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class ReportException extends BusinessException {

    private ReportException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static ReportException alreadyReported() {
        return new ReportException(ReportErrorCode.ALREADY_REPORTED);
    }

    public static ReportException cannotReportOwnPost() {
        return new ReportException(ReportErrorCode.CANNOT_REPORT_OWN_POST);
    }
}
