package main.java;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class MainTest {
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
    }

    @Test
    public void randomTests() {
        Random random = new Random();
        final int n = random.nextInt(11) + 1;
        SqrtDecomposition decomposition = new SqrtDecomposition(n);
        int max_number = 100;
        BruteForceSolver bruteForceSolver = new BruteForceSolver(max_number, n);
        final int q[][] = new int[100][3];
        for (int i = 0; i < q.length; i++) {
            if (Math.random() > 0.5) {
                q[i][0] = 1;
                q[i][1] = random.nextInt(n) + 1;
                q[i][2] = random.nextInt(max_number);
            } else {
                q[i][0] = 2;
                q[i][1] = random.nextInt(n) + 1;
            }
        }
        for (final int[] query : q) {
            if (query[0] == 1) {
                decomposition.update(query[1], query[2]);
                bruteForceSolver.update(query[1], query[2]);
            } else {
                assertEquals(bruteForceSolver.query(query[1]), decomposition.query(query[1]));
            }
        }
    }
}
