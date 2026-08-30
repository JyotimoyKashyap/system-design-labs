package com.jyotimoykashyap.cachepolicy.lru;

import com.jyotimoykashyap.CachePolicy;

import java.util.HashMap;
import java.util.Map;

public class LRUCachePolicy<K> implements CachePolicy<K> {
    LRULinkedList<K> linkedList;
    Map<K, LRUNode<K>> nodeMap;

    public LRUCachePolicy() {
        LRUNode<K> head = new LRUNode<>(null);
        LRUNode<K> tail = new LRUNode<>(null);
        linkedList = new LRULinkedList<>(head, tail);
        nodeMap = new HashMap<>();
    }

    @Override
    public void onAccess(K key) {
        if (nodeMap.containsKey(key)) {
            LRUNode<K> node = nodeMap.get(key);
            linkedList.remove(node);
            linkedList.addToHead(node);
        }
    }

    @Override
    public void onInsert(K key) {
        if (!nodeMap.containsKey(key)) {
            LRUNode<K> node = new LRUNode<>(key);
            linkedList.addToHead(node);
            nodeMap.put(key, node);
        }
    }

    @Override
    public K evict() {
        if (linkedList.getSize() <= 0)
            throw new RuntimeException("LinkedList size is 0");

        K key = linkedList.removeTail();
        nodeMap.remove(key);
        return key;
    }

    @Override
    public void onRemove(K key) {
        if (nodeMap.containsKey(key)) {
            linkedList.remove(nodeMap.get(key));
            nodeMap.remove(key);
        }
    }
}
