package main.java.dp;

public class LongestCommonSubstring {

    private static int findLCS(char A[], char B[], int m, int n) {
        int dp[][] = new int[m + 1][n + 1];
        int result = 0;
        for (int x = 1; x <= m; x++) {
            for (int y = 1; y <= n; y++) {
                if (A[x - 1] == B[y - 1]) {
                    dp[x][y] = dp[x - 1][y - 1] + 1;
                    result = Integer.max(result, dp[x][y]);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        final String a = "www.gkcs.tech", b = "www.youtube.gkcs";
        System.out.println("Length of Longest Common Substring is "
                + findLCS(a.toCharArray(), b.toCharArray(), a.length(), b.length()));
    }
}