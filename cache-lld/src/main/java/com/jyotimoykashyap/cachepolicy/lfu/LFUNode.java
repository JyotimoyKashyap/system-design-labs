package com.jyotimoykashyap.cachepolicy.lfu;

import com.jyotimoykashyap.models.Node;

public class LFUNode<K> extends Node<K, LFUNode<K>> {
    
    public int usedCount;

    public LFUNode(K key, int usedCount) {
        super(key);
        this.usedCount = usedCount;
    }
}
