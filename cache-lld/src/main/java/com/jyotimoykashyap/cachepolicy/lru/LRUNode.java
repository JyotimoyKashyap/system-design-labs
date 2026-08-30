package com.jyotimoykashyap.cachepolicy.lru;


import com.jyotimoykashyap.models.Node;

public class LRUNode<K> extends Node<K, LRUNode<K>> {

    protected LRUNode(K key) {
        super(key);
    }
}
