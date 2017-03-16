package main.java.codechef;

import main.java.InputReader;

/**
 * The INTERVAL problem from FEB-17 CodeChef is solved here
 */
public class INTERVAL {

    public static void main(String args[]) throws Exception {
        final InputReader re = new InputReader(System.in);
        final StringBuilder stringBuilder = new StringBuilder();
        for (int t = re.readInt(); t > 0; t--) {
            final int N = re.readInt(), M = re.readInt();
            final int A[] = new int[N];
            for (int i = 0; i < N; i++) {
                A[i] = re.readInt();
            }
            final int[] B = new int[M + 1];
            B[0] = N + 2;
            for (int i = 1; i <= M; i++) {
                B[i] = re.readInt();
            }
            final long precomputedSums[] = new long[N + 1];
            //Pre-compute all range sums
            for (int i = 0; i < N; i++) {
                precomputedSums[i + 1] = A[i] + precomputedSums[i];
            }
            final long dp[][] = new long[M + 2][N + 1];
            dp[M + 1] = new long[N + 2];
            final int deque[] = new int[N + 1];
            int front, rear;
            //For each move starting from last move
            for (int move = M; move >= 1; move--) {
                final long range[] = new long[N + 1];
                for (int leftIndex = move; leftIndex + B[move] <= N + 2 - move; leftIndex++) {
                    //For every possible range that you can take, compute its value
                    range[leftIndex] = precomputedSums[leftIndex + B[move] - 1] - precomputedSums[leftIndex - 1] - dp[move + 1][leftIndex + 1];
                }
                front = 0;
                rear = 1;
                final int validMoves = B[move - 1] - B[move] - 1;
                for (int i = move; i <= N + 1 - move; i++) {
                    while (front < rear - 1 && deque[front + 1] <= i - validMoves) {
                        front++;
                    }
                    while (front < rear - 1 && range[i] >= range[deque[rear - 1]]) {
                        rear--;
                    }
                    deque[rear++] = i;
                    final int L1 = i - validMoves + 1;
                    if (L1 >= move) {
                        dp[move][L1] = range[deque[front + 1]];
                    }
                }
            }
            stringBuilder.append(dp[1][1]);
        }
        System.out.println(stringBuilder);
    }
}