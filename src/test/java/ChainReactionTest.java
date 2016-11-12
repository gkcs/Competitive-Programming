import main.java.MinMax;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
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
        assertEquals("2 3", new MinMax().findBestMove(board, 1));
    }

    @Test
    public void upperBlockShouldBeChosen() {
        final int[][][] board = {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {1, 3}, {0, 0}},
                {{0, 0}, {0, 0}, {2, 1}, {1, 1}, {2, 1}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}};
        String bestMove = new MinMax().findBestMove(board, 1);
//        assertEquals("1 3", bestMove);
    }

    @Test
    public void captureOn1_3() {
        final int[][][] board = {
                {{2, 1}, {2, 2}, {1, 2}, {1, 2}, {2, 1}},
                {{2, 2}, {2, 2}, {1, 1}, {0, 0}, {0, 0}},
                {{2, 1}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {2, 2}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 2}, {0, 0}}};
        assertThat(new MinMax().findBestMove(board, 1), anyOf(containsString("0 2"), containsString("0 3")));
    }

    @Test
    public void captureOn2_2() {
        final int[][][] board = {
                {{2, 1}, {2, 1}, {0, 0}, {2, 2}, {0, 0}},
                {{2, 2}, {2, 3}, {2, 3}, {2, 3}, {2, 2}},
                {{1, 1}, {0, 0}, {1, 3}, {1, 1}, {2, 1}},
                {{1, 2}, {1, 1}, {1, 3}, {0, 0}, {0, 0}},
                {{1, 1}, {1, 2}, {1, 2}, {2, 1}, {2, 1}}};
        assertThat(new MinMax().findBestMove(board, 1), anyOf(containsString("2 2"), containsString("0 3")));
    }
}
