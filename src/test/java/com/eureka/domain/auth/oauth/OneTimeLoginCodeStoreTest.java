package com.eureka.domain.auth.oauth;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
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
    void concurrentRedeemSucceedsOnlyOnce() throws Exception {
        OneTimeLoginCodeStore store = new OneTimeLoginCodeStore();
        for (int round = 0; round < 50; round++) {
            String code = store.issue(7L, NONCE);
            CyclicBarrier barrier = new CyclicBarrier(2);
            CompletableFuture<Optional<Long>> first = CompletableFuture.supplyAsync(() -> redeemTogether(store, code, barrier));
            CompletableFuture<Optional<Long>> second = CompletableFuture.supplyAsync(() -> redeemTogether(store, code, barrier));

            assertThat(first.get().isPresent() ^ second.get().isPresent()).isTrue();
            assertThat(first.get().or(second::join)).contains(7L);
        }
    }

    private static Optional<Long> redeemTogether(OneTimeLoginCodeStore store, String code, CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return store.redeem(code, NONCE);
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
