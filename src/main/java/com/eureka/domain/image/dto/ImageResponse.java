package com.eureka.domain.image.dto;

import com.eureka.domain.image.entity.PostImage;

public record ImageResponse(
        Long id,
        Long postId,
        Long placeId,
        String url,
        Integer seq
) {
    public static ImageResponse from(PostImage image) {
        return new ImageResponse(
                image.getId(),
                image.getPost().getId(),
                image.getPlace() != null ? image.getPlace().getId() : null,
                image.getUrl(),
                image.getSeq()
        );
    }
}
