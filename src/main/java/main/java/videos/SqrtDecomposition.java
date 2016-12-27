package main.java.videos;

import java.util.Arrays;

/**
 * Square root decomposition allows us to answer queries in sqrt(N) time. As the implementation of these structures
 * is usually simpler than a segment tree, they are a useful tool in a programmer's arsenal.
 */
public class SqrtDecomposition {
    private final int a[];
    private final long blockSums[];
    private final int sqrt;

    public SqrtDecomposition(int input[]) {
        this.sqrt = (int) Math.ceil(Math.sqrt(input.length));
        a = new int[sqrt * sqrt];
        System.arraycopy(input, 0, a, 0, input.length);
        this.blockSums = new long[sqrt];
        for (int i = 0; i < blockSums.length; i++) {
            final int startIndex = i * sqrt;
            for (int j = 0; j < sqrt; j++) {
                blockSums[i] += a[startIndex + j];
            }
        }
    }

    /**
     * @param index The index to be updated
     * @param value The value to set the element at specified index
     */

    public void update(int index, int value) {
        final int blockIndex = index / sqrt;
        blockSums[blockIndex] = blockSums[blockIndex] - a[index] + value;
        a[index] = value;
    }

    /**
     * @param left  The stating index.
     * @param right The ending index.
     * @return The sum from index left to right
     */
    public long query(final int left, final int right) {
        final int startBlockIndex = left / sqrt;
        final int endIBlockIndex = right / sqrt;
        long sum = 0;
        for (int i = startBlockIndex + 1; i < endIBlockIndex; i++) {
            sum += blockSums[i];
        }
        final int startIndex = left % sqrt;
        final int endIndex = right % sqrt;
        for (int i = startIndex; i < sqrt; i++) {
            sum += a[startBlockIndex * sqrt + i];
        }
        for (int i = 0; i <= endIndex; i++) {
            sum += a[endIBlockIndex * sqrt + i];
        }
        return sum;
    }

    @Override
    public String toString() {
        return "SqrtDecomposition{\n" +
                "a=" + Arrays.toString(a) +
                ",\n blockSums=" + Arrays.toString(blockSums) +
                '}';
    }
}

class Main {
    public static void main(String[] args) {
        final SqrtDecomposition sqrtDecomposition = new SqrtDecomposition(new int[]{1, 2, 6, 7, 9, 3, 1, 9});
        System.out.println(sqrtDecomposition);
        System.out.println(sqrtDecomposition.query(2, 6));
        sqrtDecomposition.update(5, 7);
        System.out.println(sqrtDecomposition);
        System.out.println(sqrtDecomposition.query(2, 6));
    }
}

