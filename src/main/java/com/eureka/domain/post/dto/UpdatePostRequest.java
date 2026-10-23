package com.eureka.domain.post.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
