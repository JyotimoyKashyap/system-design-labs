package com.jyotimoykashyap.ratelimiters.tokenbucket;

public class TokenBucket {
    private final long capacity;
    private final long refillTokensPerSecond;
    private long tokens;
    private long lastRefillTimestamp;

    public TokenBucket(long capacity, long refillTokensPerSecond, long lastRefillTimestamp) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }
        if (refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException("Refill rate must be > 0");
        }
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.tokens = capacity; // Bucket starts full to allow initial legitimate burst
        this.lastRefillTimestamp = lastRefillTimestamp;
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

        // Calculate whole tokens earned in elapsed time
        long tokensToAdd = (elapsedMs * refillTokensPerSecond) / 1000;

        if (tokensToAdd > 0) {
            long newTokens = tokens + tokensToAdd;
            if (newTokens >= capacity) {
                tokens = capacity;
                lastRefillTimestamp = now; // Bucket full; discard overflow time
            } else {
                tokens = newTokens;
                long timeUsedMs = (tokensToAdd * 1000) / refillTokensPerSecond;
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
}
