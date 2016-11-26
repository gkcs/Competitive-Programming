package main.java;

import main.java.codechef.Solver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

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
    }

    @Test
    public void randomTests() {
        final Random random = new Random();
        final int n = 4;// random.nextInt(11) + 1;
        final SegmentTree segmentTree = new SegmentTree(n);
        final Solver bruteForceSolver = new Solver(n);
        final int q[][] = new int[100][3];
        for (int i = 0; i < q.length; i++) {
            q[i][0] = Math.random() > 0.5 ? 0 : 1;
            q[i][1] = random.nextInt(n);
            q[i][2] = random.nextInt(n - q[i][1]) + q[i][1];
        }
        System.out.println(n);
        for (final int[] query : q) {
            System.out.println(Arrays.toString(query));
            System.out.println(segmentTree.toString());
            System.out.println(bruteForceSolver.toString());
            if (query[0] == 0) {
                segmentTree.updateTree(query[1] + 1, query[2] + 1);
                bruteForceSolver.update(query[1], query[2]);
            } else {
                assertEquals(bruteForceSolver.query(query[1], query[2]), segmentTree.handleQuery(query[1] + 1,
                                                                                                 query[2] + 1));
            }
        }
    }


/*
4 7
1 0 3
0 1 2
1 0 1
1 0 0
0 0 3
1 0 3
1 3 3
 */

/*
4 2
0 0 3
0 0 0
 */
}
