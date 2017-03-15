package main.java.codechef;

public class INTERVAL {
    public static void main(String args[]) throws Exception {
        int N = 0, M = 0, B[] = new int[M + 1];
        final long cumulative[] = new long[N + 1];
        final long dp[][] = new long[0][0];
        for (int turn = M; turn >= 1; turn--) {
            final long range[] = new long[N + 1];
            for (int L1 = turn; L1 + B[turn] <= N + 2 - turn; L1++) {
                final int R1 = L1 + B[turn] - 1;
                final long score = cumulative[R1] - cumulative[L1 - 1];
                final long next = dp[turn + 1][L1 + 1];
                range[L1] = score - next;
            }
            int rear = 0, front = 1;
            final int queue[] = new int[N + 1];
            final int validMoves = B[turn - 1] - B[turn] - 1;
            for (int left = turn; left <= N + 1 - turn; left++) {
                while (rear != front - 1 && queue[rear + 1] <= left - validMoves) {
                    rear++;
                }
                while (rear != front - 1 && range[left] >= range[queue[front - 1]]) {
                    front--;
                }
                queue[front++] = left;
                final int L1 = left - validMoves + 1;
                if (L1 >= turn) {
                    dp[turn][L1] = range[queue[rear + 1]];
                }
            }
            long ans = dp[1][1];
        }
    }
}