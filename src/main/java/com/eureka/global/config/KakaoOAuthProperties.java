package com.eureka.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 카카오 로그인 설정 - 값은 application-local.yml 의 oauth.kakao (gitignore)
// frontendRedirectUri: 로그인이 끝나면 토큰을 붙여 돌려보낼 프론트 주소
@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String frontendRedirectUri
) {
}
