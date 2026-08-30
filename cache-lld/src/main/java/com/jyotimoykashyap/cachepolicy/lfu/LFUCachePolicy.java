package com.jyotimoykashyap.cachepolicy.lfu;

import com.jyotimoykashyap.CachePolicy;

import java.util.HashMap;
import java.util.Map;

public class LFUCachePolicy<K> implements CachePolicy<K> {

    private int minFreq;
    private final Map<K, LFUNode<K>> nodeMap;
    private final Map<Integer, LFULinkedList<K>> usedCountLRU;

    public LFUCachePolicy() {
        this.nodeMap = new HashMap<>();
        this.usedCountLRU = new HashMap<>();
    }

    @Override
    public void onAccess(K key) {
        if (!nodeMap.containsKey(key)) return;

        LFUNode<K> node = nodeMap.get(key);
        removeNodeFromList(node);

        // If the frequency bucket we just removed from is now empty,
        // and it was the minFreq, then increment minFreq.
        if (minFreq == node.usedCount && !usedCountLRU.containsKey(node.usedCount)) {
            minFreq++;
        }

        node.usedCount++;
        addNodeToList(key, node);
    }

    @Override
    public void onInsert(K key) {
        if (!nodeMap.containsKey(key)) {
            LFUNode<K> node = new LFUNode<>(key, 1);
            minFreq = 1;
            addNodeToList(key, node);
            nodeMap.put(key, node);
        }
    }

    @Override
    public K evict() {
        if (!usedCountLRU.containsKey(minFreq)) {
            throw new RuntimeException("Eviction failed: minFreq bucket is empty or missing.");
        }

        LFULinkedList<K> list = usedCountLRU.get(minFreq);
        K evictedKey = list.removeTail();

        if (list.getSize() == 0) {
            usedCountLRU.remove(minFreq);
        }
        
        nodeMap.remove(evictedKey);
        return evictedKey;
    }

    @Override
    public void onRemove(K key) {
        if (nodeMap.containsKey(key)) {
            LFUNode<K> node = nodeMap.remove(key);
            removeNodeFromList(node);
        }
    }

    private void removeNodeFromList(LFUNode<K> node) {
        if (!usedCountLRU.containsKey(node.usedCount)) return;

        LFULinkedList<K> list = usedCountLRU.get(node.usedCount);
        list.remove(node);

        if (list.getSize() == 0) {
            usedCountLRU.remove(node.usedCount);
        }
    }

    private void addNodeToList(K key, LFUNode<K> node) {
        if (usedCountLRU.containsKey(node.usedCount)) {
            usedCountLRU.get(node.usedCount).addToHead(node);
        } else {
            // create new list with sentinels
            LFUNode<K> head = new LFUNode<>(null, 0);
            LFUNode<K> tail = new LFUNode<>(null, 0);
            
            LFULinkedList<K> list = new LFULinkedList<>(head, tail);
            list.addToHead(node);
            usedCountLRU.put(node.usedCount, list);
        }
    }
}
