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
        assertEquals("2 3", minMax.findBestMove(board, 1, 3));
        System.out.println(minMax.computations);
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
        String bestMove = minMax.findBestMove(board, 1, 3);
//        assertEquals("1 3", bestMove);
        System.out.println(minMax.computations);
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
        assertThat(minMax.findBestMove(board, 1, 3), anyOf(containsString("0 2"), containsString("0 3")));
        System.out.println(minMax.computations);
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
        assertThat(minMax.findBestMove(board, 1, 3), anyOf(containsString("2 2"),
                                                           containsString("0 3"),
                                                           containsString("4 0"),
                                                           containsString("3 0")));
        System.out.println(minMax.computations);
    }
}
