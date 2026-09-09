package com.jyotimoykashyap.data;
import java.util.UUID;

class Node<V> {
    Node<V> next;
    Node<V> prev;
    V value;
    UUID key;
}

class DoublyLinkedList<V> {
    Node<V> head;
    Node<V> tail;
    int size;

    public DoublyLinkedList(Node<V> head, Node<V> tail) {
        this.head = head;
        this.tail = tail;
        head.next = tail;
        tail.prev = head;
    }

    // No K needed here!
    public void addToHead(Node<V> node) {
        node.next = head.next;
        node.prev = head;
        if (head.next != null) {
            head.next.prev = node;
        }
        head.next = node;
        size++;
    }
}

class MyNode<V> extends Node<V> {
    String extraData;
}

class Test {
    public static void main(String[] args) {
        DoublyLinkedList<String> list = new DoublyLinkedList<>(new Node<>(), new Node<>());
        MyNode<String> myNode = new MyNode<>();
        // Polymorphism allows this!
        list.addToHead(myNode);
    }
}
