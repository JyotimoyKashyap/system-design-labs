package com.jyotimoykashyap.ratelimiters.tokenbucket;

public class TokenBucket {
    private final long capacity;
    private final long limit;
    private final long timeWindowSeconds;
    private long tokens;
    private long lastRefillTimestamp;

    public TokenBucket(long capacity, long limit, long timeWindowSeconds, long lastRefillTimestamp) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be > 0");
        }
        if (timeWindowSeconds <= 0) {
            throw new IllegalArgumentException("timeWindowSeconds must be > 0");
        }
        this.capacity = capacity;
        this.limit = limit;
        this.timeWindowSeconds = timeWindowSeconds;
        this.tokens = capacity; // Bucket starts full to allow initial legitimate burst
        this.lastRefillTimestamp = lastRefillTimestamp;
    }

    public TokenBucket(long capacity, long limit, long timeWindowSeconds) {
        this(capacity, limit, timeWindowSeconds, System.currentTimeMillis());
    }

    public synchronized boolean tryConsume(int tokensRequested) {
        if (tokensRequested <= 0) {
            throw new IllegalArgumentException("tokensRequested must be > 0");
        }

        refill();

        if (tokens >= tokensRequested) {
            tokens -= tokensRequested;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedMs = now - lastRefillTimestamp;

        if (elapsedMs <= 0) {
            return;
        }

        long timeWindowMs = timeWindowSeconds * 1000;

        // Calculate whole tokens earned in elapsed time
        long tokensToAdd = (elapsedMs * limit) / timeWindowMs;

        if (tokensToAdd > 0) {
            long newTokens = tokens + tokensToAdd;
            if (newTokens >= capacity) {
                tokens = capacity;
                lastRefillTimestamp = now; // Bucket full; discard overflow time
            } else {
                tokens = newTokens;
                long timeUsedMs = (tokensToAdd * timeWindowMs) / limit;
                lastRefillTimestamp += timeUsedMs; // Preserve unused fractional time
            }
        }
    }

    public synchronized long getTokens() {
        refill();
        return tokens;
    }

    public long getCapacity() {
        return capacity;
    }

    public long getLimit() {
        return limit;
    }

    public long getTimeWindowSeconds() {
        return timeWindowSeconds;
    }
}
