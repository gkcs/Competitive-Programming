package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static main.java.MinMax.MAX_VALUE;
import static main.java.MinMax.MIN_VALUE;
import static main.java.Utils.getBoardCopy;

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
    private static final int LEVEL = 3;
    private final Map<String, Long> boards = new HashMap<>();
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;

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
            final long moveValue;
            final Board movedBoard = board.makeMove(possibleMove);
            if (boards.containsKey(movedBoard.representation())) {
                moveValue = boards.get(movedBoard.representation());
            } else {
                final Integer terminalValue = movedBoard.terminalValue();
                if (terminalValue != null) {
                    moveValue = terminalValue;
                } else {
                    moveValue = evaluate(movedBoard, flip(possibleMove.player), LEVEL);
                }
                boards.put(movedBoard.representation(), moveValue);
                boards.put(movedBoard.flipOnX().representation(), moveValue);
                boards.put(movedBoard.flipOnY().representation(), moveValue);
                boards.put(movedBoard.flipOnDiag().representation(), moveValue);
            }
            movedBoard.undo();
            final long relevantValue = value(moveValue, possibleMove.player);
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

    private long evaluate(final Board board, final int player, final int level) {
        long max = MIN_VALUE;
        if (level <= 0) {
            return -value(board.heuristicValue(), player);
        }
        for (final Move possibleMove : board.getAllPossibleMoves(player)) {
            final long moveValue;
            final Board movedBoard = board.makeMove(possibleMove);
            if (boards.containsKey(movedBoard.representation())) {
                moveValue = boards.get(movedBoard.representation());
            } else {
                final Integer terminalValue = movedBoard.terminalValue();
                if (terminalValue != null) {
                    moveValue = terminalValue;
                } else {
                    moveValue = evaluate(movedBoard, flip(possibleMove.player), level - 1);
                }
                boards.put(movedBoard.representation(), moveValue);
                boards.put(movedBoard.flipOnX().representation(), moveValue);
                boards.put(movedBoard.flipOnY().representation(), moveValue);
                boards.put(movedBoard.flipOnDiag().representation(), moveValue);
            }
            movedBoard.undo();
            final long relevantValue = value(moveValue, possibleMove.player);
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
    private static List<int[][][]> previousStates = new ArrayList<>();
    private int[][][] board;
    private static final int BOARD_SIZE = 5;
    private static final int neighbours[][][] = new int[BOARD_SIZE][BOARD_SIZE][];
    private static final StringBuilder stringBuilder = new StringBuilder(60);

    Board(final int[][][] board) {
        this.board = board;
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

    private Board play(final Move move) {
        board[move.x][move.y][0] = move.player;
        board[move.x][move.y][1]++;
        if (terminalValue() != null) {
            return this;
        }
        if (neighbours[move.x][move.y].length <= board[move.x][move.y][1]) {
            board[move.x][move.y][1] = board[move.x][move.y][1] - neighbours[move.x][move.y].length;
            if (board[move.x][move.y][1] == 0) {
                board[move.x][move.y][0] = 0;
            }
            explode(move.x, move.y, move.player);
        }
        return this;
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
    void undo() {
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
        board = previousStates.remove(previousStates.size() - 1);
    }

    Integer terminalValue() {
        int first = 0;
        int second = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j][0] == 1) {
                    first++;
                } else if (board[i][j][0] == 2) {
                    second++;
                }
            }
        }
        if ((first + second != 0) && (first == 0 || second == 0)) {
            return first == 0 ? MIN_VALUE : MAX_VALUE;
        } else {
            return null;
        }
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
    public String toString() {
        return Arrays.deepToString(board);
    }

    Board flipOnX() {
        final int rawBoard[][][] = new int[BOARD_SIZE][][];
        final Board flipped = new Board(rawBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            rawBoard[i] = board[BOARD_SIZE - i - 1];
        }
        return flipped;
    }

    Board flipOnY() {
        final int rawBoard[][][] = new int[BOARD_SIZE][BOARD_SIZE][];
        final Board flipped = new Board(rawBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                rawBoard[i][j] = board[i][BOARD_SIZE - j - 1];
            }
        }
        return flipped;
    }

    Board flipOnDiag() {
        final int rawBoard[][][] = new int[BOARD_SIZE][BOARD_SIZE][2];
        final Board flipped = new Board(rawBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                rawBoard[i][j] = board[j][i];
            }
        }
        return flipped;
    }

    int heuristicValue() {
        int first = 0;
        int second = 0;
        int firstExplosives = 0;
        int secondExplosives = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j][0] == 1) {
                    first++;
                    if (board[i][j][1] == neighbours[i][j].length - 1) {
                        firstExplosives++;
                    }
                } else if (board[i][j][0] == 2) {
                    second++;
                    if (board[i][j][1] == neighbours[i][j].length - 1) {
                        secondExplosives++;
                    }
                }
            }
        }
        if ((first + second != 0) && (first == 0 || second == 0)) {
            return first == 0 ? MIN_VALUE : MAX_VALUE;
        } else {
            return 100 * (first - second) + 100 * (firstExplosives - secondExplosives);
        }
    }

    String representation() {
        return compactRepresentation();
    }

    private String verboseRepresentation() {
        stringBuilder.delete(0, stringBuilder.length());
        for (int[][] row : board) {
            for (int[] col : row) {
                for (int content : col) {
                    stringBuilder.append(content);
                }
            }
        }
        return stringBuilder.toString();
    }

    private String compactRepresentation() {
        final byte[] representation = new byte[13];
        int offset = 0;
        for (final int[][] row : board) {
            for (final int[] col : row) {
                for (final int content : col) {
                    representation[offset >> 3] |= content << (offset & 7);
                    offset = offset + 2;
                }
            }
        }
        try {
            return new String(representation, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

class Utils {
    static int[][][] getBoardCopy(final int board[][][]) {
        final int copyBoard[][][] = new int[board.length][board.length][2];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        return copyBoard;
    }
}
