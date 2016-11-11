package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

class ChainReaction {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final int[][][] board = new int[5][5][2];
        for (int i = 0; i < board.length; i++) {
            final String cols[] = bufferedReader.readLine().split(" ");
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i][j].length; k++) {
                    board[i][j][k] = cols[j].charAt(k) - '0';
                }
            }
        }
        System.out.println(new MinMax().findBestMove(board, Integer.parseInt(bufferedReader.readLine())));
    }
}

/**
 * A DFS results in too many resources consumed for the first possibility. A breadth first search is a better choice
 * selection strategy. A queue of positions is maintained. Each position has a corresponding parent position, which
 * helps move up the tree when the current position is evaluated.
 * <p>
 * A better approach will be iterative deepening. Local search around an 'interesting' area is feasible in these
 * scenarios.
 */
public class MinMax {
    private final Map<Board, Long> boards = new HashMap<>();
    private int computations;

    public MinMax() {
        Board.setNeighbours();
    }

    public String findBestMove(final int[][][] rawBoard, final int player) {
        final Board board = new Board(rawBoard);
        long max = MIN_VALUE;
        final Move[] allPossibleMoves = board.getAllPossibleMoves(player);
        if (allPossibleMoves.length == 0) {
            throw new RuntimeException("No possible moves");
        }
        Move bestMove = allPossibleMoves[0];
        for (final Move possibleMove : allPossibleMoves) {
            final Board movedBoard = board.makeMove(possibleMove);
            final long moveValue;
            if (boards.containsKey(movedBoard)) {
                moveValue = boards.get(movedBoard);
            } else if (movedBoard.isTerminalState()) {
                moveValue = movedBoard.getValue();
            } else {
                moveValue = evaluate(movedBoard, flip(player));
                boards.put(movedBoard, moveValue);
                boards.put(movedBoard.flipOnX(), moveValue);
                boards.put(movedBoard.flipOnY(), moveValue);
                boards.put(movedBoard.flipOnDiag(), moveValue);
            }
            final long relevantValue = value(moveValue, player);
            board.undo();
            if (relevantValue > max) {
                max = relevantValue;
                bestMove = possibleMove;
                if (max == MAX_VALUE) {
                    break;
                }
            }
        }
        return bestMove.describe();
    }

    private long evaluate(final Board board, final int player) {
        long max = MIN_VALUE;
        computations++;
        if (computations > 5000) {
            return -max;
        }
        for (final Move possibleMove : board.getAllPossibleMoves(player)) {
            final Board movedBoard = board.makeMove(possibleMove);
            final long moveValue;
            if (boards.containsKey(movedBoard)) {
                moveValue = boards.get(movedBoard);
            } else if (movedBoard.isTerminalState()) {
                moveValue = movedBoard.getValue();
            } else {
                moveValue = evaluate(movedBoard, flip(player));
                boards.put(movedBoard, moveValue);
                boards.put(movedBoard.flipOnX(), moveValue);
                boards.put(movedBoard.flipOnY(), moveValue);
                boards.put(movedBoard.flipOnDiag(), moveValue);
            }
            final long relevantValue = value(moveValue, player);
            board.undo();
            if (relevantValue > max) {
                max = relevantValue;
                if (max == MAX_VALUE) {
                    break;
                }
            }
        }
        return -max;
    }

    private long value(final long moveValue, final int player) {
        return moveValue * (player == 1 ? 1 : -1);
    }

    private int flip(final int player) {
        return ~player & 3;
    }
}

class Move {
    final int x, y, player;

    Move(int x, int y, int player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }

    String describe() {
        return x + " " + y;
    }

    @Override
    public String toString() {
        return "Move{" +
                "x=" + x +
                ", y=" + y +
                ", player=" + player +
                '}';
    }
}

/**
 * The board contains a list of all of its parents. Each time someone asks us to undo the board, we fall back to a
 * copy of the board in the previous state. Whenever a state changing move is made on the board, the current state is
 * stored as a parent in memory, and another immutable Board is returned.
 */
class Board {
    private List<Board> previousStates = new ArrayList<>();
    private final int[][][] board;
    private static final int BOARD_SIZE = 5;
    private static final int neighbours[][][] = new int[BOARD_SIZE][BOARD_SIZE][];
    private byte first, second;
    private boolean terminated;

    Board(final int[][][] board) {
        this.board = board;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j][0] == 1) {
                    first++;
                } else if (board[i][j][0] == 2) {
                    second++;
                }
            }
        }
    }

    static void setNeighbours() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                final long x = i * BOARD_SIZE + j;
                final List<Long> near = new ArrayList<>();
                near.add(x + 1);
                near.add(x + BOARD_SIZE);
                near.add(x - 1);
                near.add(x - BOARD_SIZE);
                if (i == 0) {
                    near.remove(x - BOARD_SIZE);
                }
                if (j == 0) {
                    near.remove(x - 1);
                }
                if (i == BOARD_SIZE - 1) {
                    near.remove(x + BOARD_SIZE);
                }
                if (j == BOARD_SIZE - 1) {
                    near.remove(x + 1);
                }
                neighbours[i][j] = new int[near.size()];
                for (int k = 0; k < near.size(); k++) {
                    if (near.get(k) >= 0 && near.get(k) <= BOARD_SIZE * BOARD_SIZE) {
                        neighbours[i][j][k] = Math.toIntExact(near.get(k));
                    }
                }
            }
        }
    }

    Board makeMove(final Move move) {
        previousStates.add(getBoardCopy(board));
        return play(move);
    }

    private Board getBoardCopy(final int board[][][]) {
        final int copyBoard[][][] = new int[BOARD_SIZE][BOARD_SIZE][2];
        final Board copy = new Board(copyBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        copy.first = first;
        copy.second = second;
        return copy;
    }

    private Board play(final Move move) {
        updateScores(move);
        board[move.x][move.y][0] = move.player;
        board[move.x][move.y][1]++;
        if (isTerminalState()) {
            return this;
        }
        if (neighbours[move.x][move.y].length <= board[move.x][move.y][1]) {
            board[move.x][move.y][1] = board[move.x][move.y][1] - neighbours[move.x][move.y].length;
            setCellBlankIfRequired(move);
            explode(move.x, move.y, move.player);
        }
        return this;
    }

    private void setCellBlankIfRequired(final Move move) {
        if (board[move.x][move.y][1] == 0) {
            board[move.x][move.y][0] = 0;
            if (move.player == 1) {
                first--;
            } else {
                second--;
            }
        }
        if (first == 0 || second == 0) {
            terminated = true;
        }
    }

    private void updateScores(final Move move) {
        if (board[move.x][move.y][0] != move.player) {
            if (board[move.x][move.y][0] != 0) {
                if (move.player == 1) {
                    first++;
                    second--;
                } else {
                    first--;
                    second++;
                }
            } else {
                if (move.player == 1) {
                    first++;
                } else {
                    second++;
                }
            }
        }
        if (first == 0 || second == 0) {
            terminated = true;
        }
    }

    private void explode(final int x, final int y, final int player) {
        for (final int neighbour : neighbours[x][y]) {
            play(new Move(neighbour / BOARD_SIZE, neighbour % BOARD_SIZE, player));
        }
    }

    /**
     * We need to find the inverse of the function play.
     * As play(board, move) = board', we look for the function play^-1(board', move) such that it gives us board.
     */
    Board undo() {
        /*
         * What does a move do? Convert if necessary, and adds. Then it explodes if necessary. Which in turn calls play
         * on other points.
         *
         * So then undo should convert and un-explode if necessary and subtract.
         * Explosions need to be handled differently. An inverse explosion is needed if this point is at 0 now. Any
         * other score is ambiguous.
         *
         *
         *    X = X -1
         *
         *    Consider a list of board positions, with their corresponding affiliations and strengths. Maintaining a
         *    list of each board position owner and strength based on timestamp, we can move to any given point in
         *    time through the undo operation.
         *
         */
        return previousStates.remove(previousStates.size() - 1);
    }

    boolean isTerminalState() {
        return terminated;
    }

    int getValue() {
        return first - second;
    }

    Move[] getAllPossibleMoves(final int player) {
        final List<Move> list = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (player == board[i][j][0] || board[i][j][0] == 0) {
                    list.add(new Move(i, j, player));
                }
            }
        }
        return list.toArray(new Move[list.size()]);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || Utils.deepEqualsArray(board, ((Board) o).board);
    }

    @Override
    public int hashCode() {
        return Utils.deepHashCodeArray(board);
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.deepToString(board) +
                ", first=" + first +
                ", second=" + second +
                '}';
    }

    Board flipOnX() {
        final int rawBoard[][][] = new int[BOARD_SIZE][][];
        final Board flipped = new Board(rawBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            rawBoard[i] = board[BOARD_SIZE - i - 1];
        }
        flipped.first = first;
        flipped.second = second;
        return flipped;
    }

    Board flipOnY() {
        final int rawBoard[][][] = new int[BOARD_SIZE][][];
        final Board flipped = new Board(rawBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                rawBoard[i][j] = board[i][BOARD_SIZE - j - 1];
            }
        }
        flipped.first = first;
        flipped.second = second;
        return flipped;
    }

    Board flipOnDiag() {
        final int rawBoard[][][] = new int[BOARD_SIZE][][];
        final Board flipped = new Board(rawBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                rawBoard[i][j] = board[j][i];
            }
        }
        flipped.first = first;
        flipped.second = second;
        return flipped;
    }
}

class Utils {
    static int deepHashCodeArray(int board[][][]) {
        long x = 0;
        for (final int[][] row : board) {
            for (final int[] col : row) {
                x <<= 1;
                x |= col[0] - 1;
                x <<= 2;
                x |= col[1];
            }
        }
        return Long.hashCode(x);
    }

    static boolean deepEqualsArray(final int board[][][], final int[][][] other) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i].length; k++) {
                    if (board[i][j][k] != other[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
