package main.java.dp;

public class LongestIncreasingSubsequence {
    private static int lis(int arr[], int n) {
        int dp[] = new int[n];
        int x, y, max = 0;
        for (x = 0; x < n; x++) {
            dp[x] = 1;
        }
        for (x = 1; x < n; x++) {
            for (y = 0; y < x; y++) {
                if (arr[x] > arr[y] && dp[x] < dp[y] + 1) {
                    dp[x] = dp[y] + 1;
                }
            }
        }
        for (x = 0; x < n; x++) {
            if (max < dp[x]) {
                max = dp[x];
            }
        }
        return max;
    }

    public static void main(String args[]) {
        int arr[] = {10, 22, 9, 33, 21, 50, 41, 60};
        System.out.println("LIS: " + lis(arr, arr.length));
    }
}
