package main.java.videos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Alpha - Beta
 * Branch - 25%
 * <p>
 * D -> D + 1
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
        int bestScore = Integer.MIN_VALUE;
        final Board board = new Board(game);
        try {
            while (depth < MAX_DEPTH) {
                for (final Move startingMove : startingMoves) {
                    int score = evaluate(board, player, depth, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    if (bestScore < score) {
                        bestScore = score;
                        bestMove = startingMove;
                    }
                }
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
    private int evaluate(final Board board,
                         final int player,
                         final int level,
                         final long a,
                         final long b) throws TimeoutException {
        if (System.currentTimeMillis() - startTime > timeOut) {
            throw new TimeoutException();
        }
        final List<Move> allPossibleMoves = getAllPossibleMoves(player);
        int bestScore = Integer.MIN_VALUE;
        for (final Move move : allPossibleMoves) {
            final int score;
            if (level > 0) {
                score = evaluate(board.makeMove(move), flip(player), level - 1, -b, -a);
            } else {
                score = quietSearch(board.makeMove(move), flip(player), level - 1, -b, -a);
            }
            if (bestScore < score) {
                bestScore = score;
            }
        }
        return bestScore;
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
    private int quietSearch(final Board board,
                            final int player,
                            final int level,
                            final long a,
                            final long b) throws TimeoutException {
        if (System.currentTimeMillis() - startTime > timeOut) {
            throw new TimeoutException();
        }
        final List<Move> captureMoves = getAllPossibleMoves(player)
                .stream()
                .filter(Move::isACapture)
                .collect(Collectors.toList());
        int bestScore = Integer.MIN_VALUE;
        for (final Move move : captureMoves) {
            int score = quietSearch(board.makeMove(move), flip(player), level - 1, -b, -a);
            if (bestScore < score) {
                bestScore = score;
            }
        }
        return bestScore;
    }

    private List<Move> getAllPossibleMoves(final int player) {
        return Collections.singletonList(new Move(player));
    }

    static int flip(final int player) {
        return player == 1 ? 2 : 1;
    }
}
