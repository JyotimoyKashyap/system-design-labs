package com.jyotimoykashyap.notification.event;

import java.time.Instant;
import java.util.UUID;

public interface Event<T> {
    UUID getEntityId();
    String getEventName();
    T getOldValue();
    T getNewValue();
    Instant getEventTime();
}
