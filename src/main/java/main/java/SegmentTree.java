package main.java;

import java.util.Arrays;

public class SegmentTree {
    private final long a[];
    private int digit, n;
    private final boolean flipped[];

    public SegmentTree(final int n) {
        this.n = n;
        digit = 0;
        while (n > (1 << digit)) {
            digit++;
        }
        this.a = new long[1 << (digit + 1)];
        buildTree(1);
        flipped = new boolean[this.a.length];
    }

    private long findResult(final int node,
                            final int left,
                            final int right,
                            final int leftMostIndex,
                            final int rightMostIndex) {
        if (left <= right) {
            if (leftMostIndex >= left && rightMostIndex <= right) {
                return a[node];
            } else if ((left >= leftMostIndex && rightMostIndex >= left) || (right >= leftMostIndex && rightMostIndex >= right)) {
                if (flipped[node]) {
                    flip(node << 1, (rightMostIndex - leftMostIndex + 1) >> 1);
                    flip((node << 1) + 1, (rightMostIndex - leftMostIndex + 1) >> 1);
                    flipped[node] = false;
                }
                return findResult(node << 1, left, right, leftMostIndex, (rightMostIndex + leftMostIndex) >> 1)
                        + findResult((node << 1) + 1,
                                     left,
                                     right,
                                     ((rightMostIndex + leftMostIndex) >> 1) + 1,
                                     rightMostIndex);
            }
        }
        return 0;
    }

    private void flip(final int index, final int range) {
        if (flipped[index]) {
            flipped[index] = false;
        } else {
            flipped[index] = true;
            a[index] = range - a[index];
        }
    }

    private long buildTree(final int node) {
        if (node >= (1 << digit)) {
            return a[node];
        } else {
            return a[node] = buildTree(node << 1) + buildTree((node << 1) + 1);
        }
    }

    public void updateTree(final int l, final int r) {
        update(1, (1 << digit) + l - 1, (1 << digit) + r - 1, 1 << digit, (1 << (digit + 1)) - 1);
    }

    private void update(final int node,
                        final int left,
                        final int right,
                        final int leftMostIndex,
                        final int rightMostIndex) {
        if (left <= right) {
            if (leftMostIndex >= left && rightMostIndex <= right) {
                flip(node, rightMostIndex - leftMostIndex + 1);
            } else if ((left >= leftMostIndex && rightMostIndex >= left) || (right >= leftMostIndex && rightMostIndex >= right)) {
                if (flipped[node]) {
                    flip(node << 1, (rightMostIndex - leftMostIndex + 1) >> 1);
                    flip((node << 1) + 1, (rightMostIndex - leftMostIndex + 1) >> 1);
                    flipped[node] = false;
                }
                update(node << 1, left, right, leftMostIndex, (rightMostIndex + leftMostIndex) >> 1);
                update((node << 1) + 1, left, right, ((rightMostIndex + leftMostIndex) >> 1) + 1, rightMostIndex);
            }
        }
        if (node < (1 << digit)) {
            a[node] = a[node << 1] + a[(node << 1) + 1];
        }
    }

    public long handleQuery(final int l, final int r) {
        return findResult(1, (1 << digit) + l - 1, (1 << digit) + r - 1, 1 << digit, (1 << (digit + 1)) - 1);
    }


    @Override
    public String toString() {
        return "SegmentTree{" +
                "a=" + Arrays.toString(a) +
                ", flipped=" + Arrays.toString(flipped) +
                '}';
    }
}

class SegmentTreeMain {
    public static void main(String[] args) {
        final InputReader inputReader = new InputReader(System.in);
        final int n = inputReader.readInt(), q = inputReader.readInt();
        final StringBuilder stringBuilder = new StringBuilder();
        final SegmentTree segmentTree = new SegmentTree(n);
        for (int i = 0; i < q; i++) {
            if (inputReader.readInt() == 0) {
                segmentTree.updateTree(inputReader.readInt() + 1, inputReader.readInt() + 1);
            } else {
                stringBuilder.append(segmentTree.handleQuery(inputReader.readInt() + 1, inputReader.readInt() + 1))
                        .append('\n');
            }
        }
        System.out.println(stringBuilder);
    }
}