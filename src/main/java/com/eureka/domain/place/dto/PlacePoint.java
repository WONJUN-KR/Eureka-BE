package com.eureka.domain.place.dto;

import com.eureka.domain.place.entity.PostPlace;

// 목록 카드 · 지도 경로용 - 장소 이름과 위치만
public record PlacePoint(
        String name,
        Double latitude,
        Double longitude
) {
    public static PlacePoint from(PostPlace place) {
        return new PlacePoint(place.getName(), place.getLatitude(), place.getLongitude());
    }
}
