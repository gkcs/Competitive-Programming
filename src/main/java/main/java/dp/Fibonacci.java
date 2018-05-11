package main.java.dp;

import java.util.Scanner;

public class Fibonacci {
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final int n = scanner.nextInt();
        //Fib sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21
        System.out.println(fibonacciDP(n));
        System.out.println(fibonacciRecursive(n));
        System.out.println(fibonacciMatrix(n));
    }

    private static long fibonacciRecursive(final int n) {
        if (n < 2) {
            if (n == 0) {
                return 0;
            } else {
                return 1;
            }
        }
        return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2);
    }

    private static long fibonacciDP(final int n) {
        if (n < 2) {
            if (n == 0) {
                return 0;
            } else {
                return 1;
            }
        }
        long first = 0, second = 1;
        for (int i = 0; i < n; i++) {
            final long temp = first;
            first = second;
            second = second + temp;
        }
        return first;
    }

    private static long fibonacciMatrix(int n) {
        if (n < 2) {
            if (n == 0) {
                return 0;
            } else {
                return 1;
            }
        }
        n--;
        long product[][] = new long[][]{{1, 0}, {0, 1}}, M[][] = new long[][]{{1, 1}, {1, 0}};
        while (n > 0) {
            if ((n & 1) == 1) {
                product = matrixMult(product, M);
            }
            M = matrixMult(M, M);
            n = n >> 1;
        }
        return product[1][0] + product[1][1];
    }

    private static long[][] matrixMult(final long[][] first, final long[][] second) {
        final long f00 = first[0][0] * second[0][0] + first[0][1] * second[1][0],
                f01 = first[0][0] * second[0][1] + first[0][1] * second[1][1],
                f10 = first[1][0] * second[0][0] + first[1][1] * second[1][0],
                f11 = first[1][0] * second[0][1] + first[1][1] * second[1][1];
        return new long[][]{{f00, f01}, {f10, f11}};
    }
}
