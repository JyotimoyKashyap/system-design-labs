package com.jyotimoykashyap.service;

import com.jyotimoykashyap.dto.UpdateTaskRequest;
import com.jyotimoykashyap.models.Task;
import com.jyotimoykashyap.notification.event.Event;
import com.jyotimoykashyap.notification.publisher.EventPublisher;
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
    private final EventPublisher publisher;

    // init the cache and get the database instance here
    public TaskService(TaskRepository repository, EventPublisher publisher) {
        this.repository = Objects.requireNonNull(repository,
                "Repository cannot be null");
        this.publisher = Objects.requireNonNull(publisher,
                "Publisher cannot be null");
    }

    public List<Task> getAllTasks() {
        return repository.getAllTask();
    }

    public Task getTask(UUID uuid) {
        return repository.getTask(uuid);
    }

    public void updateTask(UpdateTaskRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");
        Objects.requireNonNull(request.uuid(), "UUID cannot be null");

        Task task = repository.getTask(request.uuid());

        if (request.name() != null) task.updateName(request.name());
        if (request.description() != null) task.updateDescription(request.description());
        if (request.assignedTo() != null) task.updateAssignee(request.assignedTo());
        if (request.dueDate() != null) task.updateDueDate(request.dueDate());
        if (request.status() != null) task.updateStatus(request.status());

        repository.updateTask(task);
        task.pullDomainEvents().forEach(publisher::notify);
    }

    public void deleteTask(UUID uuid) {
        repository.deleteTask(uuid);
    }

    public UUID saveTask(Task task) {
        repository.saveTask(task);
        return task.getId();
    }

}
