package com.jyotimoykashyap.splitwiselld.strategy;

import com.jyotimoykashyap.splitwiselld.models.User;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Incoming request specification for a participant's split.
 * - For EQUAL: amount and percentage can be null.
 * - For EXACT: amount must be provided.
 * - For PERCENT: percentage must be provided.
 */
public record SplitRequest (User user, BigDecimal amount, BigDecimal percentage) {
    public SplitRequest {
        Objects.requireNonNull(user, "User must not be null in SplitRequest");
    }

    public static SplitRequest equal(User user) {
        return new SplitRequest(user, null, null);
    }

    public static SplitRequest exact(User user, BigDecimal amount) {
        return new SplitRequest(user, amount, null);
    }

    public static SplitRequest percent(User user, BigDecimal percentage) {
        return new SplitRequest(user, null, percentage);
    }
}
