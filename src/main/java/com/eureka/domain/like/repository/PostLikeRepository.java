package com.eureka.domain.like.repository;

import com.eureka.domain.like.entity.PostLike;
import com.eureka.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // 내가 좋아요한 글 - 공개 · 삭제 안 된 글만, 좋아요 누른 순(같은 시각이면 나중 좋아요 먼저)
    // 글쓴이 · 도시 · 카테고리는 응답에 쓰므로 같이 가져옴
    @Query(value = "SELECT p FROM PostLike pl JOIN pl.post p " +
            "JOIN FETCH p.user JOIN FETCH p.city JOIN FETCH p.subCategory " +
            "WHERE pl.user.id = :userId AND p.status = 'PUBLISHED' AND p.deletedAt IS NULL " +
            "ORDER BY pl.createdAt DESC, pl.id DESC",
            countQuery = "SELECT COUNT(pl) FROM PostLike pl JOIN pl.post p " +
                    "WHERE pl.user.id = :userId AND p.status = 'PUBLISHED' AND p.deletedAt IS NULL")
    Page<Post> findLikedPosts(@Param("userId") Long userId, Pageable pageable);
}
