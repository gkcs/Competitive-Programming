package main.java.dp;

import java.util.Arrays;

public class LongestRepeatingSubsequence {
    static int findLongestRepeatingSubSeq(String str) {
        int n = str.length();
        int[][] dp = new int[n + 1][n + 1];
        for (int x = 1; x <= n; x++) {
            for (int y = 1; y <= n; y++) {
                if (str.charAt(x - 1) == str.charAt(y - 1) && x != y)
                    dp[x][y] = 1 + dp[x - 1][y - 1];
                else
                    dp[x][y] = Math.max(dp[x][y - 1], dp[x - 1][y]);
            }
        }
        for (final int[] row : dp) {
            System.out.println(Arrays.toString(row));
        }
        return dp[n][n];
    }

    public static void main(String[] args) {
        System.out.println(findLongestRepeatingSubSeq("aabpqrarqa"));
    }
}