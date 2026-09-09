package com.jyotimoykashyap.datastructures;

import java.util.UUID;

public class Node<V> {
    public Node<V> next;
    public Node<V> prev;
    public V value;
    public UUID key;

    public Node(UUID key, V value) {
        this.key = key;
        this.value = value;
    }
}
