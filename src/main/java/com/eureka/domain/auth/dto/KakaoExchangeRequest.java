package com.eureka.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 카카오 로그인 콜백에서 받은 1회용 코드 + 로그인을 시작할 때 프론트가 만든 nonce
public record KakaoExchangeRequest(
        @NotBlank
        String code,

        @NotBlank
        String nonce
) {
}
