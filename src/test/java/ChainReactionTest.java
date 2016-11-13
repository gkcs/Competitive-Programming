import main.java.MinMax;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
        assertEquals("2 3", minMax.iterativeSearchForBestMove(board, 1));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.cacheHits);
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
        System.out.println(minMax.iterativeSearchForBestMove(board, 1));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.cacheHits);
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
        String bestMove = minMax.iterativeSearchForBestMove(board, 1);
        System.out.println(bestMove);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.cacheHits);
    }

    @Test
    public void captureOn1_3() {
        final int[][][] board = {
                {{2, 1}, {2, 2}, {1, 2}, {1, 2}, {2, 1}},
                {{2, 2}, {2, 2}, {1, 1}, {0, 0}, {0, 0}},
                {{2, 1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {2, 2}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 2}, {0, 0}}};

        MinMax minMax = new MinMax();
        assertThat(minMax.iterativeSearchForBestMove(board, 1), anyOf(containsString("0 2"), containsString("0 3")));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.cacheHits);
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
        assertThat(minMax.iterativeSearchForBestMove(board, 1), anyOf(containsString("2 2"),
                                                                      containsString("0 3"),
                                                                      containsString("4 0"),
                                                                      containsString("4 1"),
                                                                      containsString("4 2"),
                                                                      containsString("3 0"),
                                                                      containsString("3 2")));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.cacheHits);
    }
}
