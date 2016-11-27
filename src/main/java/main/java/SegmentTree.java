package main.java;

/**
 * A segment tree is a data structure to perform range operations. The one implemented below can perform both range
 * updates and queries. It is based on the codechef question: <a href="https://www.codechef.com/problems/FLIPCOIN">Flipping Coins.</a>
 */
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

    /**
     * Answers a query for a specified range. A node which entirely falls within the queried range returns its value.
     * Nodes having an intersection with the given range query their children and return the sum of their respective
     * results. Nodes entirely out of range return an EMPTY_VALUE = 0.
     *
     * @param node           Index of node being queried.
     * @param left           Left index of specified Range
     * @param right          Right index of specified Range
     * @param leftMostIndex  Leftmost index that this node is responsible for
     * @param rightMostIndex Rightmost index that this node is responsible for
     * @return Sum of all nodes in the given range
     */
    private long query(final int node,
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
                return query(node << 1, left, right, leftMostIndex, (rightMostIndex + leftMostIndex) >> 1)
                        + query((node << 1) + 1,
                                left,
                                right,
                                ((rightMostIndex + leftMostIndex) >> 1) + 1,
                                rightMostIndex);
            }
        }
        return 0;
    }

    /**
     * Flips a given node. After this operation, the node inverts all coins belonging to it.
     *
     * @param index Node index
     * @param range Node range
     */
    private void flip(final int index, final int range) {
        flipped[index] = !flipped[index];
        a[index] = range - a[index];
    }

    /**
     * Builds the tree in 2*N + N + N/2 + ... + 1 = 4*N => O(N) time.
     *
     * @param node Node index of the subtree being built.
     * @return Sum of all nodes in this subtree.
     */
    private long buildTree(final int node) {
        if (node >= (1 << digit)) {
            return a[node];
        } else {
            return a[node] = buildTree(node << 1) + buildTree((node << 1) + 1);
        }
    }

    /**
     * Method to update a range in the tree
     *
     * @param l Left index of range
     * @param r Right index of range
     */
    public void update(final int l, final int r) {
        update(1, (1 << digit) + l - 1, (1 << digit) + r - 1, 1 << digit, (1 << (digit + 1)) - 1);
    }

    /**
     * Updates all values in a specified range. A node which entirely falls within the queried range updates it's own
     * value and flips itself.
     * Nodes having an intersection with the given range update their children and ensure consistency after doing so.
     * Nodes entirely out of range do nothing.
     *
     * @param node           Index of node being queried.
     * @param left           Left index of specified Range
     * @param right          Right index of specified Range
     * @param leftMostIndex  Leftmost index that this node is responsible for
     * @param rightMostIndex Rightmost index that this node is responsible for
     */
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
                if (node < (1 << digit)) {
                    a[node] = a[node << 1] + a[(node << 1) + 1];
                }
            }
        }
    }

    /**
     * Method to query a range in the tree
     *
     * @param l Left index of range
     * @param r Right index of range
     */
    public long query(final int l, final int r) {
        return query(1, (1 << digit) + l - 1, (1 << digit) + r - 1, 1 << digit, (1 << (digit + 1)) - 1);
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
                segmentTree.update(inputReader.readInt() + 1, inputReader.readInt() + 1);
            } else {
                stringBuilder.append(segmentTree.query(inputReader.readInt() + 1, inputReader.readInt() + 1))
                        .append('\n');
            }
        }
        System.out.println(stringBuilder);
    }
}