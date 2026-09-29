package com.jyotimoykashyap;

import com.jyotimoykashyap.ratelimiters.tokenbucket.TokenBucket;
import com.jyotimoykashyap.ratelimiters.tokenbucket.TokenBucketRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketRateLimiterTest {

    @Test
    @DisplayName("Should allow requests up to capacity and reject when tokens are exhausted")
    void shouldAllowRequestsUpToCapacity() {
        // Capacity: 3, Refill: 1 token/sec
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1);

        assertTrue(limiter.allowRequest("client-1"), "Request 1 should be allowed");
        assertTrue(limiter.allowRequest("client-1"), "Request 2 should be allowed");
        assertTrue(limiter.allowRequest("client-1"), "Request 3 should be allowed");

        // 4th request exceeds capacity of 3
        assertFalse(limiter.allowRequest("client-1"), "Request 4 should be rejected");
    }

    @Test
    @DisplayName("Should isolate limits across different clients")
    void shouldIsolateLimitsBetweenDifferentClients() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1);

        // Exhaust client-A
        assertTrue(limiter.allowRequest("client-A"));
        assertTrue(limiter.allowRequest("client-A"));
        assertFalse(limiter.allowRequest("client-A"));

        // client-B should still have their own full bucket
        assertTrue(limiter.allowRequest("client-B"));
        assertTrue(limiter.allowRequest("client-B"));
        assertFalse(limiter.allowRequest("client-B"));
    }

    @Test
    @DisplayName("Should refill tokens over time using integer remainder trick")
    void shouldRefillTokensOverTime() throws InterruptedException {
        // Capacity: 2, Refill: 5 tokens/sec (1 token every 200ms)
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 5);

        // Exhaust bucket
        assertTrue(limiter.allowRequest("client-1"));
        assertTrue(limiter.allowRequest("client-1"));
        assertFalse(limiter.allowRequest("client-1"));

        // Wait ~250ms (enough to generate at least 1 token)
        Thread.sleep(250);

        assertTrue(limiter.allowRequest("client-1"), "Should be allowed after 1 token is refilled");
        assertFalse(limiter.allowRequest("client-1"), "Should reject immediately after consuming the refilled token");
    }

    @Test
    @DisplayName("Should not refill tokens beyond capacity even after long idle time")
    void shouldCapTokensAtCapacity() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(3, 10, System.currentTimeMillis() - 5000); // 5 sec ago

        // Even though 50 tokens could have been generated, bucket must cap at 3
        assertEquals(3, bucket.getCapacity());
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertFalse(bucket.tryConsume(1), "Bucket should have had at most 3 tokens");
    }

    @Test
    @DisplayName("Should reject null or blank client ID safely without throwing exceptions")
    void shouldRejectInvalidClientId() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 5);

        assertFalse(limiter.allowRequest(null));
        assertFalse(limiter.allowRequest(""));
        assertFalse(limiter.allowRequest("   "));
    }

    @Test
    @DisplayName("Should work end-to-end via ApiRateLimiterFactory and ApiRateLimiterService")
    void shouldWorkViaServiceAndFactory() {
        ApiRateLimiter limiter = ApiRateLimiterFactory.createTokenBucketLimiter(2, 1);
        ApiRateLimiterService service = new ApiRateLimiterService(limiter);

        assertTrue(service.allowRequest("user-100"));
        assertTrue(service.allowRequest("user-100"));
        assertFalse(service.allowRequest("user-100"));
    }

    @Test
    @DisplayName("Should remain thread-safe under concurrent requests for the same client")
    void shouldHandleConcurrentRequestsThreadSafely() throws InterruptedException {
        int capacity = 50;
        int numThreads = 100;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1);

        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Ensure all threads burst simultaneously
                    if (limiter.allowRequest("concurrent-client")) {
                        allowedCount.incrementAndGet();
                    } else {
                        rejectedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Fire all threads at the exact same moment
        doneLatch.await();
        executor.shutdown();

        assertEquals(capacity, allowedCount.get(), "Exactly capacity (50) requests must be allowed");
        assertEquals(numThreads - capacity, rejectedCount.get(), "Excess requests must be rejected");
    }
}
