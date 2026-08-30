package com.jyotimoykashyap.cache;


import com.jyotimoykashyap.Cache;
import com.jyotimoykashyap.CachePolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryCache<K,V> implements Cache<K,V> {

    private final int capacity;
    private final CachePolicy<K> cachePolicy;
    private final Map<K,V> map;
    private ReentrantLock lock;

    private InMemoryCache(Builder<K,V> builder) {
        this.cachePolicy = builder.cachePolicy;
        this.capacity = builder.capacity;

        // init the map with capacity
        this.map = new HashMap<>(this.capacity);

        // init lock
        lock = new ReentrantLock();
    }

    @Override
    public Optional<V> get(K key) {
        lock.lock();

        try {
            if (map.containsKey(key)) {
                cachePolicy.onAccess(key);
                return Optional.of(map.get(key));
            }
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.lock();

        try {
            if (get(key).isPresent()) {
                map.replace(key, value);
                return;
            }

            if (map.size() >= capacity) {
                K removalKey = cachePolicy.evict();
                remove(removalKey);
            }

            // create a new key in the map
            map.put(key, value);
            cachePolicy.onInsert(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        lock.lock();
        try {
            if (!map.containsKey(key))
                throw new RuntimeException(key.toString() + " is not present in Cache");

            map.remove(key);
            cachePolicy.onRemove(key);
        } finally {
            lock.unlock();
        }
    }


    /**
     * Builder for InMemoryCache
     * @param <K>
     * @param <V>
     */
    public static class Builder<K,V> {
        int capacity;
        CachePolicy<K> cachePolicy;

        public Builder<K,V> setCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder<K,V> setCachePolicy(CachePolicy<K> cachePolicy) {
            this.cachePolicy = cachePolicy;
            return this;
        }

        public InMemoryCache<K,V> build() {
            if (capacity <= 0)
                throw new IllegalArgumentException("Capacity cannot be less than or equal to 0");

            if (cachePolicy == null)
                throw new IllegalArgumentException("Cache Policy cannot be NULL");

            return new InMemoryCache<K,V>(this);
        }
    }
}
