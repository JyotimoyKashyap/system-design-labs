package com.jyotimoykashyap.models;

import com.jyotimoykashyap.notification.event.Event;
import com.jyotimoykashyap.notification.event.TaskAssigneeChangeEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Task {
    private String name;
    private String description;
    private UUID id;
    private User assignedTo;
    private LocalDate createdAt;
    private LocalDate modifiedAt;
    private LocalDate dueDate;
    private Status status;

    private final List<Event<?>> domainEvents = new ArrayList<>();

    public Task(String name, String description, LocalDate dueDate) throws Exception{
        validateDueDate(dueDate);

        this.id = UUID.randomUUID();
        this.createdAt = LocalDate.now();
        updateLastModifiedDate();

        this.dueDate = dueDate;
        updateName(name);
        this.description = description;
        this.status = Status.TODO; // initial task is always set to TODO state
        this.assignedTo = null;
    }

    /**
     * * * * * * * * * * * * START SECTION : SETTERS * * * * * * * * * * *
     */

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateName(String name) {
        if (name.isBlank()) throw new IllegalArgumentException("Blank Task Name is not allowed");
        this.name = name;
    }

    public void updateAssignee(User user) {
        if (Objects.equals(this.assignedTo, user)) return;

        // assignee can be null at any point of time
        User oldAssignee = this.assignedTo;
        this.assignedTo = user;
        this.domainEvents.add(
                new TaskAssigneeChangeEvent(this.id, oldAssignee, user));
        updateLastModifiedDate(); // last modified date needs to be updated on every update
    }

    public void updateDueDate(LocalDate date)  {
        validateDueDate(date);
        this.dueDate = date;

        updateLastModifiedDate();
    }

    public void updateStatus(Status status) {
        this.status = status;

        updateLastModifiedDate();
    }

    /**
     * * * * * * * * * * * * END SECTION : SETTERS * * * * * * * * * * * *
     */

    /**
     * * * * * * * * * * * * START SECTION : GETTERS * * * * * * * * * * * *
     */

    public UUID getId() {
        return this.id;
    }


    private void updateLastModifiedDate() {
        this.modifiedAt = LocalDate.now();
    }

    public List<Event<?>> pullDomainEvents() {
        List<Event<?>> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    private void validateDueDate(LocalDate dueDate) {
        Objects.requireNonNull(dueDate);
        // due date cannot be less than today's date
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date should be greater than equal to today's date");
        }
    }

}
