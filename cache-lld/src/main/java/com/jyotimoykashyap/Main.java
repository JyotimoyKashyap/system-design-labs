package com.jyotimoykashyap;

import com.jyotimoykashyap.cache.InMemoryCache;
import com.jyotimoykashyap.cachepolicy.lru.LRUCachePolicy;

public class Main {
    public static void main(String[] args) {
        // Create an LRU Cache with capacity 3
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(3)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();

        System.out.println("--- Testing LRU Cache ---");

        // 1. Basic put and get
        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        System.out.println("Cache full. Keys: A, B, C");

        // 2. Access A to make it most recently used
        System.out.println("Accessing A: " + cache.get("A").orElse(-1));

        // 3. Put a new key, should evict B (least recently used)
        System.out.println("Putting D...");
        cache.put("D", 4);

        System.out.println("Checking B (should be evicted): " + cache.get("B").isPresent()); // false
        System.out.println("Checking C: " + cache.get("C").isPresent()); // true
        System.out.println("Checking D: " + cache.get("D").isPresent()); // true

        // 4. Update existing key (counts as access)
        System.out.println("Updating C...");
        cache.put("C", 33);

        // 5. Put another new key, should evict A (D is newer, C was just updated)
        System.out.println("Putting E...");
        cache.put("E", 5);

        System.out.println("Checking A (should be evicted): " + cache.get("A").isPresent()); // false
        System.out.println("Checking D: " + cache.get("D").isPresent()); // true
        System.out.println("Checking C: " + cache.get("C").isPresent()); // true
        System.out.println("Checking E: " + cache.get("E").isPresent()); // true

        System.out.println("--- LRU Cache works! ---");
    }
}
