package com.example.floodfill.model.structures;

import com.example.floodfill.model.Pixel;

public class CustomStack {
    private Node<Pixel> top;

    public CustomStack() {
        this.top = null;
    }

    public void push(Pixel pixel) {
        Node<Pixel> newNode = new Node<>(pixel);
        newNode.setNext(top);
        top = newNode;
    }

    public Pixel pop() {
        if (isEmpty()) {
            return null;
        }
        Pixel data = top.getData();
        top = top.getNext();
        return data;
    }

    public boolean isEmpty() {
        return top == null;
    }
}