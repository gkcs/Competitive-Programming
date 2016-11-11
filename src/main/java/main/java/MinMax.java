package main.java;

import java.util.*;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

class ChainReaction {
    public static void main(String[] args) {
        final int[][][] board = {
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {2, 1}, {1, 3}, {2, 1}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}};
        System.out.println(new MinMax().findBestMove(board, 1));
    }
}


/**
 * A DFS results in too many resources consumed for the first possibility. A breadth first search is a better choice
 * selection strategy. A queue of positions is maintained. Each position has a corresponding parent position, which
 * helps move up the tree when the current position is evaluated.
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
            final long moveValue = boards.containsKey(movedBoard)
                    ? boards.get(movedBoard)
                    : movedBoard.isTerminalState()
                    ? movedBoard.getValue()
                    : evaluate(movedBoard, flip(player));
            boards.put(movedBoard, moveValue);
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
        System.out.println(board);
        if (computations > 50) {
            return -max;
        }
        for (final Move possibleMove : board.getAllPossibleMoves(player)) {
            final Board movedBoard = board.makeMove(possibleMove);
            final long moveValue = boards.containsKey(movedBoard)
                    ? boards.get(movedBoard)
                    : movedBoard.isTerminalState()
                    ? movedBoard.getValue()
                    : evaluate(movedBoard, flip(player));
            boards.put(movedBoard, moveValue);
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
        System.out.println(Arrays.deepToString(neighbours));
    }

    Board makeMove(final Move move) {
        final int copyBoard[][][] = new int[BOARD_SIZE][BOARD_SIZE][2];
        final Board copy = new Board(copyBoard);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        copy.first = first;
        copy.second = second;
        previousStates.add(copy);
        return play(move);
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

    private void setCellBlankIfRequired(Move move) {
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
     * As play(board, move) = board' , we look for the function play^-1(board', move) such that it gives us board.
     */
    Board undo() {
        /*
         * What does move do? Convert and add. Then it explodes if necessary. Which in turn calls play on other points.
         *
         * So then undo should convert and subtract. The current point was definitely belonging to player.
         * Explosions need to be handled differently. An inverse explosion is needed if this point is at 0 now. Any
         * other score is ambiguous.
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
                if (player == board[i][j][0]) {
                    list.add(new Move(i, j, player));
                }
            }
        }
        return list.toArray(new Move[list.size()]);
    }

    private boolean deepEqualsArray(final int[][][] other) {
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

    private int deepHashCodeArray() {
        long x = 0;
        for (final int[][] row : board) {
            for (final int[] col : row) {
                for (final int content : col) {
                    x <<= 2;
                    x |= content;
                }
            }
        }
        return Long.hashCode(x);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && deepEqualsArray(((Board) o).board);

    }

    @Override
    public int hashCode() {
        return deepHashCodeArray();
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.deepToString(board) +
                ", first=" + first +
                ", second=" + second +
                '}';
    }
}
