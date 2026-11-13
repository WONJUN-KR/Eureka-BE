package com.eureka.domain.report.entity;

import com.eureka.domain.post.entity.Post;
import com.eureka.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 같은 사람은 같은 글을 한 번만 신고
@Entity
@Table(name = "post_reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportReason reason;

    // 공통코드 REPORT_STATUS (PENDING, RESOLVED, REJECTED) - 관리자 검토는 v5
    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
