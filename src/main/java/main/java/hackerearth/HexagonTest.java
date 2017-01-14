package main.java.hackerearth;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class HexagonTest {
    @Test
    public void test() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[6][7];
        game[0][0] = 1;
        game[0][1] = 1;
        game[0][5] = 2;
        game[0][6] = 2;
        game[5][0] = 2;
        game[5][6] = 1;
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
    }

    @Test
    public void doNotJumpLikeAMoron() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {1, 1, 0, 0, 0, 2, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 2, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}
        };
        //minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        assertNotEquals("0 0\n1 1", s);
    }

    @Test
    public void doNotJumpLikeAMoron2() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {1, 0, 0, 0, 0, 2, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 1, 1}
        };
        //minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        assertNotEquals("0 0\n2 0", s);
    }

    @Test
    public void dontGoBeserk() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {2, 2, 2, 2, 0, 0, 2},
                {0, 2, 2, 2, 2, 0, 0},
                {2, 2, 2, 2, 2, 0, 0},
                {2, 1, 2, 2, 2, 2, 0},
                {1, 2, 2, 1, 2, 1, 0},
                {2, 2, 1, 1, 1, 1, 0}
        };
        //minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        assertNotEquals("3 1\n1 0", s);
    }

    @Test
    public void learnToAcceptThings() {
        Board.setThoseWithinSight();
        final int[][] game = new int[][]{
                {2, 2, 2, 2, 1, 1, 2},
                {1, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2},
                {2, 1, 2, 2, 2, 2, 2},
                {1, 2, 2, 1, 2, 1, 1},
                {2, 2, 1, 1, 1, 1, 2}
        };
        //minMax.setTest(true);
        final Board board = new Board(game);
        assertTrue(board.isTerminated(1));
        assertTrue(board.isTerminated(2));
        System.out.println(board.heuristicValue(1));
        System.out.println(board.heuristicValue(2));
    }
}
