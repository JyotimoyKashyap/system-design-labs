package com.jyotimoykashyap.cachepolicy.lfu;

import com.jyotimoykashyap.models.DoublyLinkedList;

public class LFULinkedList<K> extends DoublyLinkedList<K, LFUNode<K>> {

    public LFULinkedList(LFUNode<K> head, LFUNode<K> tail) {
        super(head, tail);
    }

    public K removeTail() {
        if (size <= 0) throw new RuntimeException("LFULinkedList is empty");
        
        LFUNode<K> evictNode = tail.prev;
        super.remove(evictNode);
        
        return evictNode.key;
    }

    @Override
    public void addToHead(LFUNode<K> node) {
        super.addToHead(node);
    }
}
