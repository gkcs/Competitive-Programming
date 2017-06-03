package main.java.codechef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class SNELECT {
    public static void main(String[] args) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final StringBuilder sb = new StringBuilder();
        final Solver solver = new Solver();
        for (int t = Integer.parseInt(in.readLine()); t > 0; t--) {
            final String[] first = in.readLine().split(" ");
            final int n = Integer.parseInt(first[0]), m = Integer.parseInt(first[1]);
            final int a[][] = new int[n][m];
            final int max[][] = new int[n * m][2];
            int length = 0, maximum = 0;
            for (int i = 0; i < n; i++) {
                final String[] row = in.readLine().split(" ");
                for (int j = 0; j < m; j++) {
                    a[i][j] = Integer.parseInt(row[j]);
                    if (maximum < a[i][j]) {
                        maximum = a[i][j];
                        length = 0;
                        max[length][0] = i;
                        max[length][1] = j;
                        length++;
                    } else if (maximum == a[i][j]) {
                        max[length][0] = i;
                        max[length][1] = j;
                        length++;
                    }
                }
            }
            sb.append(solver.solve(max, length, n, m)).append('\n');
        }
        System.out.println(sb.toString());
    }
}

class Solver {

    public int solve(final int q[][], int rear, final int n, final int m) {
        final boolean[][] visited = new boolean[n][m];
        for (int i = 0; i < rear; i++) {
            visited[q[i][0]][q[i][1]] = true;
        }
        int count = 0;
        int front = 0;
        while (front < rear) {
            final int element[] = q[front++];
            final int row = element[0];
            final int col = element[1];
            final int oldRear = rear;
            if (row > 0) {
                if (col > 0) {
                    if (!visited[row - 1][col - 1]) {
                        q[rear++] = new int[]{row - 1, col - 1};
                        visited[row - 1][col - 1] = true;
                    }
                }
                if (!visited[row - 1][col]) {
                    q[rear++] = new int[]{row - 1, col};
                    visited[row - 1][col] = true;
                }
                if (col < m - 1) {
                    if (!visited[row - 1][col + 1]) {
                        q[rear++] = new int[]{row - 1, col + 1};
                        visited[row - 1][col + 1] = true;
                    }
                }
            }
            if (row < n - 1) {
                if (col > 0) {
                    if (!visited[row + 1][col - 1]) {
                        q[rear++] = new int[]{row + 1, col - 1};
                        visited[row + 1][col - 1] = true;
                    }
                }
                if (!visited[row + 1][col]) {
                    q[rear++] = new int[]{row + 1, col};
                    visited[row + 1][col] = true;
                }
                if (col < m - 1) {
                    if (!visited[row + 1][col + 1]) {
                        q[rear++] = new int[]{row + 1, col + 1};
                        visited[row + 1][col + 1] = true;
                    }
                }
            }
            if (col > 0) {
                if (!visited[row][col - 1]) {
                    q[rear++] = new int[]{row, col - 1};
                    visited[row][col - 1] = true;
                }
            }
            if (col < m - 1) {
                if (!visited[row][col + 1]) {
                    q[rear++] = new int[]{row, col + 1};
                    visited[row][col + 1] = true;
                }
            }
            if (oldRear != rear) {
                System.out.println(Arrays.deepToString(q));
                count++;
            }
        }
        return count;
    }
}