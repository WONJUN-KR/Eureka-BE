package com.eureka.domain.auth.oauth;

import com.eureka.domain.auth.exception.AuthException;
import com.eureka.global.config.KakaoOAuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

// 카카오 OAuth 호출 - 인가 주소 만들기, 인가 코드 -> 토큰, 토큰 -> 회원 정보
@Component
public class KakaoOAuthClient {

    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_URL = "https://kapi.kakao.com/v2/user/me";

    private final KakaoOAuthProperties properties;
    private final RestClient restClient;

    public KakaoOAuthClient(KakaoOAuthProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        // 카카오가 응답하지 않을 때 요청이 계속 매달려 있지 않게 시간 제한
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    public String authorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .encode()
                .toUriString();
    }

    public KakaoUser fetchUser(String code) {
        try {
            String accessToken = requestAccessToken(code);
            JsonNode body = restClient.get()
                    .uri(USER_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || !body.hasNonNull("id")) {
                throw AuthException.kakaoLoginFailed();
            }
            String nickname = body.path("kakao_account").path("profile").path("nickname").asText(null);
            return new KakaoUser(body.get("id").asText(), nickname);
        } catch (RestClientException e) {
            throw AuthException.kakaoLoginFailed();
        }
    }

    private String requestAccessToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("redirect_uri", properties.redirectUri());
        form.add("code", code);

        JsonNode body = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
        if (body == null || !body.hasNonNull("access_token")) {
            throw AuthException.kakaoLoginFailed();
        }
        return body.get("access_token").asText();
    }
}
