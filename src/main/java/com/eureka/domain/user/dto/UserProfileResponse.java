package com.eureka.domain.user.dto;

import com.eureka.domain.user.entity.User;

// 다른 사람이 보는 프로필 - 이메일 · 이름 · 생일 같은 개인 정보는 빼고 닉네임만
// likeReceivedCount: 공개 글이 받은 좋아요 합
public record UserProfileResponse(
        Long id,
        String nickname,
        long postCount,
        long likeReceivedCount
) {
    public static UserProfileResponse from(User user, long postCount, long likeReceivedCount) {
        return new UserProfileResponse(user.getId(), user.getNickname(), postCount, likeReceivedCount);
    }
}
