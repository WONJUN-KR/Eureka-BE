package com.eureka.domain.auth.service;

import com.eureka.domain.auth.dto.TokenResponse;
import com.eureka.domain.auth.oauth.KakaoUser;
import com.eureka.domain.user.entity.AuthProvider;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.exception.UserException;
import com.eureka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {

    private static final int NICKNAME_MAX = 20;
    private static final int SUFFIX_LENGTH = 4;
    private static final String DEFAULT_NICKNAME = "여행자";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final SecureRandom random = new SecureRandom();

    // 카카오에서 받은 회원을 찾고, 처음이면 가입 - 회원 id 만 돌려줌
    // 카카오 호출은 컨트롤러에서 먼저 끝내서, 여기 트랜잭션은 DB 작업만 붙잡음
    @Transactional
    public Long findOrSignup(KakaoUser kakaoUser) {
        return userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, kakaoUser.id())
                .orElseGet(() -> signup(kakaoUser))
                .getId();
    }

    // 1회용 코드 교환에 성공했을 때만 토큰 발급 - 교환이 안 되면 기존 refresh token 을 덮어쓰지 않음
    @Transactional
    public TokenResponse issueTokens(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserException::notFound);
        return authService.issueTokens(user);
    }

    private User signup(KakaoUser kakaoUser) {
        String nickname = uniqueNickname(kakaoUser.nickname());
        return userRepository.save(User.builder()
                // 이메일은 동의받지 않으므로 회원번호로 만든 내부용 주소 - 이메일 로그인에는 못 씀
                .email("kakao_" + kakaoUser.id() + User.KAKAO_EMAIL_DOMAIN)
                // 비밀번호 로그인을 막기 위해 아무도 모르는 값
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(nickname)
                .name(nickname)
                .emailVerified(false)
                .provider(AuthProvider.KAKAO)
                .providerId(kakaoUser.id())
                .build());
    }

    // 카카오 닉네임을 기본값으로 쓰고, 겹치면 뒤에 숫자 4자리를 붙임 (글자 수는 이모지도 한 글자로 셈)
    private String uniqueNickname(String kakaoNickname) {
        String base = kakaoNickname == null ? "" : kakaoNickname.strip();
        if (base.codePointCount(0, base.length()) < 2) {
            base = DEFAULT_NICKNAME;
        }
        base = firstCodePoints(base, NICKNAME_MAX - SUFFIX_LENGTH);
        if (!userRepository.existsByNickname(base)) {
            return base;
        }
        for (int i = 0; i < 10; i++) {
            String candidate = base + String.format("%04d", random.nextInt(10_000));
            if (!userRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        return firstCodePoints(base, 4) + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String firstCodePoints(String text, int count) {
        int total = text.codePointCount(0, text.length());
        return total <= count ? text : text.substring(0, text.offsetByCodePoints(0, count));
    }
}
