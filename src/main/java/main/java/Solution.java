package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Solution {
    public static void main(String[] args) throws IOException {
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        final StringBuilder stringBuilder = new StringBuilder();
        final int t = 1;
        final Solution solution = new Solution();
        for (int test = 0; test < t; test++) {
            final long a[] = new long[Integer.parseInt(inputReader.readLine())];
            final String[] split = inputReader.readLine().split(" ");
            for (int i = 0; i < split.length; i++) {
                a[i] = Integer.parseInt(split[i]);
            }
            stringBuilder.append(solution.getAnswer(a)).append('\n');
        }
        System.out.println(stringBuilder);
    }

    public long getAnswer(final long[] a) {
        Arrays.sort(a);
        final long powersOf2[] = new long[a.length];
        final long mod = 1000000007;
        powersOf2[0] = 1;
        for (int i = 1; i < a.length; i++) {
            powersOf2[i] = (powersOf2[i - 1] << 1) % mod;
        }
        long ans = 0;
        for (int i = 0; i < a.length; i++) {
            ans = (ans + (powersOf2[i] - powersOf2[a.length - i - 1]) * a[i]) % mod;
        }
        return ans;
    }
}