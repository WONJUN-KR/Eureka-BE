package com.eureka.domain.auth.controller;

import com.eureka.domain.auth.dto.KakaoExchangeRequest;
import com.eureka.domain.auth.dto.TokenResponse;
import com.eureka.domain.auth.exception.AuthException;
import com.eureka.domain.auth.oauth.KakaoOAuthClient;
import com.eureka.domain.auth.oauth.KakaoUser;
import com.eureka.domain.auth.oauth.OneTimeLoginCodeStore;
import com.eureka.domain.auth.service.KakaoLoginService;
import com.eureka.global.common.ApiResponse;
import com.eureka.global.config.KakaoOAuthProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

// 카카오 로그인
// 1. GET /api/auth/kakao?nonce=... -> 카카오 로그인 화면 (nonce 는 프론트가 만들어 그 탭에만 보관)
// 2. 카카오가 GET /login/oauth2/code/kakao 로 돌려보냄 (콘솔에 등록한 리다이렉트 URI)
// 3. 프론트 주소#code=... 로 이동 (실패하면 #error=kakao_login_failed) - 토큰은 주소에 싣지 않음
// 4. 프론트가 POST /api/auth/kakao/exchange {code, nonce} 로 토큰을 받음
// 남이 만든 콜백 링크는 그 사람의 nonce 에 묶여 있어서 교환되지 않음 (로그인 CSRF 방지)
@Slf4j
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    static final String STATE_COOKIE = "kakao_oauth_state";
    private static final String CALLBACK_PATH = "/login/oauth2/code/kakao";
    private static final Pattern NONCE = Pattern.compile("[A-Za-z0-9_-]{16,128}");
    private static final String ERROR_FRAGMENT = "error=kakao_login_failed";

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoLoginService kakaoLoginService;
    private final KakaoOAuthProperties properties;
    private final OneTimeLoginCodeStore loginCodeStore;

    @Operation(summary = "카카오 로그인 시작", description = "브라우저를 카카오 로그인 화면으로 보냄. nonce 는 프론트가 만든 16~128자 [A-Za-z0-9_-]")
    @GetMapping("/api/auth/kakao")
    public ResponseEntity<Void> start(@RequestParam(required = false) String nonce) {
        if (nonce == null || !NONCE.matcher(nonce).matches()) {
            // 이전 시도에서 남은 state 쿠키도 같이 지움
            return redirectToFront(ERROR_FRAGMENT, stateCookie("", Duration.ZERO));
        }
        // 다른 사이트가 만든 카카오 응답을 막기 위해 state 를, 교환 때 쓰려고 nonce 를 쿠키에 두고 돌아올 때 꺼냄
        String state = UUID.randomUUID().toString();
        ResponseCookie cookie = stateCookie(state + "." + nonce, Duration.ofMinutes(5));
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
            @CookieValue(name = STATE_COOKIE, required = false) String saved) {
        String fragment = ERROR_FRAGMENT;
        String[] parts = saved == null ? new String[0] : saved.split("\\.", 2);
        if (code != null && state != null && parts.length == 2 && state.equals(parts[0])) {
            try {
                KakaoUser kakaoUser = kakaoOAuthClient.fetchUser(code);
                Long userId = kakaoLoginService.findOrSignup(kakaoUser);
                fragment = "code=" + loginCodeStore.issue(userId, parts[1]);
            } catch (RuntimeException e) {
                // 카카오 오류 · 가입 충돌 등 어떤 실패든 브라우저는 프론트로 돌려보냄
                log.warn("카카오 로그인 실패: {}", e.toString());
            }
        }
        return redirectToFront(fragment, stateCookie("", Duration.ZERO));
    }

    @Operation(summary = "카카오 로그인 코드 교환", description = "콜백에서 받은 1회용 코드와 로그인을 시작한 탭의 nonce 로 토큰 발급. 코드는 1분 안에 한 번만")
    @PostMapping("/api/auth/kakao/exchange")
    public ResponseEntity<ApiResponse<TokenResponse>> exchange(@Valid @RequestBody KakaoExchangeRequest request) {
        Long userId = loginCodeStore.redeem(request.code(), request.nonce())
                .orElseThrow(AuthException::invalidToken);
        return ResponseEntity.ok(ApiResponse.success(kakaoLoginService.issueTokens(userId)));
    }

    // 코드 · 오류는 서버 로그 · Referer 에 남지 않게 # 뒤에 붙임
    private ResponseEntity<Void> redirectToFront(String fragment, ResponseCookie cookie) {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(properties.frontendRedirectUri() + "#" + fragment));
        if (cookie != null) {
            response.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return response.build();
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
