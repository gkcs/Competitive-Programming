package main.java.dp;

public class LongestCommonSubsequence {
    int lcs(char[] A, char[] B, int m, int n) {
        final int dp[][] = new int[m + 1][n + 1];
        for (int x = 1; x <= m; x++) {
            for (int y = 1; y <= n; y++) {
                if (A[x - 1] == B[y - 1]) {
                    dp[x][y] = dp[x - 1][y - 1] + 1;
                } else {
                    dp[x][y] = Math.max(dp[x - 1][y], dp[x][y - 1]);
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        final char[] A = "AGGTAB".toCharArray(), B = "GXTXAYB".toCharArray();
        System.out.println("LCS: " + new LongestCommonSubsequence().lcs(A, B, A.length, B.length));
    }

}