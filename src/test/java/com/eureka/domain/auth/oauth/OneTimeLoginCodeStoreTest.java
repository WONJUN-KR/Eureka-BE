package com.eureka.domain.auth.oauth;

import com.eureka.domain.auth.dto.TokenResponse;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

// 1회용 로그인 코드 - 1분이 지나면 교환 안 됨
class OneTimeLoginCodeStoreTest {

    private static final TokenResponse TOKENS = TokenResponse.of("access", "refresh");

    @Test
    void codeExpiresAfterOneMinute() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-13T00:00:00Z"));
        OneTimeLoginCodeStore store = new OneTimeLoginCodeStore(clock);

        String fresh = store.issue(TOKENS, "nonce-aaaaaaaaaaaa");
        String stale = store.issue(TOKENS, "nonce-aaaaaaaaaaaa");

        assertThat(store.redeem(fresh, "nonce-aaaaaaaaaaaa")).contains(TOKENS);
        clock.advance(OneTimeLoginCodeStore.TTL.plusSeconds(1));
        assertThat(store.redeem(stale, "nonce-aaaaaaaaaaaa")).isEmpty();
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}
