package com.jyotimoykashyap.models;

import java.time.LocalDate;
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
        // assignee can be null at any point of time
        this.assignedTo = user;
        updateLastModifiedDate(); // last modified date needs to be updated on every update
    }

    public void updateDueDate(LocalDate date) throws Exception {
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

    private void validateDueDate(LocalDate dueDate) throws Exception {
        Objects.requireNonNull(dueDate);
        // due date cannot be less than today's date
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date should be greater than equal to today's date");
        }
    }

}
