package main.java.codechef;

import java.util.Arrays;
import java.util.Random;

public class BruteForceSolver {
    private final int[][][] w;
    private final int[] starts;
    private final int[] ends;
    private final int[][] cycleLengths;

    public BruteForceSolver(int n, int[][] weights, final int[] cycleWeights, final int[] starts, int[] ends) {
        w = getWeights(weights, n);
        cycleLengths = getCycleLengths(n, w, cycleWeights, starts, ends);
        this.starts = starts;
        this.ends = ends;
    }

    public static void main(String[] args) {
        final Random random = new Random();
        int n = random.nextInt(20) + 1;
        int a[] = new int[n];
        int[][] weights = new int[n][];
        int[] starts = new int[n];
        int[] ends = new int[n];
        int[] cycleWeights = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt(20) + 1;
            weights[i] = new int[a[i]];
            for (int j = 0; j < a[i]; j++) {
                weights[i][j] = random.nextInt(20) + 1;
            }
            starts[i] = random.nextInt(a[i]);
            ends[i] = random.nextInt(a[i]);
            cycleWeights[i] = random.nextInt(20) + 1;
        }
    }

    public int solve(int v1, int v2, int c1, int c2) {
        if (c1 > c2) {
            int temp = v1;
            v1 = v2;
            v2 = temp;
            temp = c1;
            c1 = c2;
            c2 = temp;
        }
        final int cycleDistance1 = Math.abs(cycleLengths[c1][0] - cycleLengths[c2][0]),
                excess = findDistance(starts[c1], ends[c1], w[c1]),
                first1 = findDistance(v1, ends[c1], w[c1]),
                second1 = findDistance(starts[c2], v2, w[c2]);
        final int cycleDistance2 = Math.abs(cycleLengths[c1][1] - cycleLengths[c2][1]),
                first2 = findDistance(v1, starts[c1], w[c1]),
                second2 = findDistance(ends[c2], v2, w[c2]);
        final int possibilityOne = cycleDistance1 - excess + first1 + second1;
        final int possibilityTwo = cycleDistance2 - excess + first2 + second2;
        return Math.min(possibilityOne, possibilityTwo);
    }

    public int[][] getCycleLengths(final int n, final int[][][] w, final int[] cycleWeights, final int[] starts, final int[] ends) {
        final int[][] lengths = new int[n][2];
        final int[] distances = new int[n];
        for (int i = 0; i < n; i++) {
            distances[i] = findDistance(starts[i], ends[i], w[i]);
        }
        for (int j = 1; j < n; j++) {
            lengths[j][0] = lengths[j - 1][0] + distances[j - 1] + cycleWeights[j - 1];
        }
        for (int j = n - 1; j > 0; j--) {
            lengths[j][1] = lengths[(j + 1) % n][1] + distances[(j + 1) % n] + cycleWeights[j];
        }
        return lengths;
    }

    public int[][][] getWeights(final int weights[][], final int n) {
        final int w[][][] = new int[n][][];
        for (int i = 0; i < n; i++) {
            final int size = weights[i].length;
            w[i] = new int[size][2];
            for (int j = 1; j < size; j++) {
                w[i][j][0] = w[i][j - 1][0] + weights[i][j - 1];
            }
            w[i][size - 1][1] = weights[i][size - 1];
            for (int j = size - 2; j > 0; j--) {
                w[i][j][1] = w[i][j + 1][1] + weights[i][j];
            }
        }
        return w;
    }

    private int findDistance(final int start, final int end, final int[][] weights) {
        final int clockwise = Math.abs(weights[start][0] - weights[end][0]);
        final int antiClockwise = weights[weights.length - 1][1] + weights[weights.length - 1][0] - clockwise;
        return clockwise > antiClockwise ? antiClockwise : clockwise;
    }
}