package com.eureka.domain.post.dto;

import com.eureka.domain.place.dto.PlaceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdatePostRequest {

    private Long cityId;

    private Long subCategoryId;

    @Size(min = 1, max = 100, message = "제목은 1-100자여야 합니다")
    private String title;

    @Size(min = 1, max = 10000, message = "내용은 1-10000자여야 합니다")
    private String content;

    @Size(max = 200, message = "요약은 200자 이하여야 합니다")
    private String summary;

    // null 이면 장소는 그대로, 값이 있으면 이 목록(순서 포함)으로 통째로 바꿈 - 기존 장소는 id 를 같이 보냄
    @Size(min = 1, max = 20, message = "장소는 1곳 이상 20곳 이하여야 합니다")
    private List<@Valid @NotNull PlaceRequest> places;
}
