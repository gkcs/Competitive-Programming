package main.java;

import java.util.Arrays;

public class SegmentTree {
    private static final int mod = 1000000007;
    private final int a[][][];
    private int digit, n;
    private long powers[] = new long[200000];

    private void populatePowers() {
        for (int i = 0; i < powers.length; i++) {
            powers[i] = pow(i + 1);
        }
    }

    public SegmentTree(int n) {
        this.n = n;
        digit = 0;
        while (n > (1 << digit)) {
            digit++;
        }
        a = new int[1 << (digit + 1)][][];
        buildTree(1);
        populatePowers();
    }

    private int[][] findResult(int node, int left, int right, int gleft, int gright) {
        if (left <= right) {
            if (gleft >= left && gright <= right) {
                return a[node];
            } else if ((left >= gleft && gright >= left) || (right >= gleft && gright >= right)) {
                return frequency(findResult(node << 1, left, right, gleft, (gright + gleft) / 2),
                                 findResult((node << 1) + 1, left, right, ((gright + gleft) / 2) + 1, gright));
            }
        }
        return new int[0][0];
    }

    private long product(int[][] frequencies) {
        long prod = 1;
        int length = frequencies[frequencies.length - 1][0] == -1 ? frequencies[frequencies.length - 1][1] : frequencies.length;
        for (int i = 0; i < length; i++) {
            prod = (prod * powers[frequencies[i][1]]) % mod;
        }
        return prod;
    }

    private long pow(int mult) {
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

    private int[][] buildTree(int node) {
        if (node >= (1 << digit) + n) {
            return (a[node] = new int[0][0]);
        } else if (node >= (1 << digit)) {
            return (a[node] = new int[][]{{0, 1}});
        } else {
            a[node] = frequency(buildTree(node << 1), buildTree((node << 1) + 1));
            return a[node];
        }
    }

    private int[][] frequency(int a[][], int b[][]) {
        if (a.length == 0) {
            return b;
        } else if (b.length == 0) {
            return a;
        }
        int firstSize = a[a.length - 1][0] == -1 ? a[a.length - 1][1] : a.length;
        int secondSize = b[b.length - 1][0] == -1 ? b[b.length - 1][1] : b.length;
        return frequency(a, b, new int[firstSize + secondSize][2]);
    }

    private int[][] frequency(int a[][], int b[][], int result[][]) {
        int count = 0;
        int i = 0, j = 0;
        if (a.length == 0) {
            return b;
        } else if (b.length == 0) {
            return a;
        }
        int firstSize = a[a.length - 1][0] == -1 ? a[a.length - 1][1] : a.length;
        int secondSize = b[b.length - 1][0] == -1 ? b[b.length - 1][1] : b.length;
        while (i < firstSize && j < secondSize) {
            if (a[i][0] > b[j][0]) {
                result[count][0] = b[j][0];
                result[count][1] = b[j][1];
                count++;
                j++;
            } else if (a[i][0] < b[j][0]) {
                result[count][0] = a[i][0];
                result[count][1] = a[i][1];
                count++;
                i++;
            } else {
                result[count][0] = a[i][0];
                result[count][1] = a[i][1] + b[j][1];
                count++;
                i++;
                j++;
            }
        }
        while (i < firstSize) {
            result[count][0] = a[i][0];
            result[count][1] = a[i][1];
            count++;
            i++;
        }
        while (j < secondSize) {
            result[count][0] = b[j][0];
            result[count][1] = b[j][1];
            count++;
            j++;
        }
        if (count < result.length) {
            result[result.length - 1][0] = -1;
            result[result.length - 1][1] = count;
        }
        return result;
    }

    public void updateTree(int index, int value) {
        update((1 << digit) + index - 1, value);
    }

    private void update(int index, int value) {
        if (index > 0) {
            if ((index >= 1 << digit)) {
                a[index] = new int[][]{{value, 1}};
            } else {
                frequency(a[index << 1], a[(index << 1) + 1], a[index]);
            }
            update(index >> 1, value);
        }
    }

    public long handleQuery(int l, int r) {
        return product(findResult(1, (1 << digit) + l - 1, (1 << digit) + r - 1, 1 << digit, (1 << (digit + 1)) - 1));
    }


    @Override
    public String toString() {
        return "SegmentTree{" +
                "a=" + Arrays.deepToString(a) +
                '}';
    }
}