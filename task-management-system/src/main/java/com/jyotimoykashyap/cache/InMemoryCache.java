package com.jyotimoykashyap.cache;

import com.jyotimoykashyap.datastructures.DoublyLinkedList;
import com.jyotimoykashyap.datastructures.Node;
import com.jyotimoykashyap.models.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * An LRU based in-memory cache.
 */
public class InMemoryCache implements CacheDao {
    private final Map<UUID, Node<Task>> taskMap;
    private final DoublyLinkedList<Task, Node<Task>> linkedList;
    private final int capacity;

    // private constructor
    private InMemoryCache(Builder builder) {
        this.capacity = builder.capacity;
        taskMap = new HashMap<>();
        linkedList = new DoublyLinkedList<>(
                new Node<>(null, null), new Node<>(null, null));
    }

    public static class Builder {
        private static final int DEFAULT_CAPACITY = 100;
        private int capacity = DEFAULT_CAPACITY;

        public Builder capacity(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("Capacity must be greater than 0");
            }
            this.capacity = capacity;
            return this;
        }

        public InMemoryCache build() {
            return new InMemoryCache(this);
        }
    }

    @Override
    public Optional<Task> get(UUID uuid) {
        Objects.requireNonNull(uuid);
        Node<Task> node = taskMap.getOrDefault(uuid, null);
        if (node == null) {
            return Optional.empty();
        }

        linkedList.remove(node);
        linkedList.addToHead(node);

        return Optional.of(node.value);
    }

    @Override
    public void put(Task task) {
        Objects.requireNonNull(task, "NULL task is not allowed");
        Objects.requireNonNull(task.getId(), "NULL UUID is not allowed");

        if (taskMap.containsKey(task.getId())) {
            Node<Task> existingNode = taskMap.get(task.getId());
            existingNode.value = task;
            linkedList.remove(existingNode);
            linkedList.addToHead(existingNode);
            return;
        }

        if (getSize() >= capacity) {
            // evict the cache
            linkedList.removeTail()
                    .ifPresentOrElse(
                            taskMap::remove,
                            () -> {
                                throw new IllegalStateException("Cache evict failed");
                            }
                    );
        }

        Node<Task> node = new Node<>(task.getId(), task);
        taskMap.put(task.getId(), node);
        linkedList.addToHead(node);
    }

    @Override
    public void remove(UUID uuid) {
        Objects.requireNonNull(uuid);

        if (!taskMap.containsKey(uuid)) return;

        Node<Task> node = taskMap.get(uuid);
        linkedList.remove(node);
        taskMap.remove(uuid);
    }

    private int getSize() {
        return taskMap.size();
    }
}
