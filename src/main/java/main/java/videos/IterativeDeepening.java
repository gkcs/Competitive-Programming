package main.java.videos;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * Contains a lot of objects for metrics. Should ideally be separated from those responsibilities.
 */
public class IterativeDeepening {
    public static void main(String[] args) {
        System.out.println(new Game().iterativeSearchForBestMove(new int[0][0], 1));
    }
}

/**
 * Alpha - Beta
 * Branch - 25%
 *
 * D -> D + 1
 *
 */

class Game {
    private static final int MAX_DEPTH = 60;
    public int depth = 4;
    private final long startTime = System.currentTimeMillis();
    private static final int timeOut = 1000;

    /**
     * Iterative deepening is implemented for flexible depth search. Also, it allows us to rearrange all moves as per
     * (known) optimal ordering after each iteration. This is important because alpha-beta
     * performs best when given a good move order.
     * On the final iteration, when an exception is thrown, the best move will be propagated upwards from the
     * {@link #evaluate(Board, int, int, long, long)} method.
     */
    public String iterativeSearchForBestMove(final int[][] game, final int player) {
        final Move[] startingMoves = new Move[game.length];
        for (int i = 0; i < startingMoves.length; i++) {
            startingMoves[i] = new Move(player);
        }
        for (int i = 0; i < game.length; i++) {
            startingMoves[i] = new Move(player);
        }
        Arrays.sort(startingMoves);
        Move bestMove = new Move(player);
        final Board board = new Board(game);
        try {
            while (depth < MAX_DEPTH) {
                bestMove = evaluate(board, player, depth, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                depth++;
            }
        } catch (TimeoutException ignored) {
        }
        return bestMove.toString();
    }

    /**
     * @param board  Input Board. All branches in the Min Max Tree from this node are possible moves from this board.
     * @param player Player making the move.
     * @param level  Depth on which this tree is now.
     * @param a      Alpha
     * @param b      Beta
     * @return The value of current board position
     * @throws TimeoutException if it runs out of time.
     */
    private Move evaluate(final Board board,
                          final int player,
                          final int level,
                          final long a,
                          final long b) throws TimeoutException {
        if (System.currentTimeMillis() - startTime > timeOut) {
            throw new TimeoutException();
        }
        return evaluate(board.makeMove(new Move(player)), flip(player), level - 1, -b, -a);
    }

    static int flip(final int player) {
        return player == 1 ? 2 : 1;
    }
}

class Move implements Comparable<Move> {
    public Move(final int player) {

    }

    @Override
    public int compareTo(final Move o) {
        return 0;
    }
}

class Board {
    Board(final int[][] game) {
    }

    Board makeMove(final Move move) {
        return this;
    }
}
