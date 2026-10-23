package com.eureka.domain.like.controller;

import com.eureka.domain.like.dto.LikeResponse;
import com.eureka.domain.like.service.LikeService;
import com.eureka.global.common.ApiResponse;
import com.eureka.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like", description = "좋아요 API")
@RestController
@RequestMapping("/api/posts/{postId}/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "좋아요 토글")
    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        LikeResponse response = likeService.toggleLike(userDetails.getUserId(), postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "좋아요 상태 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<LikeResponse>> getLikeStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {
        LikeResponse response = likeService.getLikeStatus(userDetails.getUserId(), postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
