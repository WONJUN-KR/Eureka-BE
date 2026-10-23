package com.eureka.domain.region.repository;

import com.eureka.domain.region.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByRegionId(Long regionId);
}
