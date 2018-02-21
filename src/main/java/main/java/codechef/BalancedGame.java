package main.java.codechef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.Integer.parseInt;

public class BalancedGame {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final StringBuilder stringBuilder = new StringBuilder();
        for (int t = parseInt(bufferedReader.readLine()); t > 0; t--) {
            String line[] = bufferedReader.readLine().split(" ");
            final int n = parseInt(line[0]), cMax = parseInt(line[1]), hMax = parseInt(line[2]);
            final long seeds[][] = new long[2][n];
            for (int i = 0; i < n; i++) {
                line = bufferedReader.readLine().split(" ");
                seeds[1][i] = Long.parseLong(line[0]);
                seeds[2][i] = Long.parseLong(line[1]);
            }
        }
        System.out.print(stringBuilder);
    }
}