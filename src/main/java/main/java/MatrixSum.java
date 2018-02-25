package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MatrixSum {
    public static void main(String[] args) throws IOException {
        int n = 5, m = 5;
        int matrix[][] = new int[n][m];
        int sum[][] = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = i * n + j + 1;
                int sumOfPreviousColumn;
                if (j == 0)
                    sumOfPreviousColumn = 0;
                else
                    sumOfPreviousColumn = sum[i][j - 1];
                int sumOfPreviousRow;
                if (i == 0)
                    sumOfPreviousRow = 0;
                else
                    sumOfPreviousRow = sum[i - 1][j];
                int sumOfDiagonal;
                if (i == 0 || j == 0) {
                    sumOfDiagonal = 0;
                } else {
                    sumOfDiagonal = sum[i - 1][j - 1];
                }
                sum[i][j] = matrix[i][j]
                        + sumOfPreviousColumn
                        + sumOfPreviousRow
                        - sumOfDiagonal;
            }
        }
        for (int i = 0; i < n; i++) {
            System.out.println(Arrays.toString(matrix[i]));
        }
        System.out.println();
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.println(Arrays.toString(sum[i]));
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String s = reader.readLine();
        while (!s.equalsIgnoreCase("Quit")) {
            int r1 = Integer.parseInt(reader.readLine()) - 1,
                    r2 = Integer.parseInt(reader.readLine()) - 1,
                    c1 = Integer.parseInt(reader.readLine()) - 1,
                    c2 = Integer.parseInt(reader.readLine()) - 1;
            System.out.println(sum[r2][c2]
                    - sum[r1 - 1][c2]
                    - sum[r2][c1 - 1]
                    + sum[r1 - 1][c1 - 1]);
            s = reader.readLine();
        }
    }
}