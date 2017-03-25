package main.java.codechef;

import main.java.InputReader;

/**
 * The INTERVAL problem from FEB-17 CodeChef is solved here
 */
public class INTERVAL {

    public static void main(String[] args) {
        final InputReader in = new InputReader(System.in);
        final StringBuilder sb = new StringBuilder();
        for (int t = in.readInt(); t > 0; t--) {
            final int n = in.readInt();
            final int m = in.readInt();
            final int a[] = new int[n];
            final int b[] = new int[m];
            final long precomputedSum[] = new long[n + 1];
            for (int i = 0; i < n; i++) {
                a[i] = in.readInt();
                precomputedSum[i + 1] = precomputedSum[i] + a[i];
            }
            for (int i = 0; i < m; i++) {
                b[i] = in.readInt();
            }
            final long dp[] = new long[n];
            long ans = 0;
            for (int i = 0; i <= n - b[m - 1]; i++) {
                dp[i] = precomputedSum[i + b[m - 1]] - precomputedSum[i];
                if (m == 1) {
                    ans = Math.max(ans, dp[i]);
                }
            }
            for (int move = m - 2; move >= 0; move--) {
                int deque[] = new int[n];
                int front = 0, rear = -1;
                int leftIndex;
                //Standard Deque Implementation
                for (leftIndex = move + 1; leftIndex < move + b[move] - b[move + 1]; leftIndex++) {
                    //If the element at rear is lesser, kick them out
                    while (front <= rear && dp[leftIndex] >= dp[deque[rear]]) {
                        rear--;
                    }
                    //Add the index of the greater element to the deque
                    deque[++rear] = leftIndex;
                }
                for (int current = move; current <= n - move - b[move]; current++, leftIndex++) {
                    dp[current] = precomputedSum[current + b[move]] - precomputedSum[current] - dp[deque[front]];
                    if (move == 0) {
                        ans = Math.max(ans, dp[current]);
                    }
                    //Remove invalid/obsolete elements from the front
                    while (front <= rear && deque[front] <= current + 1) {
                        front++;
                    }
                    //Remove smaller elements from rear
                    while (front <= rear && dp[leftIndex] >= dp[deque[rear]]) {
                        rear--;
                    }
                    deque[++rear] = leftIndex;
                }
            }
            sb.append(ans).append("\n");
        }
        System.out.println(sb.toString());
    }
}