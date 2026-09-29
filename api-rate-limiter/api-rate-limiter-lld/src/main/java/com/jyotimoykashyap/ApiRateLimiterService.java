package com.jyotimoykashyap;

import java.util.Objects;

public class ApiRateLimiterService {

    private final ApiRateLimiter apiRateLimiter;

    public ApiRateLimiterService(ApiRateLimiter apiRateLimiter) {
        this.apiRateLimiter = Objects.requireNonNull(apiRateLimiter, "apiRateLimiter cannot be null");
    }

    public boolean allowRequest(String clientId) {
        return apiRateLimiter.allowRequest(clientId);
    }
}
