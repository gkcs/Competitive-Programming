package main.java.course;

import java.util.Arrays;

public class Stack {
    private final int array[];
    private int top = 0;

    public Stack(final int length) {
        array = new int[length];
    }

    public void push(final int element) {
        if (top == array.length) {
            throw new RuntimeException("Stack Overflow");
        }
        array[top] = element;
        top++;
    }

    public int pop() {
        if (top == 0) {
            throw new RuntimeException("Stack Underflow");
        }
        top--;
        return array[top];
    }

    public int peek() {
        if (top == 0) {
            throw new RuntimeException("No elements in stack");
        }
        return array[top - 1];
    }

    public boolean isEmpty() {
        return top == 0;
    }

    public int search(int x) {
        int i;
        for (i = top - 1; i >= 0; i--) {
            if (array[i] == x) {
                break;
            }
        }
        if (i >= 0) {
            return top - i;
        }
        return i;
    }

    @Override
    public String toString() {
        return "Stack{" +
                "array=" + Arrays.toString(array) +
                ", top=" + top +
                '}';
    }
}

class StackMain {
    public static void main(String[] args) {
//        java.util.Stack<Integer> stack = new java.util.Stack<>();
//        stack.push(12312);
//        System.out.println(stack.pop());
//        stack.push(12313);
//        System.out.println(stack.search(12314));
        {
            Stack myStack = new Stack(5);
            myStack.push(12312);
            System.out.println(myStack.pop());
            myStack.push(12313);
            myStack.push(12314);
            myStack.push(12315);
            myStack.push(12316);
            System.out.println(myStack.search(12313));
        }
    }
}
