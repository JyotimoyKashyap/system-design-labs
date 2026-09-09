package com.jyotimoykashyap.cache;

import com.jyotimoykashyap.models.Task;

import java.util.Optional;
import java.util.UUID;

public interface CacheDao {
    Optional<Task> get(UUID uuid);
    void put(Task task);
    void remove(UUID uuid);
}
