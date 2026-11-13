package com.eureka.domain.auth.oauth;

import com.eureka.domain.auth.dto.TokenResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// 카카오 로그인 콜백이 토큰 대신 프론트에 넘기는 1회용 코드 - 1분 안에 한 번만 교환
// 로그인을 시작한 브라우저의 nonce 에 묶여 있어서, 남이 만든 콜백 링크로는 교환할 수 없음
@Component
public class OneTimeLoginCodeStore {

    static final Duration TTL = Duration.ofMinutes(1);

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    public OneTimeLoginCodeStore() {
        this(Clock.systemUTC());
    }

    OneTimeLoginCodeStore(Clock clock) {
        this.clock = clock;
    }

    public String issue(TokenResponse tokens, String nonce) {
        purgeExpired();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        entries.put(code, new Entry(tokens, nonce, clock.instant().plus(TTL)));
        return code;
    }

    // 코드는 nonce 가 맞든 틀리든 한 번 꺼내면 사라짐
    public Optional<TokenResponse> redeem(String code, String nonce) {
        if (code == null || nonce == null) {
            return Optional.empty();
        }
        Entry entry = entries.remove(code);
        if (entry == null || entry.expiresAt().isBefore(clock.instant())) {
            return Optional.empty();
        }
        boolean nonceMatches = MessageDigest.isEqual(
                entry.nonce().getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8));
        return nonceMatches ? Optional.of(entry.tokens()) : Optional.empty();
    }

    private void purgeExpired() {
        Instant now = clock.instant();
        entries.values().removeIf(e -> e.expiresAt().isBefore(now));
    }

    private record Entry(TokenResponse tokens, String nonce, Instant expiresAt) {
    }
}
