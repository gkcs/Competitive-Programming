package main.java.dp;

public class OptimalBST {
    static int optimalSearchTree(final int freq[], final int n) {
        final int cost[][] = new int[n + 1][n + 1];
        final int prefixSums[] = new int[n];
        prefixSums[0] = freq[0];
        for (int i = 1; i < n; i++) {
            prefixSums[i] = prefixSums[i - 1] + freq[i];
        }
        for (int i = 0; i < n; i++) {
            cost[i][i] = freq[i];
        }
        for (int lengthOfBlock = 2; lengthOfBlock <= n; lengthOfBlock++) {
            for (int start = 0; start <= n - lengthOfBlock + 1; start++) {
                final int end = start + lengthOfBlock - 1;
                cost[start][end] = Integer.MAX_VALUE;
                for (int root = start; root <= end; root++) {
                    final int currentCost = ((root > start) ? cost[start][root - 1] : 0)
                            + ((root < end) ? cost[root + 1][end] : 0) +
                            prefixSums[Math.min(end, prefixSums.length - 1)] - (start > 0 ? prefixSums[start - 1] : 0);
                    if (currentCost < cost[start][end]) {
                        cost[start][end] = currentCost;
                    }
                }
            }
        }
        return cost[0][n - 1];
    }

    public static void main(String[] args) {
        int keys[] = {10, 12, 20};
        int freq[] = {34, 8, 50};
        int n = keys.length;
        System.out.println("Optimal BST cost " + optimalSearchTree(freq, n));
    }
}