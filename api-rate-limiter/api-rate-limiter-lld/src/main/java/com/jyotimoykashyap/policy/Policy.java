package com.jyotimoykashyap.policy;

public record Policy(
        long limit,
        long timeWindowSeconds,
        long burstCapacity
) {
    public Policy {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit should be > 0");
        }

        if (timeWindowSeconds <= 0) {
            throw new IllegalArgumentException("timeWindow should be > 0");
        }

        if (burstCapacity <= 0) {
            throw new IllegalArgumentException("burstCapacity should be > 0");
        }
    }
}
