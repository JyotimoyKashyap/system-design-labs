package com.jyotimoykashyap.notification.event;

import com.jyotimoykashyap.models.User;

import java.time.Instant;
import java.util.UUID;

public class TaskAssigneeChangeEvent implements Event<User> {
    private final UUID entityId;
    private final User oldValue;
    private final User newValue;
    private final Instant time;

    public TaskAssigneeChangeEvent(UUID entityId, User oldValue, User newValue) {
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.time = Instant.now();
    }

    @Override
    public UUID getEntityId() {
        return entityId;
    }

    @Override
    public String getEventName() {
        return "TASK_ASSIGNEE_CHANGED";
    }

    @Override
    public User getOldValue() {
        return oldValue;
    }

    @Override
    public User getNewValue() {
        return newValue;
    }

    @Override
    public Instant getEventTime() {
        return time;
    }
}
