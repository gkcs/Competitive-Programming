package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class DynamicArrayMain {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final DynamicArray dynamicArray = new DynamicArray();
        int input = Integer.parseInt(bufferedReader.readLine());
        while (input != 0) {
            if (input < 0) {
                dynamicArray.delete();
            } else {
                dynamicArray.add(input);
            }
            input = Integer.parseInt(bufferedReader.readLine());
        }
        System.out.println(dynamicArray);
    }
}

class DynamicArray {
    private int size = 1;
    private int a[] = new int[size];
    private int counter = 0;

    public void add(int element) {
        if (counter == size) {
            System.out.println("DOUBLING SIZE OF ARRAY!!!" + size);
            size = 2 * size;
            final int[] temp = new int[size];
            System.arraycopy(a, 0, temp, 0, a.length);
            a = temp;
        }
        a[counter] = element;
        counter++;
    }

    public void delete() {
        if (counter > 0) {
            counter--;
            a[counter] = 0;
        }
        if (counter == size / 4) {
            System.out.println("HALVING SIZE OF ARRAY!!!" + size);
            size = size / 2;
            final int[] temp = new int[size];
            System.arraycopy(a, 0, temp, 0, temp.length);
            a = temp;
        }
    }

    @Override
    public String toString() {
        return "DynamicArray{" +
                "size=" + size +
                ", a=" + Arrays.toString(a) +
                ", counter=" + counter +
                '}';
    }
}