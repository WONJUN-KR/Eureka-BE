package com.eureka.domain.post.dto;

import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePostRequest {

    @NotNull(message = "도시 ID는 필수입니다")
    private Long cityId;

    @NotNull(message = "서브카테고리 ID는 필수입니다")
    private Long subCategoryId;

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 100, message = "제목은 1-100자여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(min = 1, max = 10000, message = "내용은 1-10000자여야 합니다")
    private String content;

    @Size(max = 200, message = "요약은 200자 이하여야 합니다")
    private String summary;

    public Post toEntity(User user, City city, SubCategory subCategory) {
        return Post.builder()
                .user(user)
                .city(city)
                .subCategory(subCategory)
                .title(title)
                .content(content)
                .summary(summary)
                .build();
    }
}
