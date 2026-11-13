package com.eureka.domain.user.controller;

import com.eureka.domain.post.dto.PostResponse;
import com.eureka.domain.post.service.PostService;
import com.eureka.domain.user.dto.UpdateUserRequest;
import com.eureka.domain.user.dto.UserProfileResponse;
import com.eureka.domain.user.dto.UserResponse;
import com.eureka.domain.user.service.UserService;
import com.eureka.global.common.ApiResponse;
import com.eureka.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = userService.getMyProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateMyProfile(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteMyAccount(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "내가 쓴 글 목록")
    @GetMapping("/me/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponse> response = postService.getMyPosts(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내가 좋아요한 글 목록", description = "공개 글만, 좋아요 누른 순 고정 (sort 파라미터는 무시)")
    @GetMapping("/me/likes")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getMyLikedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<PostResponse> response = postService.getMyLikedPosts(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 아래 두 API 는 비로그인 공개 - {userId} 는 숫자만 받아서 /me 와 겹치지 않게 함
    @Operation(summary = "사용자 프로필 조회", description = "닉네임 · 공개한 글 수 · 받은 좋아요 수")
    @GetMapping("/{userId:\\d+}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable Long userId) {
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "사용자가 쓴 글 목록", description = "공개된 글만, 기본 최신순")
    @GetMapping("/{userId:\\d+}/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getUserPosts(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostResponse> response = postService.getUserPosts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
