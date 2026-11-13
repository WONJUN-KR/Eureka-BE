package com.eureka.domain.auth;

import com.eureka.domain.auth.oauth.KakaoOAuthClient;
import com.eureka.domain.auth.oauth.KakaoUser;
import com.eureka.domain.user.entity.AuthProvider;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 동시 첫 로그인 - 중복 확인 뒤 다른 요청이 먼저 저장한 상황을 실제 DB 유니크 위반으로 만들고,
// 실패한 트랜잭션 뒤 새 트랜잭션에서 복구되는지 봄 (테스트 트랜잭션 없이 실제 커밋)
@SpringBootTest
@AutoConfigureMockMvc
class KakaoSignupConflictTest {

    private static final String FRONT = "http://localhost:3000/auth/kakao/callback";
    private static final String NONCE = "tab-nonce-1234567890";

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager em;
    @MockitoSpyBean private UserRepository userRepository;
    @MockitoBean private KakaoOAuthClient kakaoOAuthClient;

    // 스파이는 인터페이스라 진짜 메서드를 부를 수 없어서, 두 번째 호출부터 넘길 진짜 레포지토리
    private UserRepository realRepository() {
        return new JpaRepositoryFactory(em).getRepository(UserRepository.class);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    private String callback(String kakaoCode) throws Exception {
        return mockMvc.perform(get("/login/oauth2/code/kakao").param("code", kakaoCode).param("state", "s")
                        .cookie(new Cookie("kakao_oauth_state", "s." + NONCE)))
                .andExpect(status().isFound()).andReturn().getResponse().getHeader("Location");
    }

    @Test
    void nicknameTakenAfterCheckGetsAnotherNickname() throws Exception {
        userRepository.save(User.builder().email("first@test.local").password("x").nickname("동시가입").name("테스트").build());
        // 첫 시도의 중복 확인은 저장 전에 본 것처럼 '없음' -> 저장에서 닉네임 유니크 위반
        doReturn(false).doAnswer(delegatesTo(realRepository())).when(userRepository).existsByNickname("동시가입");
        given(kakaoOAuthClient.fetchUser("code-1")).willReturn(new KakaoUser("1001", "동시가입"));

        assertThat(callback("code-1")).startsWith(FRONT + "#code=");

        User user = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "1001").orElseThrow();
        assertThat(user.getNickname()).matches("동시가입\\d{4}");
    }

    @Test
    void sameKakaoAccountSavedAfterLookupLogsIntoThatAccount() throws Exception {
        given(kakaoOAuthClient.fetchUser("code-2")).willReturn(new KakaoUser("1002", "같은계정"));
        callback("code-2");
        Long existingId = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "1002").orElseThrow().getId();
        // 첫 시도의 조회는 다른 요청이 저장하기 전에 본 것처럼 '없음' -> 저장에서 (provider, provider_id) 유니크 위반
        doReturn(Optional.empty()).doAnswer(delegatesTo(realRepository()))
                .when(userRepository).findByProviderAndProviderId(AuthProvider.KAKAO, "1002");

        assertThat(callback("code-2")).startsWith(FRONT + "#code=");

        assertThat(userRepository.findAll().stream()
                .filter(u -> u.getProvider() == AuthProvider.KAKAO)).hasSize(1)
                .allMatch(u -> u.getId().equals(existingId));
    }
}
