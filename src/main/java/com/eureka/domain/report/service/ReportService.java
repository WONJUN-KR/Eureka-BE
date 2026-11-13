package com.eureka.domain.report.service;

import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.exception.PostException;
import com.eureka.domain.post.repository.PostRepository;
import com.eureka.domain.report.dto.ReportRequest;
import com.eureka.domain.report.entity.PostReport;
import com.eureka.domain.report.exception.ReportException;
import com.eureka.domain.report.repository.PostReportRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.exception.UserException;
import com.eureka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    // 신고가 이만큼 쌓이면 관리자 검토 전에 글을 먼저 숨김
    public static final int HIDE_THRESHOLD = 3;

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void reportPost(Long userId, Long postId, ReportRequest request) {
        // 동시에 들어온 신고가 숨김 기준을 같이 세도록 게시글 행을 잠그고 진행
        Post post = postRepository.findByIdWithLock(postId)
                .filter(Post::isPublished)
                .orElseThrow(PostException::notFound);

        if (post.isOwner(userId)) {
            throw ReportException.cannotReportOwnPost();
        }
        if (postReportRepository.existsByPostIdAndUserId(postId, userId)) {
            throw ReportException.alreadyReported();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserException::notFound);

        postReportRepository.save(PostReport.builder()
                .post(post)
                .user(user)
                .reason(request.reason())
                .build());

        if (postReportRepository.countByPostIdAndStatus(postId, "PENDING") >= HIDE_THRESHOLD) {
            post.hide();
        }
    }
}
