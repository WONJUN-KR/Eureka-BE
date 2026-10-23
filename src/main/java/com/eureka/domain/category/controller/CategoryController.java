package com.eureka.domain.category.controller;

import com.eureka.domain.category.dto.CategoryResponse;
import com.eureka.domain.category.dto.SubCategoryResponse;
import com.eureka.domain.category.service.CategoryService;
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

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        List<CategoryResponse> categories = categoryService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(summary = "서브카테고리 목록 조회")
    @GetMapping("/{categoryId}/sub")
    public ResponseEntity<ApiResponse<List<SubCategoryResponse>>> getSubCategories(@PathVariable Long categoryId) {
        List<SubCategoryResponse> subCategories = categoryService.getSubCategories(categoryId);
        return ResponseEntity.ok(ApiResponse.success(subCategories));
    }
}
