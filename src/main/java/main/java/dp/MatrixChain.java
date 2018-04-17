package main.java.dp;

import java.util.Arrays;

public class MatrixChain {
    static int MatrixChainOrder(int p[], int n) {
        final int dp[][] = new int[n][n];
        int start, end, partition, blockLength, q;
        for (start = 1; start < n; start++) {
            dp[start][start] = 0;
        }
        for (blockLength = 2; blockLength < n; blockLength++) {
            for (start = 1; start < n - blockLength + 1; start++) {
                end = start + blockLength - 1;
                dp[start][end] = Integer.MAX_VALUE;
                for (partition = start; partition <= end - 1; partition++) {
                    q = dp[start][partition]
                            + dp[partition + 1][end]
                            + p[start - 1] * p[partition] * p[end];
                    if (q < dp[start][end]) {
                        dp[start][end] = q;
                    }
                }
            }
        }
        System.out.println(Arrays.deepToString(dp));
        return dp[1][n - 1];
    }

    public static void main(String args[]) {
        int arr[] = new int[]{7, 2, 1, 4, 5};
        int size = arr.length;

        System.out.println("Minimum number of multiplications is " +
                MatrixChainOrder(arr, size));
    }
}