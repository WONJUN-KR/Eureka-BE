package com.eureka.domain.report.controller;

import com.eureka.domain.report.dto.ReportRequest;
import com.eureka.domain.report.service.ReportService;
import com.eureka.global.common.ApiResponse;
import com.eureka.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequestMapping("/api/posts/{postId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고", description = "한 사람당 한 번, 본인 글은 불가. 신고 3건이 쌓이면 글을 숨김")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> reportPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody ReportRequest request) {
        reportService.reportPost(userDetails.getUserId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
