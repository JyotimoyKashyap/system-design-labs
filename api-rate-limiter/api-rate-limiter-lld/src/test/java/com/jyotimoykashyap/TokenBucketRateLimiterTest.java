package com.jyotimoykashyap;

import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.dto.Tier;
import com.jyotimoykashyap.policy.DefaultPolicyResolver;
import com.jyotimoykashyap.policy.Policy;
import com.jyotimoykashyap.policy.PolicyResolver;
import com.jyotimoykashyap.ratelimiters.ApiRateLimiter;
import com.jyotimoykashyap.ratelimiters.ApiRateLimiterFactory;
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
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(3, 1);
        Policy policy = new Policy(1, 1, 3); // limit: 1/sec, burstCapacity: 3
        RequestContext context = new RequestContext("client-1", Tier.FREE, "/api/test");

        assertTrue(limiter.allowRequest(policy, context), "Request 1 should be allowed");
        assertTrue(limiter.allowRequest(policy, context), "Request 2 should be allowed");
        assertTrue(limiter.allowRequest(policy, context), "Request 3 should be allowed");

        // 4th request exceeds burst capacity of 3
        assertFalse(limiter.allowRequest(policy, context), "Request 4 should be rejected");
    }

    @Test
    @DisplayName("Should isolate limits across different clients")
    void shouldIsolateLimitsBetweenDifferentClients() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 1);
        Policy policy = new Policy(1, 1, 2);
        RequestContext clientA = new RequestContext("client-A", Tier.FREE, "/api/test");
        RequestContext clientB = new RequestContext("client-B", Tier.FREE, "/api/test");

        // Exhaust client-A
        assertTrue(limiter.allowRequest(policy, clientA));
        assertTrue(limiter.allowRequest(policy, clientA));
        assertFalse(limiter.allowRequest(policy, clientA));

        // client-B should still have their own full bucket
        assertTrue(limiter.allowRequest(policy, clientB));
        assertTrue(limiter.allowRequest(policy, clientB));
        assertFalse(limiter.allowRequest(policy, clientB));
    }

    @Test
    @DisplayName("Should refill tokens over time using integer remainder trick")
    void shouldRefillTokensOverTime() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 5);
        // limit: 5 tokens, timeWindowSeconds: 1, burstCapacity: 2 (1 token every 200ms)
        Policy policy = new Policy(5, 1, 2);
        RequestContext context = new RequestContext("client-1", Tier.FREE, "/api/test");

        // Exhaust bucket
        assertTrue(limiter.allowRequest(policy, context));
        assertTrue(limiter.allowRequest(policy, context));
        assertFalse(limiter.allowRequest(policy, context));

        // Wait ~250ms (enough to generate at least 1 token)
        Thread.sleep(250);

        assertTrue(limiter.allowRequest(policy, context), "Should be allowed after 1 token is refilled");
        assertFalse(limiter.allowRequest(policy, context), "Should reject immediately after consuming the refilled token");
    }

    @Test
    @DisplayName("Should not refill tokens beyond capacity even after long idle time")
    void shouldCapTokensAtCapacity() throws InterruptedException {
        TokenBucket bucket = new TokenBucket(3, 10, 1, System.currentTimeMillis() - 5000); // 10 tokens/sec, 5 sec ago

        // Even though 50 tokens could have been generated, bucket must cap at 3
        assertEquals(3, bucket.getCapacity());
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertFalse(bucket.tryConsume(1), "Bucket should have had at most 3 tokens");
    }

    @Test
    @DisplayName("Should reject invalid RequestContext arguments safely")
    void shouldRejectInvalidRequestContext() {
        assertThrows(IllegalArgumentException.class, () -> new RequestContext(null, Tier.FREE, "/api/test"));
        assertThrows(IllegalArgumentException.class, () -> new RequestContext("", Tier.FREE, "/api/test"));
        assertThrows(IllegalArgumentException.class, () -> new RequestContext("   ", Tier.FREE, "/api/test"));
        assertThrows(NullPointerException.class, () -> new RequestContext("client-1", null, "/api/test"));
        assertThrows(IllegalArgumentException.class, () -> new RequestContext("client-1", Tier.FREE, ""));
    }

    @Test
    @DisplayName("Should work end-to-end via ApiRateLimiterService and DefaultPolicyResolver")
    void shouldWorkViaServiceAndFactory() {
        ApiRateLimiter limiter = ApiRateLimiterFactory.createTokenBucketLimiter(2, 1);
        PolicyResolver resolver = new DefaultPolicyResolver();
        ApiRateLimiterService service = new ApiRateLimiterService(limiter, resolver);

        // FREE tier policy in DefaultPolicyResolver has burstCapacity: 10
        RequestContext context = new RequestContext("user-100", Tier.FREE, "/api/test");

        for (int i = 0; i < 10; i++) {
            assertTrue(service.allowRequest(context), "Request " + (i + 1) + " should be allowed");
        }
        assertFalse(service.allowRequest(context), "11th request should be rejected");
    }

    @Test
    @DisplayName("Should remain thread-safe under concurrent requests for the same client")
    void shouldHandleConcurrentRequestsThreadSafely() throws InterruptedException {
        int capacity = 50;
        int numThreads = 100;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(capacity, 1);
        Policy policy = new Policy(1, 1, capacity);
        RequestContext context = new RequestContext("concurrent-client", Tier.FREE, "/api/test");

        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Ensure all threads burst simultaneously
                    if (limiter.allowRequest(policy, context)) {
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
