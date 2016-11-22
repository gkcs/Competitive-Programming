package main.java;

import org.junit.Test;

public class MainTest {
    @Test
    public void test() {
        SqrtDecomposition decomposition = new SqrtDecomposition(8);
        final int q[][] = new int[][]{
                {1, 3, 6},
                {2, 2},
                {2, 8},
                {2, 7},
                {2, 6},
                {1, 1, 6},
                {2, 6},
                {2, 1}};
        for (final int[] query : q) {
            if (query[0] == 1) {
                decomposition.update(query[1], query[2]);
            } else {
                System.out.println(decomposition.query(query[1]));
            }
        }
/*
8 8
1 3 6
2 2
2 8
2 7
2 6
1 1 6
2 6
2 1
 */
    }
}
