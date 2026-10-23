package com.eureka.domain.like.service;

import com.eureka.domain.like.dto.LikeResponse;
import com.eureka.domain.like.entity.PostLike;
import com.eureka.domain.like.repository.PostLikeRepository;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.exception.PostException;
import com.eureka.domain.post.repository.PostRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.exception.UserException;
import com.eureka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponse toggleLike(Long userId, Long postId) {
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(PostException::notFound);
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::notFound);

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean liked;
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.decrementLikeCount();
            liked = false;
        } else {
            PostLike postLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(postLike);
            post.incrementLikeCount();
            liked = true;
        }

        return LikeResponse.of(postId, liked, post.getLikeCount());
    }

    public LikeResponse getLikeStatus(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostException::notFound);

        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return LikeResponse.of(postId, liked, post.getLikeCount());
    }
}
