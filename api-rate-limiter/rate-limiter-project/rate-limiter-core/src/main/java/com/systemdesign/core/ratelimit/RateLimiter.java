package com.systemdesign.core.ratelimit;

public interface RateLimiter {
    /**
     * Checks if a request for a specific key (e.g., IP address) is allowed.
     * 
     * @param key The unique identifier for the client
     * @param maxRequests The maximum number of requests allowed in the window
     * @param windowSizeSeconds The size of the time window in seconds
     * @return true if allowed, false if rate limited
     */
    boolean isAllowed(String key, int maxRequests, int windowSizeSeconds);
}
