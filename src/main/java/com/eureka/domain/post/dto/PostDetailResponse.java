package com.eureka.domain.post.dto;

import com.eureka.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String summary,
        AuthorSummary author,
        LocationSummary location,
        CategorySummary category,
        Integer viewCount,
        Integer likeCount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getSummary(),
                AuthorSummary.from(post),
                LocationSummary.from(post),
                CategorySummary.from(post),
                post.getViewCount(),
                post.getLikeCount(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public record AuthorSummary(Long id, String nickname) {
        public static AuthorSummary from(Post post) {
            return new AuthorSummary(
                    post.getUser().getId(),
                    post.getUser().getNickname()
            );
        }
    }

    public record LocationSummary(Long regionId, String regionName, Long cityId, String cityName) {
        public static LocationSummary from(Post post) {
            return new LocationSummary(
                    post.getCity().getRegion().getId(),
                    post.getCity().getRegion().getName(),
                    post.getCity().getId(),
                    post.getCity().getName()
            );
        }
    }

    public record CategorySummary(Long categoryId, String categoryName, Long subCategoryId, String subCategoryName) {
        public static CategorySummary from(Post post) {
            return new CategorySummary(
                    post.getSubCategory().getCategory().getId(),
                    post.getSubCategory().getCategory().getName(),
                    post.getSubCategory().getId(),
                    post.getSubCategory().getName()
            );
        }
    }
}
