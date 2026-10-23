package com.eureka.domain.like.dto;

public record LikeResponse(
        Long postId,
        boolean liked,
        int likeCount
) {
    public static LikeResponse of(Long postId, boolean liked, int likeCount) {
        return new LikeResponse(postId, liked, likeCount);
    }
}
