package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class ChristmasSquares {

    private static final int TIME_OUT = 4000;

    public static void main(String[] args) throws IOException {
        final int n = Integer.parseInt(new BufferedReader(new InputStreamReader(
                System.in)).readLine());
        System.out.println(display(new MaximizeSquares(n).maximize(n, TIME_OUT)));
    }

    private static String display(final int[] solution) {
        return Arrays.stream(solution).mapToObj(String::valueOf).collect(Collectors.joining(" "));
    }
}

class MaximizeSquares {
    private final long squares[];
    private final Random random = new Random();

    MaximizeSquares(int n) {
        this.squares = new long[n / 2 + 1];
        long x = n / 2;
        for (int i = 0; i < squares.length; i++, x++) {
            squares[i] = x * x;
        }
    }

    private int[] improveRandomly(final int a[], final int timeOut) {
        final long startTime = System.currentTimeMillis();
        int squareIndex = 0;
        int mutableAfter = 0;
        long sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
            if (Arrays.binarySearch(squares, sum) >= 0) {
                mutableAfter = squareIndex;
                squareIndex = i;
            }
        }
        mutableAfter++;
        final long sums[] = new long[a.length - mutableAfter];
        for (int i = 0; i <= mutableAfter; i++) {
            sums[0] += a[i];
        }
        for (int i = 1; i < sums.length; i++) {
            sums[i] = sums[i - 1] + a[i];
        }
        int maxScore = 1;
        final int b[] = new int[sums.length];
        System.arraycopy(a, mutableAfter, b, 0, b.length);
        while (System.currentTimeMillis() < startTime + timeOut) {
            final int first = random.nextInt(b.length);
            final int second = random.nextInt(b.length);
            if (first == second) {
                continue;
            }
            final int temp = b[first];
            b[first] = b[second];
            b[second] = temp;
            int currentScore = 0;
            for (int j = 1; j < b.length; j++) {
                sums[j] = sums[j - 1] + b[j];
                if (Arrays.binarySearch(squares, sums[j]) >= 0) {
                    currentScore++;
                }
            }
            if (currentScore > maxScore) {
                maxScore = currentScore;
                System.arraycopy(b, 0, a, mutableAfter, b.length);
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