package com.jyotimoykashyap.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class PaymentRecord {
    private final String transactionId;
    private final String sessionId;
    private PaymentStatus status;
    private final PaymentType type;
    private final BigDecimal amount;
    private final Instant createdAt;

    public PaymentRecord(String transactionId, String sessionId, PaymentType paymentType, BigDecimal amount) {
        this.transactionId = Objects.requireNonNull(transactionId);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.type = paymentType;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
