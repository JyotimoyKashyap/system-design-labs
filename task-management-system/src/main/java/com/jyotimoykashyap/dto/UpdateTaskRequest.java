package com.jyotimoykashyap.dto;

import com.jyotimoykashyap.models.Status;
import com.jyotimoykashyap.models.User;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        UUID uuid,
        String name,
        String description,
        User assignedTo,
        LocalDate dueDate,
        Status status
) {}
