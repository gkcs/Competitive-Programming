package main.java.course;

import java.util.ArrayList;
import java.util.Arrays;

public class DynamicList {
    private String array[];
    private int size;

    public DynamicList() {
        array = new String[5];
        size = 0;
    }

    public void add(String element) {
        if (array.length == size) {
            System.out.println("DOUBLING SIZE");
            String[] temp = new String[size * 2];
            System.arraycopy(array, 0, temp, 0, array.length);
            array = temp;
        }
        array[size] = element;
        size++;
    }

    public String delete(int position) {
        if (position < 0 || position >= size) {
            throw new IllegalArgumentException();
        }
        String deletedElement = array[position];
        for (int i = position; i < size - 1; i++) {
            array[i] = array[i + 1];
        }
        if (size != array.length) {
            array[size - 1] = array[size];
        }
        size--;
        if (size < array.length / 4.0) {
            System.out.println("HALVING SIZE");
            String[] temp = new String[array.length / 2];
            System.arraycopy(array, 0, temp, 0, temp.length);
            array = temp;
        }
        return deletedElement;
    }

    public String get(int position) {
        if (position < 0 || position >= size) {
            throw new IllegalArgumentException();
        }
        return array[position];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        return "DynamicList{" +
                "array=" + Arrays.toString(array) +
                ", size=" + size +
                '}';
    }
}

class DynamicListMain {
    public static void main(String[] args) {
//        final DynamicList dynamicList = new DynamicList();
//        dynamicList.add("Java");
//        dynamicList.add("Python");
//        dynamicList.add("C++");
//        dynamicList.add("C#");
//        dynamicList.add("JavaScript");
//        dynamicList.add("Cobol");
//        dynamicList.delete(0);
//        dynamicList.delete(0);
//        dynamicList.delete(0);
//        dynamicList.delete(0);
//        System.out.println(dynamicList);
        ArrayList<String> dynamicList = new ArrayList<>();
        dynamicList.add("Java");
        dynamicList.add("Python");
        dynamicList.add("C++");
        dynamicList.add("C#");
        dynamicList.add("JavaScript");
        dynamicList.add("Cobol");
        dynamicList.remove(0);
        dynamicList.remove(0);
        dynamicList.remove(0);
        System.out.println(dynamicList);
        dynamicList.remove(2);
        dynamicList.remove("Cobol");
        System.out.println(dynamicList);
    }
}
