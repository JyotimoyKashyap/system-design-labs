package com.jyotimoykashyap.cachepolicy.lru;

import com.jyotimoykashyap.models.DoublyLinkedList;

public class LRULinkedList<K> extends DoublyLinkedList<K, LRUNode<K>> {

    public LRULinkedList(LRUNode<K> head, LRUNode<K> tail) {
        super(head, tail);
    }

    public K removeTail() {
        LRUNode<K> evictNode = tail.prev;
        super.remove(evictNode);
        return evictNode.key;
    }

    @Override
    public void addToHead(LRUNode<K> node) {
        super.addToHead(node);
    }
}
