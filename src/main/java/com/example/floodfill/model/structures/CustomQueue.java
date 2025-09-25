package com.example.floodfill.model.structures;

import com.example.floodfill.model.Pixel;

public class CustomQueue {
    private Node<Pixel> front;
    private Node<Pixel> rear;

    public CustomQueue() {
        this.front = null;
        this.rear = null;
    }

    public void enqueue(Pixel pixel) {
        Node<Pixel> newNode = new Node<>(pixel);
        if (rear != null) {
            rear.setNext(newNode);
        }
        rear = newNode;
        if (front == null) {
            front = rear;
        }
    }

    public Pixel dequeue() {
        if (isEmpty()) {
            return null;
        }
        Pixel data = front.getData();
        front = front.getNext();
        if (front == null) {
            rear = null;
        }
        return data;
    }

    public boolean isEmpty() {
        return front == null;
    }
}