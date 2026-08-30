package com.jyotimoykashyap;

import com.jyotimoykashyap.cache.InMemoryCache;
import com.jyotimoykashyap.cachepolicy.lru.LRUCachePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    @Test
    @DisplayName("get() returns Optional.empty() for missing key")
    void getMissingKey() {
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(2)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();
        
        assertTrue(cache.get("missing").isEmpty());
    }

    @Test
    @DisplayName("Basic put and get")
    void basicPutAndGet() {
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(2)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();
        
        cache.put("A", 10);
        cache.put("B", 20);
        
        assertEquals(10, cache.get("A").get());
        assertEquals(20, cache.get("B").get());
    }

    @Test
    @DisplayName("Evicts Least Recently Used (LRU) key on put when full")
    void evictsLRU() {
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(2)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();
                
        cache.put("A", 10);
        cache.put("B", 20);
        // order: A (oldest), B (newest)
        
        cache.put("C", 30); // evicts A
        
        assertTrue(cache.get("A").isEmpty());
        assertEquals(20, cache.get("B").get());
        assertEquals(30, cache.get("C").get());
    }

    @Test
    @DisplayName("get() updates recency (moves key to front)")
    void getUpdatesRecency() {
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(2)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();
                
        cache.put("A", 10);
        cache.put("B", 20);
        
        cache.get("A"); // A is now most recently used, B is oldest
        
        cache.put("C", 30); // evicts B
        
        assertTrue(cache.get("B").isEmpty());
        assertEquals(10, cache.get("A").get());
        assertEquals(30, cache.get("C").get());
    }

    @Test
    @DisplayName("put() on existing key updates value and recency")
    void putUpdatesExistingValueAndRecency() {
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(2)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();
                
        cache.put("A", 10);
        cache.put("B", 20);
        
        cache.put("A", 100); // A's value updated to 100, and it's now MRU
        
        cache.put("C", 30); // evicts B
        
        assertEquals(100, cache.get("A").get());
        assertTrue(cache.get("B").isEmpty());
    }

    @Test
    @DisplayName("Explicit remove() deletes the key and allows new inserts")
    void explicitRemove() {
        Cache<String, Integer> cache = new InMemoryCache.Builder<String, Integer>()
                .setCapacity(2)
                .setCachePolicy(new LRUCachePolicy<>())
                .build();
                
        cache.put("A", 10);
        cache.put("B", 20);
        
        cache.remove("A");
        assertTrue(cache.get("A").isEmpty());
        
        // Cache now only has 1 item, so this shouldn't evict B
        cache.put("C", 30);
        
        assertEquals(20, cache.get("B").get());
        assertEquals(30, cache.get("C").get());
    }

    @Test
    @DisplayName("Builder rejects invalid capacity")
    void builderValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new InMemoryCache.Builder<String, Integer>()
                    .setCapacity(0)
                    .setCachePolicy(new LRUCachePolicy<>())
                    .build();
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new InMemoryCache.Builder<String, Integer>()
                    .setCapacity(5)
                    .setCachePolicy(null)
                    .build();
        });
    }
}
