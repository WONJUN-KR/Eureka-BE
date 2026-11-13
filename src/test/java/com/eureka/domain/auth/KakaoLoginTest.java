package com.eureka.domain.auth;

import com.eureka.domain.auth.exception.AuthException;
import com.eureka.domain.auth.oauth.KakaoOAuthClient;
import com.eureka.domain.auth.oauth.KakaoUser;
import com.eureka.domain.user.entity.AuthProvider;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 카카오 로그인 - 카카오 호출은 가짜로 바꾸고, state 확인 · 첫 로그인 가입 · 다시 로그인 · 실패 처리를 봄
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class KakaoLoginTest {

    private static final String FRONT = "http://localhost:3000/auth/kakao/callback";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @MockitoBean private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        given(kakaoOAuthClient.authorizeUrl(anyString()))
                .willAnswer(inv -> "https://kauth.kakao.com/oauth/authorize?state=" + inv.getArgument(0));
    }

    private MvcResult callback(String code, String state, String cookieState) throws Exception {
        var request = get("/login/oauth2/code/kakao").param("code", code).param("state", state);
        if (cookieState != null) {
            request.cookie(new Cookie("kakao_oauth_state", cookieState));
        }
        return mockMvc.perform(request).andExpect(status().isFound()).andReturn();
    }

    private String location(MvcResult result) {
        return result.getResponse().getHeader("Location");
    }

    @Test
    void startSendsBrowserToKakaoWithStateCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/kakao"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", startsWith("https://kauth.kakao.com/oauth/authorize")))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("kakao_oauth_state");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(location(result)).endsWith("state=" + cookie.getValue());
    }

    @Test
    void firstLoginCreatesUserAndSecondLoginReusesIt() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-1")).willReturn(new KakaoUser("12345", "바다여행"));

        String first = location(callback("code-1", "s1", "s1"));
        assertThat(first).startsWith(FRONT + "#accessToken=").contains("&refreshToken=");

        User user = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "12345").orElseThrow();
        assertThat(user.getNickname()).isEqualTo("바다여행");

        String second = location(callback("code-1", "s2", "s2"));
        assertThat(second).startsWith(FRONT + "#accessToken=");
        assertThat(userRepository.findAll().stream()
                .filter(u -> u.getProvider() == AuthProvider.KAKAO)).hasSize(1);
    }

    @Test
    void takenNicknameGetsNumberSuffix() throws Exception {
        userRepository.save(User.builder().email("x@test.local").password("x").nickname("바다여행").name("테스트").build());
        given(kakaoOAuthClient.fetchUser("code-2")).willReturn(new KakaoUser("777", "바다여행"));

        callback("code-2", "s", "s");

        String nickname = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "777").orElseThrow().getNickname();
        assertThat(nickname).matches("바다여행\\d{4}");
    }

    @Test
    void missingNicknameFallsBackToDefault() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-3")).willReturn(new KakaoUser("888", null));

        callback("code-3", "s", "s");

        assertThat(userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "888").orElseThrow().getNickname())
                .startsWith("여행자");
    }

    @Test
    void wrongStateIsRejectedWithoutCallingKakao() throws Exception {
        assertThat(location(callback("code-4", "attacker", "mine"))).isEqualTo(FRONT + "#error=kakao_login_failed");
        assertThat(location(callback("code-4", "s", null))).isEqualTo(FRONT + "#error=kakao_login_failed");
        verify(kakaoOAuthClient, never()).fetchUser(anyString());
    }

    @Test
    void sameKakaoAccountCanJoinAgainAfterLeaving() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-5")).willReturn(new KakaoUser("555", "산책러"));
        callback("code-5", "s", "s");
        User first = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "555").orElseThrow();
        first.delete();
        userRepository.flush();

        assertThat(location(callback("code-5", "s", "s"))).startsWith(FRONT + "#accessToken=");
        User second = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "555").orElseThrow();
        assertThat(second.getId()).isNotEqualTo(first.getId());
    }

    @Test
    void kakaoEmailCannotBeTakenBySignup() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"kakao_999@kakao.eureka.local\",\"password\":\"password1\",\"nickname\":\"선점\",\"name\":\"테스트\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void unexpectedErrorStillGoesBackToFront() throws Exception {
        given(kakaoOAuthClient.fetchUser("boom")).willThrow(new IllegalStateException("boom"));

        assertThat(location(callback("boom", "s", "s"))).isEqualTo(FRONT + "#error=kakao_login_failed");
    }

    @Test
    void kakaoFailureGoesBackWithError() throws Exception {
        given(kakaoOAuthClient.fetchUser("bad")).willThrow(AuthException.kakaoLoginFailed());

        assertThat(location(callback("bad", "s", "s"))).isEqualTo(FRONT + "#error=kakao_login_failed");
    }
}
