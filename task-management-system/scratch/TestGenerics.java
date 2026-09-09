package com.jyotimoykashyap.data;
import java.util.UUID;

class Node<V> {
    Node<V> next;
    Node<V> prev;
    V value;
    UUID key;
}

class DoublyLinkedList<K extends Node<?>> {
    K head;
    K tail;
    int size;

    public DoublyLinkedList(K head, K tail) {
        this.head = head;
        this.tail = tail;
        head.next = tail;
        tail.prev = head;
    }

    public void addToHead(K node) {
        node.next = head.next;
        node.prev = head;
        if (head.next != null) {
            head.next.prev = node;
        }
        head.next = node;
        size++;
    }
}
