package main.java.codechef;

import java.util.Arrays;

public class Solver {
    @Override
    public String toString() {
        return "Solver{" +
                "a=" + Arrays.toString(a) +
                '}';
    }

    private final int a[];

    public Solver(int n) {
        this.a = new int[n];
    }

    public void update(int l, int r) {
        for (int i = l; i <= r; i++) {
            a[i] ^= 1;
        }
    }

    public long query(int l, int r) {
        long sum = 0;
        for (int i = l; i <= r; i++) {
            sum += a[i];
        }
        return sum;
    }
}
