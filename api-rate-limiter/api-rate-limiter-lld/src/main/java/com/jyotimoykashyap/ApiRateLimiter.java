package com.jyotimoykashyap;

public interface ApiRateLimiter {
    boolean allowRequest(String clientId);
}
