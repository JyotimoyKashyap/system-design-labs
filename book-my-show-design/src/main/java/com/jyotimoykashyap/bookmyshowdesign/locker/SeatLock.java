package com.jyotimoykashyap.bookmyshowdesign.locker;

import java.time.Instant;

public record SeatLock(
    int userId, Instant lockedAt, Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
