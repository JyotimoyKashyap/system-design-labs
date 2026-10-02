package com.jyotimoykashyap;

import com.jyotimoykashyap.cleanup.Cleanable;
import com.jyotimoykashyap.cleanup.RateLimiterCleaner;
import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.dto.Tier;
import com.jyotimoykashyap.policy.Policy;
import com.jyotimoykashyap.ratelimiters.tokenbucket.TokenBucket;
import com.jyotimoykashyap.ratelimiters.tokenbucket.TokenBucketRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterCleanerTest {

    @Test
    @DisplayName("TokenBucket isStale returns true when full and idle past TTL")
    void tokenBucketIsStaleWhenFullAndPastTtl() {
        long fiveMinutesAgo = System.currentTimeMillis() - 300_000;
        TokenBucket bucket = new TokenBucket(10, 1, 1, fiveMinutesAgo);

        // Bucket started full 5 minutes ago and hasn't had requests
        assertTrue(bucket.isStale(60_000), "Bucket idle for 5 min should be stale for 1 min TTL");
        assertFalse(bucket.isStale(600_000), "Bucket idle for 5 min should NOT be stale for 10 min TTL");
    }

    @Test
    @DisplayName("TokenBucket isStale returns false when recently active")
    void tokenBucketIsNotStaleWhenRecentlyUsed() {
        TokenBucket bucket = new TokenBucket(10, 1, 1, System.currentTimeMillis());

        // Just created / active
        assertFalse(bucket.isStale(1000), "Recently initialized bucket should not be stale");
    }

    @Test
    @DisplayName("TokenBucketRateLimiter cleanUpStaleEntries evicts only stale entries")
    void cleanUpStaleEntriesEvictsOnlyStale() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1);
        Policy policy = new Policy(1, 1, 5);

        RequestContext staleClient = new RequestContext("stale-client", Tier.FREE, "/api/test");
        RequestContext activeClient = new RequestContext("active-client", Tier.FREE, "/api/test");

        limiter.allowRequest(policy, staleClient);
        limiter.allowRequest(policy, activeClient);

        assertEquals(2, limiter.getActiveClientCount());
        assertTrue(limiter.containsClient("stale-client"));
        assertTrue(limiter.containsClient("active-client"));

        // With ttlMillis = 1 hour, neither should be evicted
        limiter.cleanUpStaleEntries(3_600_000);
        assertEquals(2, limiter.getActiveClientCount());

        // With a tiny ttlMillis (e.g. 1ms) after sleeping 10ms, full buckets become stale
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Refill active client so it's fresh, but leave stale client idle
        limiter.cleanUpStaleEntries(10);
        // Both buckets started with capacity 5 and consumed 1 token.
        // After 15ms at 1 token/sec (1000ms), they have NOT refilled to full capacity (5).
        // Since tokens < capacity, they are NOT stale!
        assertEquals(2, limiter.getActiveClientCount(), "Buckets that have not refilled to full capacity must NOT be evicted");
    }

    @Test
    @DisplayName("TokenBucketRateLimiter cleanUpStaleEntries evicts bucket after it refills and stays idle past TTL")
    void shouldEvictStaleBucketAfterRefillAndIdle() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 100);
        // limit: 100 tokens/sec (1 token every 10ms), burstCapacity: 2
        Policy policy = new Policy(100, 1, 2);

        RequestContext staleClient = new RequestContext("idle-client", Tier.FREE, "/api/test");
        RequestContext activeClient = new RequestContext("active-client", Tier.FREE, "/api/test");

        limiter.allowRequest(policy, staleClient);
        limiter.allowRequest(policy, activeClient);

        assertEquals(2, limiter.getActiveClientCount());

        // Wait 35ms:
        // idle-client refilled (needs 10ms for 1 token) and reached full capacity at ~10ms.
        // It has now remained idle at full capacity for ~25ms.
        Thread.sleep(35);

        // Keep active-client busy so it was active recently
        limiter.allowRequest(policy, activeClient);

        // Run cleanup with TTL = 15ms.
        // idle-client was full since ~10ms (idle for ~25ms > 15ms TTL) -> EVICTED!
        // active-client just made a request (idle for ~0ms < 15ms TTL) -> RETAINED!
        limiter.cleanUpStaleEntries(15);

        assertEquals(1, limiter.getActiveClientCount(), "Only active client should remain");
        assertFalse(limiter.containsClient("idle-client"), "Stale client should have been evicted");
        assertTrue(limiter.containsClient("active-client"), "Active client must be retained");
    }

    @Test
    @DisplayName("TokenBucketRateLimiter cleanUpStaleEntries rejects non-positive ttlMillis")
    void cleanUpStaleEntriesRejectsInvalidTtl() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 1);
        assertThrows(IllegalArgumentException.class, () -> limiter.cleanUpStaleEntries(0));
        assertThrows(IllegalArgumentException.class, () -> limiter.cleanUpStaleEntries(-100));
    }

    @Test
    @DisplayName("RateLimiterCleaner constructor validates arguments safely")
    void cleanerConstructorValidation() {
        Cleanable cleanable = ttl -> {};

        assertThrows(NullPointerException.class, () -> new RateLimiterCleaner(null, 1000));
        assertThrows(IllegalArgumentException.class, () -> new RateLimiterCleaner(cleanable, 0));
        assertThrows(IllegalArgumentException.class, () -> new RateLimiterCleaner(cleanable, -500));
        assertThrows(IllegalArgumentException.class, () -> new RateLimiterCleaner(cleanable, 0, 1000));

        RateLimiterCleaner cleaner = new RateLimiterCleaner(cleanable, 5000);
        assertEquals(cleanable, cleaner.getCleanable());
        assertEquals(5000, cleaner.getTtlMillis());
    }

    @Test
    @DisplayName("RateLimiterCleaner run delegates to Cleanable with configured ttlMillis")
    void cleanerRunDelegatesToCleanable() {
        AtomicBoolean cleaned = new AtomicBoolean(false);
        long expectedTtl = 60_000;

        Cleanable cleanable = ttl -> {
            if (ttl == expectedTtl) {
                cleaned.set(true);
            }
        };

        RateLimiterCleaner cleaner = new RateLimiterCleaner(cleanable, expectedTtl);
        cleaner.run();

        assertTrue(cleaned.get(), "Cleaner run() must invoke cleanUpStaleEntries with configured ttlMillis");
    }

    @Test
    @DisplayName("RateLimiterCleaner runs successfully on a background ScheduledExecutorService")
    void cleanerWorksWithScheduledExecutor() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Cleanable cleanable = ttl -> latch.countDown();

        RateLimiterCleaner cleaner = new RateLimiterCleaner(cleanable, 10_000);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "test-cleaner");
            t.setDaemon(true);
            return t;
        });

        try {
            scheduler.scheduleWithFixedDelay(cleaner, 0, 20, TimeUnit.MILLISECONDS);
            boolean executedTwice = latch.await(500, TimeUnit.MILLISECONDS);
            assertTrue(executedTwice, "Cleaner should have run at least twice via ScheduledExecutorService");
        } finally {
            scheduler.shutdownNow();
        }
    }
}
