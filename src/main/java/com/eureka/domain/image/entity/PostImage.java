package com.eureka.domain.image.entity;

import com.eureka.domain.place.entity.PostPlace;
import com.eureka.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_images")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 장소에 붙은 사진이면 그 장소, 게시글 전체 사진이면 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private PostPlace place;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Integer seq;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 장소가 글에서 빠지면 사진은 게시글 사진으로 남김
    public void detachPlace() {
        this.place = null;
    }
}
