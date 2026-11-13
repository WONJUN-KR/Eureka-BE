package com.eureka.domain.auth.oauth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

// 카카오 로그인 콜백이 토큰 대신 프론트에 넘기는 1회용 코드 - 1분 안에 한 번만 교환
// 로그인을 시작한 브라우저의 nonce 에 묶여 있어서, 남이 만든 콜백 링크로는 교환할 수 없음
// 토큰은 들고 있지 않고 회원 id 만 - 토큰은 교환에 성공했을 때 발급
@Component
public class OneTimeLoginCodeStore {

    static final Duration TTL = Duration.ofMinutes(1);

    private final Cache<String, Entry> entries;
    private final SecureRandom random = new SecureRandom();

    public OneTimeLoginCodeStore() {
        this(Ticker.systemTicker());
    }

    OneTimeLoginCodeStore(Ticker ticker) {
        this.entries = Caffeine.newBuilder().expireAfterWrite(TTL).ticker(ticker).build();
    }

    public String issue(Long userId, String nonce) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        entries.put(code, new Entry(userId, nonce));
        return code;
    }

    // 코드는 nonce 가 맞든 틀리든 한 번 꺼내면 사라짐 (남의 코드로 nonce 를 맞혀 보는 시도를 막음)
    public Optional<Long> redeem(String code, String nonce) {
        Entry entry = entries.asMap().remove(code);
        if (entry == null) {
            return Optional.empty();
        }
        boolean nonceMatches = MessageDigest.isEqual(
                entry.nonce().getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8));
        return nonceMatches ? Optional.of(entry.userId()) : Optional.empty();
    }

    private record Entry(Long userId, String nonce) {
    }
}
