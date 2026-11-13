package com.eureka.domain.auth.oauth;

// 카카오에서 받은 회원 정보 - 회원번호와 (동의했으면) 닉네임
public record KakaoUser(String id, String nickname) {
}
