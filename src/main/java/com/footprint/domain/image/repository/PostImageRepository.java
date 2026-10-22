package com.footprint.domain.image.repository;

import com.footprint.domain.image.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdOrderBySeqAsc(Long postId);

    // 목록 썸네일용 - 한 페이지의 게시글 이미지를 한 번에 조회 (게시글마다 따로 조회하지 않게)
    List<PostImage> findByPostIdInOrderBySeqAsc(Collection<Long> postIds);

    int countByPostId(Long postId);

    @Query("SELECT MAX(pi.seq) FROM PostImage pi WHERE pi.post.id = :postId")
    Integer findMaxSeqByPostId(@Param("postId") Long postId);
}
