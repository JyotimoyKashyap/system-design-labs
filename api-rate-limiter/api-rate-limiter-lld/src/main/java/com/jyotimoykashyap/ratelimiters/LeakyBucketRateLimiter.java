package com.jyotimoykashyap.ratelimiters;

import com.jyotimoykashyap.ApiRateLimiter;

public class LeakyBucketRateLimiter implements ApiRateLimiter {
    @Override
    public boolean allowRequest(String clientId) {
        return false;
    }
}
