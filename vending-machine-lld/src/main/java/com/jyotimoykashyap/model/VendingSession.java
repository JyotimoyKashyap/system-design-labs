package com.jyotimoykashyap.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class VendingSession {
    private final String sessionId;
    private final List<String> slotCodes;
    private final List<Item> items;
    private SessionStatus status;
    private final Instant createdAt;
    private Instant expiresAt;

    public VendingSession(String sessionId, List<String> slotCodes, List<Item> items, long ttlSeconds) {
        this.sessionId = Objects.requireNonNull(sessionId);
        this.slotCodes = List.copyOf(Objects.requireNonNull(slotCodes));
        this.items = List.copyOf(Objects.requireNonNull(items));
        this.status = SessionStatus.CREATED;
        this.createdAt = Instant.now();
        this.expiresAt = this.createdAt.plusSeconds(ttlSeconds);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isCancelled() {
        return status == SessionStatus.CANCELLED;
    }

    public String getSessionId() {
        return sessionId;
    }

    public List<String> getSlotCodes() {
        return slotCodes;
    }

    public List<Item> getItems() {
        return items;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(Item::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setStatus(SessionStatus status) { this.status = status; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
