package com.jyotimoykashyap.datastructures;

import java.util.Optional;
import java.util.UUID;

public class DoublyLinkedList<V, N extends Node<V>> {
    N head;
    N tail;
    int size;

    public DoublyLinkedList(N head, N tail) {
        if (head == null || tail == null)
            throw new IllegalArgumentException("Head and Tail cannot be NULL");

        this.head = head;
        this.tail = tail;

        // connect the head and tail
        head.next = tail;
        tail.prev = head;
    }

    public void addToHead(Node<V> node) {
        node.next = head.next;
        node.prev = head;

        head.next.prev = node;
        head.next = node;
        size++;
    }

    public void remove(Node<V> node) {
        if (size <= 0)
            throw new RuntimeException("Cannot delete since size is 0");

        Node<V> left = node.prev;
        Node<V> right = node.next;

        left.next = right;
        right.prev = left;
        size--;
    }

    public Optional<UUID> removeTail() {
        if (size <= 0 || tail.prev == head) {
            return Optional.empty();
        }

        Node<V> evict = tail.prev;
        remove(evict);
        return Optional.ofNullable(evict.key);
    }
}
