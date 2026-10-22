package com.footprint.domain.post.dto;

import com.footprint.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String summary,
        String authorNickname,
        String cityName,
        String subCategoryName,
        Integer viewCount,
        Integer likeCount,
        String status,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
    // thumbnailUrl: 첫 번째(seq 최소) 이미지 URL, 이미지가 없으면 null
    public static PostResponse from(Post post, String thumbnailUrl) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getUser().getNickname(),
                post.getCity().getName(),
                post.getSubCategory().getName(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getStatus(),
                thumbnailUrl,
                post.getCreatedAt()
        );
    }
}
