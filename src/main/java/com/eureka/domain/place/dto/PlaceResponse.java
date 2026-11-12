package com.eureka.domain.place.dto;

import com.eureka.domain.image.dto.ImageResponse;
import com.eureka.domain.place.entity.PostPlace;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.List;

public record PlaceResponse(
        Long id,
        Integer seq,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String kakaoPlaceId,
        Long subCategoryId,
        String subCategoryName,
        String memo,
        @JsonFormat(pattern = "HH:mm") LocalTime visitedAt,
        List<ImageResponse> images
) {
    public static PlaceResponse from(PostPlace place, List<ImageResponse> images) {
        return new PlaceResponse(
                place.getId(),
                place.getSeq(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getKakaoPlaceId(),
                place.getSubCategory() != null ? place.getSubCategory().getId() : null,
                place.getSubCategory() != null ? place.getSubCategory().getName() : null,
                place.getMemo(),
                place.getVisitedAt(),
                images
        );
    }
}
