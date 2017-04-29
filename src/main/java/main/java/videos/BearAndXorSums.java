package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/*
3
7 5 13
6
11 4 4 13 11 5
6
1 4 6 8 1 9
*/
public class BearAndXorSums {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        //final StringBuilder stringBuilder = new StringBuilder();
        final int n = Integer.parseInt(bufferedReader.readLine());
        final byte a[] = new byte[n];
        final int prefixSums[] = new int[n + 1];
        final String split[] = bufferedReader.readLine().split(" ");
        for (int i = 0; i < n; i++) {
            a[i] = Byte.parseByte(split[i]);
        }
        prefixSums[1] = a[0];
        for (int i = 1; i <= n; i++) {
            prefixSums[i] = prefixSums[i - 1] + a[i - 1];
        }
        final boolean bits[] = new boolean[25];
        final BIT fenwickTree = new BIT(prefixSums[n - 1] + 1);
        for (int b = 0; b < bits.length; b++) {
            final int POWER = 1 << b;
            final int MOD = ((POWER << 1) - 1);
            long total = 0;
            for (final int prefixSum : prefixSums) {
                final int sumModulo2Power = prefixSum & MOD;
                final long firstRange = fenwickTree.query(0, sumModulo2Power - POWER);
                final long secondRange = fenwickTree.query(sumModulo2Power + 1, sumModulo2Power + POWER);
                total += firstRange + secondRange;
                fenwickTree.update(sumModulo2Power, 1);
            }
            bits[b] = (total & 1) != 0;
            for (final int prefixSum : prefixSums) {
                fenwickTree.update(prefixSum & MOD, -1);
            }
        }
        int result = 0;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                result |= 1 << i;
            }
        }
        System.out.println(result);
    }
}

class BIT {
    private final long a[];

    public BIT(final int n) {
        this.a = new long[n + 1];
    }

    public void update(final int index, final int value) {
        int effectiveIndex = index + 1;
        while (effectiveIndex > 0 && effectiveIndex < a.length) {
            a[effectiveIndex] += value;
            effectiveIndex = getPeer(effectiveIndex);
        }
    }

    public long query(final int left, final int right) {
        if (left > right) {
            return 0;
        }
        return query(right + 1) - query(left);
    }

    private long query(int i) {
        if (i <= 0) {
            return 0;
        }
        if (i >= a.length) {
            i = a.length - 1;
        }
        long sum = 0;
        while (i > 0) {
            sum += a[i];
            i = getParent(i);
        }
        return sum;
    }

    private int getPeer(final int effectiveIndex) {
        return effectiveIndex + (~effectiveIndex + 1 & effectiveIndex);
    }

    private int getParent(final int effectiveIndex) {
        return effectiveIndex - (~effectiveIndex + 1 & effectiveIndex);
    }

    @Override
    public String toString() {
        return "BIT{" +
                "a=" + Arrays.toString(a) +
                '}';
    }
}