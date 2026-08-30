package com.jyotimoykashyap;

public interface CachePolicy<K> {
    void onAccess(K key);
    void onInsert(K key);
    K evict();
    void onRemove(K key);
}
