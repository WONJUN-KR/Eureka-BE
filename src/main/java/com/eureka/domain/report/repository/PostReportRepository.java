package com.eureka.domain.report.repository;

import com.eureka.domain.report.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // 숨김 기준은 아직 처리 안 된(PENDING) 신고만 셈 - 관리자가 반려한 신고는 빠짐 (v5)
    long countByPostIdAndStatus(Long postId, String status);
}
