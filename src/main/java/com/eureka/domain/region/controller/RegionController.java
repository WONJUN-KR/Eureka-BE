package com.eureka.domain.region.controller;

import com.eureka.domain.region.dto.CityResponse;
import com.eureka.domain.region.dto.RegionResponse;
import com.eureka.domain.region.service.RegionService;
import com.eureka.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Region", description = "지역 API")
@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "지역 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegionResponse>>> getRegions() {
        List<RegionResponse> regions = regionService.getRegions();
        return ResponseEntity.ok(ApiResponse.success(regions));
    }

    @Operation(summary = "도시 목록 조회")
    @GetMapping("/{regionId}/cities")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getCities(@PathVariable Long regionId) {
        List<CityResponse> cities = regionService.getCities(regionId);
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}
