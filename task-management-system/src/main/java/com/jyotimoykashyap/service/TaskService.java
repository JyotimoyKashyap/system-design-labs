package com.jyotimoykashyap.service;

import com.jyotimoykashyap.models.Task;
import com.jyotimoykashyap.repository.TaskRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service class that will interact with the client.
 * For a client server communication, I should use dto objects here
 * But for simplicity sake, I'm just using the Task model directly
 */
public class TaskService {
    private final TaskRepository repository;

    // init the cache and get the database instance here
    public TaskService(TaskRepository repository) {
        this.repository = Objects.requireNonNull(repository,
                "Repository cannot be null");
    }

    public List<Task> getAllTasks() {
        return repository.getAllTask();
    }

    public Task getTask(UUID uuid) {
        return repository.getTask(uuid);
    }

    public void updateTask(Task task) {
        repository.updateTask(task);
    }

    public void deleteTask(UUID uuid) {
        repository.deleteTask(uuid);
    }

    public UUID saveTask(Task task) {
        repository.saveTask(task);
        return task.getId();
    }

}
