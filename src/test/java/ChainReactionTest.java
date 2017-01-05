import main.java.MinMax;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class ChainReactionTest {
    @Test
    public void centralBlockShouldBeFilled() {
        final int[][][] board = {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {2, 1}, {1, 3}, {2, 1}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}};
        MinMax minMax = new MinMax();
//        minMax.setTest(true);
        assertEquals("2 3", minMax.iterativeSearchForBestMove(board, 1));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }

    @Test
    public void analyzeDeep() {
        final int[][][] board = {
                {{1, 1}, {1, 2}, {1, 1}, {2, 2}, {2, 1}},
                {{0, 0}, {0, 0}, {2, 2}, {2, 3}, {0, 0}},
                {{2, 1}, {2, 1}, {1, 1}, {2, 1}, {0, 0}},
                {{0, 0}, {1, 1}, {0, 0}, {1, 1}, {1, 2}},
                {{1, 1}, {1, 2}, {1, 2}, {1, 2}, {1, 1}}};
        final MinMax minMax = new MinMax();
        //minMax.setTest(true);
        String actual = minMax.iterativeSearchForBestMove(board, 1);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
        assertEquals("2 4", actual);
    }

    @Test
    public void analyzeDeepAgain() {
        final int[][][] board = {
                {{1, 1}, {1, 2}, {1, 1}, {2, 2}, {0, 0}},
                {{1, 2}, {1, 1}, {1, 1}, {1, 2}, {2, 2}},
                {{1, 1}, {1, 3}, {1, 1}, {2, 1}, {1, 2}},
                {{1, 2}, {0, 0}, {2, 3}, {0, 0}, {0, 0}},
                {{1, 1}, {2, 2}, {0, 0}, {2, 1}, {1, 1}}};
        MinMax minMax = new MinMax();
        //minMax.setTest(true);
        String actual = minMax.iterativeSearchForBestMove(board, 1);
        System.out.println(actual);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
        assertEquals("3 0", actual);
    }

    @Test
    public void shouldBeFoundInCache() {
        final int[][][] board = {{{1, 1}, {0, 0}, {1, 2}, {1, 2}, {1, 1}},
                {{1, 2}, {1, 1}, {1, 2}, {0, 0}, {1, 2}},
                {{1, 1}, {0, 0}, {1, 1}, {1, 3}, {0, 0}},
                {{1, 1}, {1, 3}, {2, 1}, {1, 1}, {1, 2}},
                {{1, 1}, {0, 0}, {1, 2}, {0, 0}, {1, 1}}};
        MinMax minMax = new MinMax();
//        minMax.setTest(true);
        System.out.println(minMax.iterativeSearchForBestMove(board, 2));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }

    @Test
    public void firstBlock() {
        final int[][][] board = {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}};
        MinMax minMax = new MinMax();
//        minMax.setTest(true);
        System.out.println(minMax.iterativeSearchForBestMove(board, 1));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }

    @Test
    public void upperBlockShouldBeChosen() {
        final int[][][] board = {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {1, 3}, {0, 0}},
                {{0, 0}, {0, 0}, {2, 1}, {1, 1}, {2, 1}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}};

        MinMax minMax = new MinMax();
//        minMax.setTest(true);
        String bestMove = minMax.iterativeSearchForBestMove(board, 1);
        System.out.println(bestMove);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }

    @Test
    public void captureOn0_3() {
        final int[][][] board = {
                {{2, 1}, {2, 2}, {1, 2}, {1, 2}, {2, 1}},
                {{2, 2}, {2, 2}, {1, 1}, {0, 0}, {0, 0}},
                {{2, 1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {2, 2}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 2}, {0, 0}}};
        MinMax minMax = new MinMax();
//        minMax.setTest(true);
        String actual = minMax.iterativeSearchForBestMove(board, 1);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
        assertThat(actual, anyOf(containsString("0 2"), containsString("0 3")));
    }

    @Test
    public void captureOn2_2() {
        final int[][][] board = {
                {{2, 1}, {2, 1}, {0, 0}, {2, 2}, {0, 0}},
                {{2, 2}, {2, 3}, {2, 3}, {2, 3}, {2, 2}},
                {{1, 1}, {0, 0}, {1, 3}, {1, 1}, {2, 1}},
                {{1, 2}, {1, 1}, {1, 3}, {0, 0}, {0, 0}},
                {{1, 1}, {1, 2}, {1, 2}, {2, 1}, {2, 1}}};

        MinMax minMax = new MinMax();
//        minMax.setTest(true);
        assertThat(minMax.iterativeSearchForBestMove(board, 1), anyOf(containsString("2 2"),
                                                                      containsString("0 3"),
                                                                      containsString("4 0"),
                                                                      containsString("4 1"),
                                                                      containsString("4 2"),
                                                                      containsString("3 0"),
                                                                      containsString("3 2")));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }

    @Test
    public void dontHariKari() {
        final int[][][] board = {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {1, 1}}};
        final MinMax minMax = new MinMax();
        final String move = minMax.iterativeSearchForBestMove(board, 2);
        System.out.println(move);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
        assertFalse("3 4".equals(move));
        assertFalse("4 3".equals(move));
    }
}
