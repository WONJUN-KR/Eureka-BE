package com.eureka.domain.auth.controller;

import com.eureka.domain.auth.dto.TokenResponse;
import com.eureka.domain.auth.oauth.KakaoOAuthClient;
import com.eureka.domain.auth.oauth.KakaoUser;
import com.eureka.domain.auth.service.KakaoLoginService;
import com.eureka.global.config.KakaoOAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

// 카카오 로그인 - 브라우저가 이동하는 주소라 JSON 대신 302 로 응답
// 1. GET /api/auth/kakao -> 카카오 로그인 화면
// 2. 카카오가 GET /login/oauth2/code/kakao 로 돌려보냄 (콘솔에 등록한 리다이렉트 URI)
// 3. 프론트 주소#accessToken=...&refreshToken=... 로 이동 (실패하면 #error=kakao_login_failed)
@Slf4j
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    static final String STATE_COOKIE = "kakao_oauth_state";
    private static final String CALLBACK_PATH = "/login/oauth2/code/kakao";

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoLoginService kakaoLoginService;
    private final KakaoOAuthProperties properties;

    @Operation(summary = "카카오 로그인 시작", description = "브라우저를 카카오 로그인 화면으로 보냄")
    @GetMapping("/api/auth/kakao")
    public ResponseEntity<Void> start() {
        // 다른 사이트가 만든 로그인 응답을 막기 위해 state 를 쿠키에 두고 돌아올 때 비교
        String state = UUID.randomUUID().toString();
        ResponseCookie cookie = stateCookie(state, Duration.ofMinutes(5));
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .location(URI.create(kakaoOAuthClient.authorizeUrl(state)))
                .build();
    }

    @Operation(summary = "카카오 로그인 콜백", description = "카카오가 돌려보내는 주소 - 직접 호출하지 않음")
    @GetMapping(CALLBACK_PATH)
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @CookieValue(name = STATE_COOKIE, required = false) String savedState) {
        String fragment;
        if (code == null || state == null || !state.equals(savedState)) {
            fragment = "error=kakao_login_failed";
        } else {
            try {
                KakaoUser kakaoUser = kakaoOAuthClient.fetchUser(code);
                TokenResponse tokens = kakaoLoginService.login(kakaoUser);
                fragment = "accessToken=" + tokens.accessToken() + "&refreshToken=" + tokens.refreshToken();
            } catch (RuntimeException e) {
                // 카카오 오류 · 가입 충돌 등 어떤 실패든 브라우저는 프론트로 돌려보냄
                log.warn("카카오 로그인 실패: {}", e.toString());
                fragment = "error=kakao_login_failed";
            }
        }
        // 토큰은 서버 로그 · Referer 에 남지 않게 # 뒤에 붙임
        ResponseCookie clear = stateCookie("", Duration.ZERO);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .location(URI.create(properties.frontendRedirectUri() + "#" + fragment))
                .build();
    }

    // 만들 때와 지울 때 같은 속성을 써야 브라우저가 같은 쿠키로 봄
    private static ResponseCookie stateCookie(String value, Duration maxAge) {
        return ResponseCookie.from(STATE_COOKIE, value)
                .httpOnly(true)
                .path(CALLBACK_PATH)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}
