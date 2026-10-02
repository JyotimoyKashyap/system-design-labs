package com.jyotimoykashyap.cleanup;

import java.util.Objects;

public class RateLimiterCleaner implements Runnable {
    private final Cleanable cleanable;
    private final long ttlMillis;

    public RateLimiterCleaner(Cleanable cleanable, long ttlMillis) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis should be > 0");
        }
        this.cleanable = Objects.requireNonNull(cleanable, "cleanable cannot be null");
        this.ttlMillis = ttlMillis;
    }

    public RateLimiterCleaner(Cleanable cleanable, long intervalSeconds, long ttlMillis) {
        this(cleanable, ttlMillis);
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("intervalSeconds should be > 0");
        }
    }

    @Override
    public void run() {
        cleanable.cleanUpStaleEntries(ttlMillis);
    }

    public Cleanable getCleanable() {
        return cleanable;
    }

    public long getTtlMillis() {
        return ttlMillis;
    }
}
