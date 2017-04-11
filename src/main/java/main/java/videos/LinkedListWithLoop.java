package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Code to detect the start of a loop in a linked list.
 * Requested by: Jayadev Senapathi Kota (Hi there!)
 *
 * Run this after removing the package statement above, and specifying the list.
 *
 * Sample Test Case:
 *
 * 1->2->3->4->5->6->3
 *
 * Output:
 *
 * We should have a loop at 3
 * We have a loop! Meeting point is: 5
 * The start of the loop is at 3!
 */

public class LinkedListWithLoop {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final String nodes[] = bufferedReader.readLine().split("->");
        final Node start = new Node(Integer.parseInt(nodes[0]));
        Node current = start;
        final Map<Integer, Node> values = new HashMap<>();
        for (int i = 1; i < nodes.length; i++) {
            final int value = Integer.parseInt(nodes[i]);
            if (values.containsKey(value)) {
                current.next = values.get(value);
                System.out.println("We should have a loop at " + value);
                break;
            } else {
                current.next = new Node(value);
                values.put(value, current.next);
                current = current.next;
            }
        }

        Node hare = start, tortoise = start;
        try {
            do {
                tortoise = tortoise.next;
                hare = hare.next.next;
            }
            while (hare != tortoise);
            System.out.println("We have a loop! Meeting point is: " + hare.value);
            hare = start;
            do {
                tortoise = tortoise.next;
                hare = hare.next;
            }
            while (hare != tortoise);
            System.out.println("The start of the loop is at " + hare.value + "!");
        } catch (NullPointerException loopNotDetected) {
            System.out.println("I don't see a loop there!");
        }
    }


    private static class Node {
        Node next;
        final int value;

        public Node(final int value) {
            this.value = value;
        }
    }
}


