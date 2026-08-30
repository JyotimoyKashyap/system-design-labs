package com.jyotimoykashyap.models;

public class DoublyLinkedList <K, N extends  Node<K,N>> {
    protected N head;
    protected N tail;
    protected int size;

    public DoublyLinkedList(N head, N tail) {
        if (head == null || tail == null) {
            throw new IllegalArgumentException("head and null cannot be NULL");
        }
        this.head = head;
        this.tail = tail;
        // link head and tail
        head.next = tail;
        tail.prev = head;

        size = 0;
    }

    public void remove(N node) {
        if (size <= 0)
            throw new RuntimeException("DoublyLinkedList is empty");

        N left = node.prev;
        N right = node.next;

        left.next = right;
        right.prev = left;

        size--;
    }

    protected void addToHead(N node) {
        // create connections for incoming node
        node.next = head.next;
        node.prev = head;

        // sever previous connections
        head.next.prev = node;
        head.next = node;

        size++;
    }

    protected void addToTail(N node) {
        // create the connections for incoming node
        node.next = tail;
        node.prev = tail.prev;

        // sever the previous connections
        tail.prev.next = node;
        tail.prev = node;

        size++;
    }

    public int getSize() { return size; }
}
