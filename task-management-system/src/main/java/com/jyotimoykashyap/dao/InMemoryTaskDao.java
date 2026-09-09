package com.jyotimoykashyap.dao;

import com.jyotimoykashyap.models.Task;

import java.util.*;

/**
 * This class is a singleton since we want only 1 instance of this
 * class running in our process the entire time when the process is running.
 */
public class InMemoryTaskDao implements TaskDao {

    private static volatile InMemoryTaskDao INSTANCE;
    private final Map<UUID, Task> taskMap;

    private InMemoryTaskDao() {
        taskMap = new HashMap<>();
    }

    public static InMemoryTaskDao getInstance() {
        if (INSTANCE == null) {
            synchronized (InMemoryTaskDao.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InMemoryTaskDao();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Optional<Task> getTask(UUID uuid) {
        return Optional.ofNullable(taskMap.get(uuid));
    }

    @Override
    public List<Task> getAllTask() {
        return taskMap.values().stream().toList();
    }

    @Override
    public Optional<UUID> saveTask(Task task) {
        Objects.requireNonNull(task, "Cannot save a null task");

        if (taskMap.containsKey(task.getId())) {
            throw new IllegalArgumentException("A task with ID " + task.getId() + " already exists in the database");
        }

        taskMap.put(task.getId(), task);
        return Optional.of(task.getId());
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Cannot update a null task");
        if (!taskMap.containsKey(task.getId())) {
            throw new IllegalArgumentException("Cannot update : Task with ID " +
                    task.getId() + " does not exist");
        }
        taskMap.replace(task.getId(), task);
    }

    @Override
    public void deleteTask(UUID uuid) {
        Objects.requireNonNull(uuid, "NULL UUID cannot be deleted");
        taskMap.remove(uuid);
    }
}
