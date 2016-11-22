package main.java;

import java.math.BigInteger;

public class BruteForceSolver {
    private final int a[];
    private final int frequencies[][];
    private long powers[] = new long[1000];
    private long invPowers[] = new long[1000];
    private static final int mod = 1000000007;
    private static final BigInteger modo = BigInteger.valueOf(mod);

    private void populatePowers() {
        for (int i = 0; i < powers.length; i++) {
            powers[i] = pow(i + 1);
            invPowers[i] = BigInteger.valueOf(powers[i]).modInverse(modo).longValue();
        }
    }

    public static long pow(int mult) {
        long result = 1;
        long exponent = mult;
        if ((mult & 1) != 0) {
            result = mult;
        }
        for (int bit = 1; bit < 32; bit++) {
            exponent = (exponent * exponent) % mod;
            if ((mult & (1 << bit)) != 0) {
                result = (result * exponent) % mod;
            }
        }
        return result;
    }

    public BruteForceSolver(int MAX_NUMBER, int n) {
        a = new int[n];
        frequencies = new int[MAX_NUMBER][n];
        for (int i = 0; i < n; i++) {
            frequencies[0][i] = i + 1;
        }
        populatePowers();
    }

    void update(int index, int value) {
        index--;
        for (int i = index; i < a.length; i++) {
            frequencies[a[index]][i]--;
            frequencies[value][i]++;
        }
        a[index] = value;
    }

    long query(int index) {
        index--;
        long result = 1;
        for (int[] frequency : frequencies) {
            result = (result * powers[frequency[index]]) % mod;
        }
        return result;
    }
}
