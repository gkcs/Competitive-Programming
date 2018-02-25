package main.java.course;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class Queue {
    private final List<Integer> list;

    public Queue(final int size) {
        list = new LinkedList<>();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void push(final int element) {
        list.add(element);
    }

    public int pop() {
        return list.remove(0);
    }

    public int peek() {
        return list.get(0);
    }

    @Override
    public String toString() {
        return "Queue{" +
                "list=" + list +
                '}';
    }
}

class QueueMain {
    public static void main(String[] args) {
        final java.util.Queue<Integer> queue = new PriorityQueue<>();
        for (int i = 0; i < 5; i++) {
            queue.add(i);
        }
        System.out.println(queue);
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue.poll());
        System.out.println(queue);
        queue.add(100);
        System.out.println(queue);
    }
}
