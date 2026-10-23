package com.eureka.domain.category.dto;

import com.eureka.domain.category.entity.SubCategory;

public record SubCategoryResponse(
        Long id,
        String name
) {
    public static SubCategoryResponse from(SubCategory subCategory) {
        return new SubCategoryResponse(subCategory.getId(), subCategory.getName());
    }
}
