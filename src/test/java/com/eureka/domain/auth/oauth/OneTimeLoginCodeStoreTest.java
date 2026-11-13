package com.eureka.domain.auth.oauth;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

// 1회용 로그인 코드 - nonce 가 맞아야 하고, 한 번만, 1분이 지나면 교환 안 됨
class OneTimeLoginCodeStoreTest {

    private static final String NONCE = "nonce-aaaaaaaaaaaa";

    @Test
    void codeIsRedeemedOnceWithTheSameNonce() {
        OneTimeLoginCodeStore store = new OneTimeLoginCodeStore();
        String code = store.issue(7L, NONCE);

        assertThat(store.redeem(code, NONCE)).contains(7L);
        assertThat(store.redeem(code, NONCE)).isEmpty();
    }

    @Test
    void wrongNonceBurnsTheCode() {
        OneTimeLoginCodeStore store = new OneTimeLoginCodeStore();
        String code = store.issue(7L, NONCE);

        assertThat(store.redeem(code, "other-nonce-bbbbbbbb")).isEmpty();
        assertThat(store.redeem(code, NONCE)).isEmpty();
    }

    @Test
    void codeExpiresAfterOneMinute() {
        AtomicLong nanos = new AtomicLong();
        OneTimeLoginCodeStore store = new OneTimeLoginCodeStore(nanos::get);
        String code = store.issue(7L, NONCE);

        nanos.addAndGet(OneTimeLoginCodeStore.TTL.toNanos());

        assertThat(store.redeem(code, NONCE)).isEmpty();
    }
}
