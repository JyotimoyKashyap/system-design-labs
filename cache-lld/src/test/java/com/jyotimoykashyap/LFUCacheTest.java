package com.jyotimoykashyap;

import com.jyotimoykashyap.cache.InMemoryCache;
import com.jyotimoykashyap.cachepolicy.lfu.LFUCachePolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class LFUCacheTest {

    // Helper method to keep tests concise
    private Cache<Integer, Integer> createCache(int capacity) {
        return new InMemoryCache.Builder<Integer, Integer>()
                .setCapacity(capacity)
                .setCachePolicy(new LFUCachePolicy<>())
                .build();
    }

    @Test
    @DisplayName("get() returns Optional.empty() for missing key")
    void getMissingKey() {
        Cache<Integer, Integer> cache = createCache(2);
        assertTrue(cache.get(1).isEmpty());
    }

    @Test
    @DisplayName("Basic put and get")
    void basicPutAndGet() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 10);
        cache.put(2, 20);
        assertEquals(10, cache.get(1).get());
        assertEquals(20, cache.get(2).get());
    }

    @Test
    @DisplayName("Evicts least frequently used key")
    void evictsLFU() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.get(1);       // key 1 freq = 2, key 2 freq = 1
        cache.put(3, 30);   // evicts key 2 (lowest freq)

        assertTrue(cache.get(2).isEmpty());  // evicted
        assertEquals(10, cache.get(1).get());  // still there
        assertEquals(30, cache.get(3).get());  // still there
    }

    @Test
    @DisplayName("LRU tie-breaking when frequencies are equal")
    void lruTieBreaker() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 10);   // freq 1
        cache.put(2, 20);   // freq 1 — both at freq 1, key 1 is LRU (oldest unaccessed)
        cache.put(3, 30);   // evicts key 1 (LRU among freq-1)

        assertTrue(cache.get(1).isEmpty());  // evicted
        assertEquals(20, cache.get(2).get());  // still there
        assertEquals(30, cache.get(3).get());  // still there
    }

    @Test
    @DisplayName("put() updates value for existing key")
    void putUpdatesExistingValue() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 10);
        cache.put(1, 100);  // update value
        assertEquals(100, cache.get(1).get());
    }

    @Test
    @DisplayName("put() on existing key bumps frequency (shouldn't be evicted easily)")
    void putExistingKeyBumpsFrequency() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 10);   // freq 1
        cache.put(2, 20);   // freq 1
        cache.put(1, 100);  // updates key 1 — get() inside bumps freq to 2
        cache.put(3, 30);   // evicts key 2 (freq 1), not key 1 (freq 2)

        assertEquals(100, cache.get(1).get()); // still there with updated value
        assertTrue(cache.get(2).isEmpty());  // evicted
        assertEquals(30, cache.get(3).get());  // still there
    }

    @Test
    @DisplayName("Capacity of 1")
    void capacityOne() {
        Cache<Integer, Integer> cache = createCache(1);
        cache.put(1, 10);
        assertEquals(10, cache.get(1).get());

        cache.put(2, 20);  // evicts key 1
        assertTrue(cache.get(1).isEmpty());
        assertEquals(20, cache.get(2).get());
    }

    @Test
    @DisplayName("Constructor rejects invalid builder arguments")
    void rejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> createCache(0));
        assertThrows(IllegalArgumentException.class, () -> createCache(-1));
    }

    @Test
    @DisplayName("Multiple evictions in sequence")
    void multipleEvictions() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);   // evicts key 1
        assertTrue(cache.get(1).isEmpty());

        cache.put(4, 40);   // evicts key 2
        assertTrue(cache.get(2).isEmpty());
        assertEquals(30, cache.get(3).get());
        assertEquals(40, cache.get(4).get());
    }

    @Test
    @DisplayName("Frequency promotion across multiple levels")
    void frequencyPromotion() {
        Cache<Integer, Integer> cache = createCache(3);
        cache.put(1, 10);   // freq 1
        cache.put(2, 20);   // freq 1
        cache.put(3, 30);   // freq 1

        cache.get(1);        // freq 2
        cache.get(1);        // freq 3
        cache.get(2);        // freq 2

        // freqs: key1=3, key2=2, key3=1
        cache.put(4, 40);   // evicts key 3 (freq 1)

        assertTrue(cache.get(3).isEmpty());
        assertEquals(10, cache.get(1).get());
        assertEquals(20, cache.get(2).get());
        assertEquals(40, cache.get(4).get());
    }

    @Test
    @DisplayName("LeetCode 460 example")
    void leetCode460Example() {
        Cache<Integer, Integer> cache = createCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        assertEquals(1, cache.get(1).get());   // key 1 freq = 2

        cache.put(3, 3);                 // evicts key 2 (freq 1)
        assertTrue(cache.get(2).isEmpty());  // evicted
        assertEquals(3, cache.get(3).get());   // freq 2

        cache.put(4, 4);                 // evicts key 1 or 3 (both freq 2) — key 1 is LRU
        assertTrue(cache.get(1).isEmpty());  // evicted
        assertEquals(3, cache.get(3).get());
        assertEquals(4, cache.get(4).get());
    }
}
