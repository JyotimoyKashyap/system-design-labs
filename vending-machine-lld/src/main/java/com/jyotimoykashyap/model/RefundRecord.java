package com.jyotimoykashyap.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class RefundRecord {
    private final String refundId;
    private final String sessionId;
    private final String transactionId;
    private final BigDecimal amount;
    private final String reason;
    private int retryCount;
    private final Instant createdAt;

    public RefundRecord(String refundId, String sessionId, String transactionId, BigDecimal amount, String reason) {
        this.refundId = Objects.requireNonNull(refundId);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.transactionId = Objects.requireNonNull(transactionId);
        this.amount = Objects.requireNonNull(amount);
        this.reason = reason;

        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
