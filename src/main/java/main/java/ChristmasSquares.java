package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Keep a segment tree for each K. The Kth segment tree tells us by how many elements are off by K for a given segment.
 * For a given segment, find the incentive to change it. In case we have a lot of numbers off by X for a given
 * segment, give it max priority. Look to make a swap from its right to left so that X is added to this segment.
 */

public class ChristmasSquares {

    private static final int TIME_OUT = 4000;

    public static void main(String[] args) throws IOException {
        System.out.println(display(new MaximizeSquares().maximize(Integer.parseInt(new BufferedReader(new InputStreamReader(
                System.in)).readLine()), TIME_OUT)));
    }

    private static String display(final int[] solution) {
        return Arrays.stream(solution).mapToObj(String::valueOf).collect(Collectors.joining(" "));
    }
}

class MaximizeSquares {
    private final long squares[];

    MaximizeSquares() {
        this.squares = new long[10001];
        for (int i = 0; i < squares.length; i++) {
            squares[i] = i * (long) i;
        }
    }

    private int[] improveRandomly(final int a[], final int timeOut) {
        final long startTime = System.currentTimeMillis();
        final Random random = new Random();
        long sums[] = new long[a.length];
        sums[0] = a[0];
        int squareIndex = 0;
        for (int i = 1; i < sums.length; i++) {
            sums[i] = sums[i - 1] + a[i];
            if (Arrays.binarySearch(squares, sums[i]) >= 0) {
                squareIndex = i;
            }
        }
        int maxScore = 0;
        final int b[] = new int[a.length];
        System.arraycopy(a, squareIndex, b, squareIndex, b.length - squareIndex);
        while (System.currentTimeMillis() < startTime + timeOut) {
            int currentScore = 0;
            for (int i = 0; i < b.length / 3; i++) {
                final int left = squareIndex + random.nextInt(b.length - squareIndex);
                final int right = squareIndex + random.nextInt(b.length - squareIndex);
                final int temp = b[left];
                b[left] = b[right];
                b[right] = temp;
            }
            for (int j = squareIndex; j < sums.length; j++) {
                sums[j] = sums[j - 1] + b[j];
                if (Arrays.binarySearch(squares, sums[j]) >= 0) {
                    currentScore++;
                }
            }
            if (currentScore > maxScore) {
                System.arraycopy(b, squareIndex, a, squareIndex, b.length - squareIndex);
                maxScore = currentScore;
            }
        }
        return a;
    }

    public int[] maximize(final int n, final int timeOut) {
        final int[] a = new int[n];
        int count = 0;
        for (int x = 1; x <= n; x = x + 2) {
            a[count++] = x;
        }
        final int list[] = new int[n >> 1];
        for (int i = 2; i <= n; i = i + 2) {
            list[(i >> 1) - 1] = i;
        }
        final boolean touched[] = new boolean[n + 1];
        int currentSum = 0;
        int currentIndex = count;
        while (currentSum == 0 && currentIndex < n) {
            final int oldCount = count;
            currentSum = (currentIndex + 1) << 2;
            final int tempTouched[] = new int[n + 1];
            int tempIndex = 0;
            for (int i = list.length - 1; i >= 0; i--) {
                if (!touched[i]) {
                    if (list[i] <= currentSum) {
                        currentSum -= list[i];
                        touched[i] = true;
                        tempTouched[tempIndex++] = i;
                        a[count++] = list[i];
                        if (currentSum == 0) {
                            currentIndex += 2;
                            break;
                        }
                    }
                }
            }
            if (currentSum != 0) {
                for (int i = 0; i < tempIndex; i++) {
                    touched[tempTouched[i]] = false;
                }
                currentIndex += 2;
                count = oldCount;
            }
        }
        for (int i = 0; i < list.length; i++) {
            if (!touched[i]) {
                a[count++] = list[i];
            }
        }
        final long sums[] = new long[a.length];
        sums[0] = a[0];
        for (int i = 1; i < a.length; i++) {
            sums[i] = a[i] + sums[i - 1];
        }
        return improveRandomly(a, timeOut);
    }
}