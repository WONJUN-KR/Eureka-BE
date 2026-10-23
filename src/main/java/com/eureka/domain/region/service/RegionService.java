package com.eureka.domain.region.service;

import com.eureka.domain.region.dto.CityResponse;
import com.eureka.domain.region.dto.RegionResponse;
import com.eureka.domain.region.exception.RegionException;
import com.eureka.domain.region.repository.CityRepository;
import com.eureka.domain.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;

    @Cacheable("regions")
    public List<RegionResponse> getRegions() {
        return regionRepository.findAll().stream()
                .map(RegionResponse::from)
                .toList();
    }

    @Cacheable(value = "cities", key = "#regionId")
    public List<CityResponse> getCities(Long regionId) {
        if (!regionRepository.existsById(regionId)) {
            throw RegionException.notFound();
        }
        return cityRepository.findByRegionId(regionId).stream()
                .map(CityResponse::from)
                .toList();
    }
}
