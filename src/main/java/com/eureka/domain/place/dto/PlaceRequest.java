package com.eureka.domain.place.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class PlaceRequest {

    // 수정할 때 기존 장소면 ID, 새 장소면 null
    private Long id;

    @NotBlank(message = "장소 이름은 필수입니다")
    @Size(max = 100, message = "장소 이름은 100자 이하여야 합니다")
    private String name;

    @Size(max = 255, message = "주소는 255자 이하여야 합니다")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "-90.0", message = "위도 범위를 벗어났습니다")
    @DecimalMax(value = "90.0", message = "위도 범위를 벗어났습니다")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "-180.0", message = "경도 범위를 벗어났습니다")
    @DecimalMax(value = "180.0", message = "경도 범위를 벗어났습니다")
    private Double longitude;

    @Size(max = 20, message = "장소 ID는 20자 이하여야 합니다")
    private String kakaoPlaceId;

    private Long subCategoryId;

    @Size(max = 100, message = "메모는 100자 이하여야 합니다")
    private String memo;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime visitedAt;
}
