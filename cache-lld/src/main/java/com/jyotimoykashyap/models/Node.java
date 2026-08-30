package com.jyotimoykashyap.models;

public abstract class Node<K, N extends Node<K,N>> {
    public K key;
    public N prev;
    public N next;

    protected Node(K key) {
        this.key = key;
    }
}
