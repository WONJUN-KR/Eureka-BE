package com.eureka.domain.post.dto;

import com.eureka.domain.place.dto.PlacePoint;
import com.eureka.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime createdAt,
        List<PlacePoint> places
) {
    // thumbnailUrl: 첫 번째(seq 최소) 이미지 URL, 이미지가 없으면 null / places: 들른 순서대로 이름 · 위치
    public static PostResponse from(Post post, String thumbnailUrl, List<PlacePoint> places) {
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
                post.getCreatedAt(),
                places
        );
    }
}
