package com.jyotimoykashyap;

import com.jyotimoykashyap.ratelimiters.LeakyBucketRateLimiter;
import com.jyotimoykashyap.ratelimiters.tokenbucket.TokenBucketRateLimiter;

public class ApiRateLimiterFactory {

    public static ApiRateLimiter createTokenBucketLimiter(long capacity, long refillTokensPerSecond) {
        return new TokenBucketRateLimiter(capacity, refillTokensPerSecond);
    }

    public static ApiRateLimiter createLeakyBucketLimiter() {
        return new LeakyBucketRateLimiter();
    }

    public static ApiRateLimiter createSlidingWindowLogLimiter() {
        throw new UnsupportedOperationException("SlidingWindowLogRateLimiter not implemented yet");
    }
}
