package com.jyotimoykashyap.ratelimiters.tokenbucket;

import com.jyotimoykashyap.dto.RequestContext;
import com.jyotimoykashyap.policy.Policy;
import com.jyotimoykashyap.ratelimiters.ApiRateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements ApiRateLimiter {

    private final Map<String, TokenBucket> map;
    private final long capacity;
    private final long refillTokensPerSecond;

    public TokenBucketRateLimiter(long capacity, long refillTokensPerSecond) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }
        if (refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException("Refill rate must be > 0");
        }

        this.map = new ConcurrentHashMap<>();
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

    @Override
    public boolean allowRequest(Policy policy, RequestContext requestContext) {
        if (requestContext.clientId() == null || requestContext.clientId().isBlank()) {
            return false;
        }

        TokenBucket bucket = map.computeIfAbsent(requestContext.clientId(), k ->
            new TokenBucket(
                    policy.burstCapacity(),
                    policy.limit(),
                    policy.timeWindowSeconds(),
                    System.currentTimeMillis()
            )
        );

        return bucket.tryConsume(1);
    }

    public long getCapacity() {
        return capacity;
    }

    public long getRefillTokensPerSecond() {
        return refillTokensPerSecond;
    }
}
