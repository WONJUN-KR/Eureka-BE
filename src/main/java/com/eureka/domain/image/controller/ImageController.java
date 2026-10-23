package com.eureka.domain.image.controller;

import com.eureka.domain.image.dto.ImageResponse;
import com.eureka.domain.image.service.ImageService;
import com.eureka.global.common.ApiResponse;
import com.eureka.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Image", description = "이미지 API")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "게시글 이미지 업로드")
    @PostMapping(value = "/api/posts/{postId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> uploadImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file) {
        ImageResponse response = imageService.uploadImage(userDetails.getUserId(), postId, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "게시글 이미지 목록 조회")
    @GetMapping("/api/posts/{postId}/images")
    public ResponseEntity<ApiResponse<List<ImageResponse>>> getImages(@PathVariable Long postId) {
        List<ImageResponse> response = imageService.getImages(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "이미지 삭제")
    @DeleteMapping("/api/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId) {
        imageService.deleteImage(userDetails.getUserId(), imageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
