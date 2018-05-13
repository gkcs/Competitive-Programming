package main.java.videos;

import main.java.InputReader;

import java.math.BigInteger;
import java.util.Arrays;

public class GiantChessBoard {
    private static long mod = (long) (1e9 + 7);
    private static long[] fact = generateFactorials(1000000);
    private static long[] invFact = generateReverseFactorials(1000000);

    public static void main(final String[] args) {
        final InputReader in = new InputReader(System.in);
        final int n = in.readInt();
        final int m = in.readInt();
        final int k = in.readInt();
        final Point point[] = new Point[k];
        for (int i = 0; i < k; i++) {
            point[i] = new Point(in.readInt(), in.readInt());
        }
        Arrays.sort(point);
        final int[] x = new int[k + 1];
        final int[] y = new int[k + 1];
        for (int i = 0; i < k; i++) {
            x[i] = point[i].x;
            y[i] = point[i].y;
        }
        x[k] = n;
        y[k] = m;
        final long[] dp = new long[k + 1];
        for (int i = 0; i <= k; i++) {
            dp[i] = comb(x[i] - 1, x[i] - 1 + y[i] - 1);
            for (int j = 0; j < i; j++) {
                if (y[j] <= y[i]) {
                    dp[i] -= dp[j] * comb(x[i] - x[j], x[i] - x[j] + y[i] - y[j]) % mod;
                }
            }
            dp[i] = ((dp[i] % mod) + mod) % mod;
        }
        long result = dp[k];
        System.out.println(result);
    }

    private static long comb(final int m, final int n) {
        return (((fact[n] * invFact[m]) % mod) * invFact[n - m]) % mod;
    }

    public static long[] generateFactorials(int count) {
        long[] result = new long[count];
        result[0] = 1;
        for (int i = 1; i < count; i++) {
            result[i] = (result[i - 1] * i) % mod;
        }
        return result;
    }

    public static long[] generateReverseFactorials(int upTo) {
        final long[] reverseFactorials = new long[upTo];
        reverseFactorials[0] = reverseFactorials[1] = 1;
        final BigInteger BIG_MOD = BigInteger.valueOf(mod);
        for (int i = 1; i < upTo; i++) {
            reverseFactorials[i] = (BigInteger.valueOf(i).modInverse(BIG_MOD).longValue() * reverseFactorials[i - 1]) % mod;
        }
        return reverseFactorials;
    }
}

class Point implements Comparable<Point> {
    final int x, y;

    Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(final Point other) {
        if (x < other.x) {
            return -1;
        } else if (x > other.x) {
            return 1;
        }
        return this.y - other.y;
    }
}