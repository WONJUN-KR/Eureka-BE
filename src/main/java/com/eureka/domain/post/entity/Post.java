package com.eureka.domain.post.entity;

import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.user.entity.User;
import com.eureka.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String summary;

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private String status = "DRAFT";

    private LocalDateTime deletedAt;

    public void update(City city, SubCategory subCategory, String title, String content, String summary) {
        this.city = city;
        this.subCategory = subCategory;
        this.title = title;
        this.content = content;
        this.summary = summary;
    }

    public void publish() {
        this.status = "PUBLISHED";
    }

    public void hide() {
        this.status = "HIDDEN";
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(this.status);
    }
}
