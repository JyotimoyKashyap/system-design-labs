package com.jyotimoykashyap.repository;

import com.jyotimoykashyap.cache.CacheDao;
import com.jyotimoykashyap.dao.TaskDao;
import com.jyotimoykashyap.models.Task;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class TaskRepository {
    private CacheDao cacheDao;
    private TaskDao taskDao;

    public TaskRepository(CacheDao cacheDao, TaskDao taskDao) {
        this.cacheDao = cacheDao;
        this.taskDao = taskDao;
    }

    /**
     * write logic for cache hit
     * if there's a cache miss then store it in the cache for the next time
     *
     * this kind of logic is a little flawed since if we do a sequential read
     * then it could take the round trip time to be very high
     *
     * for demo cases we are doing it this way
     */
    public Task getTask(UUID uuid) {
        Objects.requireNonNull(uuid);

        // Check cache (Cache Hit)
        Optional<Task> cachedTask = cacheDao.get(uuid);
        if (cachedTask.isPresent()) {
            return cachedTask.get();
        }

        // check DB (Cache Miss)
        Task taskFromDb = taskDao.getTask(uuid)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        cacheDao.put(taskFromDb);
        return taskFromDb;
    }

    public List<Task> getAllTask() {
        List<Task> tasks = taskDao.getAllTask();

        if (tasks.isEmpty()) throw new RuntimeException("No Tasks Created Yet");
        return tasks;
    }

    public void deleteTask(UUID uuid) {
        // delete from both cache and database
        cacheDao.remove(uuid);
        taskDao.deleteTask(uuid);
    }

    public void updateTask(Task task) {
        Objects.requireNonNull(task);
        Objects.requireNonNull(task.getId());

        taskDao.updateTask(task);
        cacheDao.put(task);
    }

    public void saveTask(Task task) {
        Objects.requireNonNull(task);
        Objects.requireNonNull(task.getId());

        // check if the object is already created earlier
        Optional<Task> presentTask = taskDao.getTask(task.getId());
        if (presentTask.isPresent()) {
            throw new RuntimeException("Task is already saved");
        }

        taskDao.saveTask(task);
        cacheDao.put(task);
    }
}
