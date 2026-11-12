package com.eureka.domain.place.entity;

import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.post.entity.Post;
import com.eureka.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

// 게시글(여행 한 번)에서 들른 장소 - seq 순서대로 이으면 경로가 됨
@Entity
@Table(name = "post_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private Integer seq;

    @Column(nullable = false, length = 100)
    private String name;

    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // 카카오 장소 검색 결과의 장소 ID - 직접 찍은 위치면 null
    @Column(length = 20)
    private String kakaoPlaceId;

    // 장소 종류 (한식 · 카페 · 힐링/휴식 등) - 선택
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @Column(length = 100)
    private String memo;

    private LocalTime visitedAt;

    public void update(Integer seq, String name, String address, Double latitude, Double longitude,
                       String kakaoPlaceId, SubCategory subCategory, String memo, LocalTime visitedAt) {
        this.seq = seq;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.kakaoPlaceId = kakaoPlaceId;
        this.subCategory = subCategory;
        this.memo = memo;
        this.visitedAt = visitedAt;
    }
}
