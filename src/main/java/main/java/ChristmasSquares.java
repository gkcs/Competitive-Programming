package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ChristmasSquares {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(display(new MaximizeSquares().maximize(Integer.parseInt(bufferedReader.readLine()))));
    }

    private static String display(int[] solution) {
        return Arrays.stream(solution).mapToObj(String::valueOf).collect(Collectors.joining(" "));
    }
}

class MaximizeSquares {

    public int[] maximize(final int n) {
        final long squares[] = new long[n + 1];
        final boolean touched[] = new boolean[squares.length];
        for (int i = 0; i <= n; i++) {
            squares[i] = i * i;
        }
        final int[] a = new int[n];
        int count;
        long currentSum = 0;
        for (count = 1; count <= Math.sqrt(n); count++) {
            a[count - 1] = (int) (squares[count] - currentSum);
            currentSum += a[count - 1];
            touched[a[count - 1]] = true;
        }
        for (int i = 1; i <= n; i++) {
            if (!touched[i]) {
                a[count - 1] = i;
                count++;
            }
        }
        return a;
    }
}