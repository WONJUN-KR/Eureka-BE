package com.eureka.domain.post.repository;

import com.eureka.domain.post.entity.Post;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdWithLock(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND (:regionId IS NULL OR p.city.region.id = :regionId) " +
            "AND (:cityId IS NULL OR p.city.id = :cityId) " +
            "AND (:categoryId IS NULL OR p.subCategory.category.id = :categoryId) " +
            "AND (:subCategoryId IS NULL OR p.subCategory.id = :subCategoryId)")
    Page<Post> findAllWithFilters(
            @Param("regionId") Long regionId,
            @Param("cityId") Long cityId,
            @Param("categoryId") Long categoryId,
            @Param("subCategoryId") Long subCategoryId,
            Pageable pageable
    );

    Page<Post> findByUserId(Long userId, Pageable pageable);
}
