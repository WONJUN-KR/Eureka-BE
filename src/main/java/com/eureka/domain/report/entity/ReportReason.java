package com.eureka.domain.report.entity;

// 신고 사유 - docs/business-rules.md 의 5가지
public enum ReportReason {
    FALSE_INFO,     // 허위 정보
    INAPPROPRIATE,  // 부적절한 내용
    SPAM,           // 스팸/광고
    COPYRIGHT,      // 저작권 침해
    ETC             // 기타
}
