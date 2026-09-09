package com.jyotimoykashyap.dao;

import com.jyotimoykashyap.models.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskDao {
    Optional<Task> getTask(UUID uuid);
    List<Task> getAllTask();
    Optional<UUID> saveTask(Task task);
    void updateTask(Task task);
    void deleteTask(UUID uuid);
}
