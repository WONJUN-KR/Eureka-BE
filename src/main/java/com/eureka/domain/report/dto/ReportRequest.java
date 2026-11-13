package com.eureka.domain.report.dto;

import com.eureka.domain.report.entity.ReportReason;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        @NotNull
        ReportReason reason
) {
}
