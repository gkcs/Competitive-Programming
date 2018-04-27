package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SingleUseStones {
    public static void main(final String args[]) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line[] = bufferedReader.readLine().split(" ");
        final int w = Integer.parseInt(line[0]), l = Integer.parseInt(line[1]);
        line = bufferedReader.readLine().split(" ");
        final int a[] = new int[line.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = Integer.parseInt(line[i]);
        }
        int sum = 0;
        for (int i = 0; i < l; i++) {
            sum += a[i];
        }
        int minValue = sum;
        for (int i = l; i < w - 1; i++) {
            sum = sum - a[i - l] + a[i];
            minValue = Math.min(minValue, sum);
        }
        System.out.println(minValue);
    }
}
