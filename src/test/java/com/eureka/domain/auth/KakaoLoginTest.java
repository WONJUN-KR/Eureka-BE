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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 카카오 로그인 - 카카오 호출은 가짜로 바꾸고, state · nonce 확인, 1회용 코드 교환, 첫 로그인 가입, 실패 처리를 봄
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class KakaoLoginTest {

    private static final String FRONT = "http://localhost:3000/auth/kakao/callback";
    private static final String ERROR = FRONT + "#error=kakao_login_failed";
    private static final String NONCE = "tab-nonce-1234567890";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @MockitoBean private KakaoOAuthClient kakaoOAuthClient;

    @BeforeEach
    void setUp() {
        given(kakaoOAuthClient.authorizeUrl(anyString()))
                .willAnswer(inv -> "https://kauth.kakao.com/oauth/authorize?state=" + inv.getArgument(0));
    }

    // 쿠키에는 'state.nonce' 가 들어 있음
    private String callback(String code, String state, String cookieValue) throws Exception {
        var request = get("/login/oauth2/code/kakao").param("code", code).param("state", state);
        if (cookieValue != null) {
            request.cookie(new Cookie("kakao_oauth_state", cookieValue));
        }
        return mockMvc.perform(request).andExpect(status().isFound()).andReturn()
                .getResponse().getHeader("Location");
    }

    private String loginCode(String kakaoCode) throws Exception {
        String location = callback(kakaoCode, "s", "s." + NONCE);
        assertThat(location).startsWith(FRONT + "#code=");
        return location.substring((FRONT + "#code=").length());
    }

    private ResultActions exchange(String code, String nonce) throws Exception {
        return mockMvc.perform(post("/api/auth/kakao/exchange")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"nonce\":\"" + nonce + "\"}"));
    }

    @Test
    void startSendsBrowserToKakaoWithStateAndNonceCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/kakao").param("nonce", NONCE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", startsWith("https://kauth.kakao.com/oauth/authorize")))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("kakao_oauth_state");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getValue()).endsWith("." + NONCE);
        String state = cookie.getValue().substring(0, cookie.getValue().indexOf('.'));
        assertThat(result.getResponse().getHeader("Location")).endsWith("state=" + state);
    }

    @Test
    void startWithoutValidNonceGoesBackWithError() throws Exception {
        mockMvc.perform(get("/api/auth/kakao")).andExpect(header().string("Location", ERROR));
        mockMvc.perform(get("/api/auth/kakao").param("nonce", "short")).andExpect(header().string("Location", ERROR));
        verify(kakaoOAuthClient, never()).authorizeUrl(anyString());
    }

    @Test
    void callbackGivesOneTimeCodeNotTokens() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-1")).willReturn(new KakaoUser("12345", "바다여행"));

        String location = callback("code-1", "s", "s." + NONCE);

        assertThat(location).startsWith(FRONT + "#code=").doesNotContain("accessToken").doesNotContain("refreshToken");
    }

    @Test
    void codeIsExchangedOnceWithTheSameNonce() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-1")).willReturn(new KakaoUser("12345", "바다여행"));
        String code = loginCode("code-1");

        exchange(code, NONCE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()));
        exchange(code, NONCE).andExpect(status().isUnauthorized());
    }

    @Test
    void someoneElsesLinkCannotBeExchanged() throws Exception {
        // 공격자가 자기 브라우저에서 받은 코드를 링크로 보내도, 피해자 탭의 nonce 와 달라서 실패하고 코드는 사라짐
        given(kakaoOAuthClient.fetchUser("code-1")).willReturn(new KakaoUser("12345", "바다여행"));
        String attackerCode = loginCode("code-1");

        exchange(attackerCode, "victim-tab-nonce-0000").andExpect(status().isUnauthorized());
        exchange(attackerCode, NONCE).andExpect(status().isUnauthorized());
    }

    @Test
    void firstLoginCreatesUserAndSecondLoginReusesIt() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-1")).willReturn(new KakaoUser("12345", "바다여행"));

        loginCode("code-1");
        User user = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "12345").orElseThrow();
        assertThat(user.getNickname()).isEqualTo("바다여행");

        loginCode("code-1");
        assertThat(userRepository.findAll().stream()
                .filter(u -> u.getProvider() == AuthProvider.KAKAO)).hasSize(1);
    }

    @Test
    void takenNicknameGetsNumberSuffix() throws Exception {
        userRepository.save(User.builder().email("x@test.local").password("x").nickname("바다여행").name("테스트").build());
        given(kakaoOAuthClient.fetchUser("code-2")).willReturn(new KakaoUser("777", "바다여행"));

        loginCode("code-2");

        String nickname = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "777").orElseThrow().getNickname();
        assertThat(nickname).matches("바다여행\\d{4}");
    }

    @Test
    void missingNicknameFallsBackToDefault() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-3")).willReturn(new KakaoUser("888", null));

        loginCode("code-3");

        assertThat(userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "888").orElseThrow().getNickname())
                .startsWith("여행자");
    }

    @Test
    void wrongStateIsRejectedWithoutCallingKakao() throws Exception {
        assertThat(callback("code-4", "attacker", "mine." + NONCE)).isEqualTo(ERROR);
        assertThat(callback("code-4", "s", null)).isEqualTo(ERROR);
        assertThat(callback("code-4", "s", "s")).isEqualTo(ERROR);
        verify(kakaoOAuthClient, never()).fetchUser(anyString());
    }

    @Test
    void sameKakaoAccountCanJoinAgainAfterLeaving() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-5")).willReturn(new KakaoUser("555", "산책러"));
        loginCode("code-5");
        User first = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "555").orElseThrow();
        first.delete();
        userRepository.flush();

        loginCode("code-5");
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

        assertThat(callback("boom", "s", "s." + NONCE)).isEqualTo(ERROR);
    }

    @Test
    void kakaoFailureGoesBackWithError() throws Exception {
        given(kakaoOAuthClient.fetchUser("bad")).willThrow(AuthException.kakaoLoginFailed());

        assertThat(callback("bad", "s", "s." + NONCE)).isEqualTo(ERROR);
    }
}
