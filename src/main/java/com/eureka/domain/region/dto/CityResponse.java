package com.eureka.domain.region.dto;

import com.eureka.domain.region.entity.City;

public record CityResponse(
        Long id,
        String name
) {
    public static CityResponse from(City city) {
        return new CityResponse(city.getId(), city.getName());
    }
}
