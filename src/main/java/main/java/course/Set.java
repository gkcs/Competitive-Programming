package main.java.course;

import java.util.Arrays;

public class Set {
    final int array[] = new int[20];
    int size = 0;

    public void add(int element) {
        for (int i = 0; i < size; i++) {
            if (array[i] == element)
                return;
        }
        array[size] = element;
        size++;
    }

    public void delete(int element) {
        if (isEmpty()) {
            throw new RuntimeException("Set is empty");
        }
        for (int i = 0; i < size; i++) {
            if (array[i] == element) {
                array[i] = array[size - 1];
                size--;
                break;
            }
        }
    }

    public boolean search(int element) {
        for (int i = 0; i < size; i++) {
            if (array[i] == element) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        return "Set{" +
                "array=" + Arrays.toString(array) +
                ", size=" + size +
                '}';
    }
}

class SetMain {
    public static void main(String[] args) {
        Set set = new Set();
//        java.util.Set<Integer> set = new HashSet<>();
        for (int i = 10; i > 3; i--) {
            set.add(i);
        }
        for (int i = 0; i < 10; i++) {
            set.add(i);
        }
        set.delete(0);
        set.delete(3);
        System.out.println(set.search(4));
        System.out.println(set.search(0));
        System.out.println(set.search(3));
        System.out.println(set);
    }
}