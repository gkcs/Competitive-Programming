package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Codeforces Single Use Stones: https://youtu.be/5s0MRxojQQo
 * Problem Statement: http://codeforces.com/contest/965/problem/D
 * Two pointer sliding window solution
 */
public class SingleUseStones {
    public static void main(final String args[]) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line[] = bufferedReader.readLine().split(" ");
        final int width = Integer.parseInt(line[0]), L = Integer.parseInt(line[1]);
        line = bufferedReader.readLine().split(" ");
        final int river[] = new int[line.length];
        for (int i = 0; i < river.length; i++) {
            river[i] = Integer.parseInt(line[i]);
        }
        // Solution starts here
        int sum = 0;
        for (int i = 0; i < L; i++) {
            sum += river[i];
        }
        int minValue = sum;
        for (int i = L; i < width - 1; i++) {
            sum = sum - river[i - L] + river[i];
            minValue = Math.min(minValue, sum);
        }
        System.out.println(minValue);
    }
}
