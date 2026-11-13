package com.eureka.domain.place.repository;

import com.eureka.domain.place.entity.PostPlace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostPlaceRepository extends JpaRepository<PostPlace, Long> {

    // 상세용 - 세부 종류 이름까지 한 번에 (장소마다 따로 조회하지 않게)
    @EntityGraph(attributePaths = "subCategory")
    List<PostPlace> findByPostIdOrderBySeqAsc(Long postId);

    // 목록용 - 한 페이지의 게시글 장소를 한 번에 조회
    List<PostPlace> findByPostIdInOrderBySeqAsc(Collection<Long> postIds);
}
