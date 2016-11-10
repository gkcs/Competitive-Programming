import main.java.MinMax;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        assertEquals("1 3", new MinMax().findBestMove(board, 1));
    }
}
