package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Iterative deepening and alpha beta are the ways to move forward.
 */
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
        final int player = Integer.parseInt(bufferedReader.readLine());
        final MinMax minMax = new MinMax();
        System.out.println(minMax.iterativeSearchForBestMove(board, player));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves);
    }
}

/**
 * A DFS results in too many resources consumed for the first possibility. A breadth first search is a better choice
 * selection strategy. A queue of positions is maintained. Each position has a corresponding parent position, which
 * helps move up the tree when the current position is evaluated.
 * <p>
 * A better approach will be iterative deepening. Local search around an 'interesting' area is feasible in these
 * scenarios.
 * Alpha Beta pruning is necessary so that we do not blindly follow whatever we see.
 */
public class MinMax {
    public static int TIME_OUT = 900;
    public int computations = 0, depth = 3, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Cache cache = new Cache();

    static {
        Board.setNeighbours();
    }

    public String iterativeSearchForBestMove(final int[][][] board, final int player) {
        String bestMove = "LOL";
        while (depth < 60) {
            try {
                bestMove = findBestMove(Board.getCopy(board), player, depth);
            } catch (final Exception e) {
                if (!e.getMessage().equals("Time out...")) {
                    bestMove = e.getMessage() + " " + e.getClass().getSimpleName();
                }
                break;
            } finally {
                Board.previousStates.clear();
                depth++;
            }
        }
        return bestMove;
    }

    private String findBestMove(final int[][][] rawBoard,
                                final int player,
                                final int level) {
        final Board board = new Board(rawBoard);
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        long max = MIN_VALUE;
        final Move[] allPossibleMoves = board.getAllPossibleMoves(player);
        if (allPossibleMoves.length == 0) {
            throw new RuntimeException("No possible moves");
        }
        for (final Move move : allPossibleMoves) {
            move.strength = board.makeMove(move).heuristicValue(player);
            board.undo();
        }
        Arrays.sort(allPossibleMoves);
        Move bestMove = allPossibleMoves[0];
        for (final Move possibleMove : allPossibleMoves) {
            final long moveValue;
            final Board movedBoard = board.makeMove(possibleMove);
            moveValue = evaluate(movedBoard, flip(possibleMove.player), level, toTake, toGive);
            movedBoard.undo();
            if (player == 1) {
                if (toTake < moveValue) {
                    toTake = moveValue;
                }
            } else {
                if (toGive > -moveValue) {
                    toGive = -moveValue;
                }
            }
            if (moveValue > max) {
                max = moveValue;
                bestMove = possibleMove;
                if (Math.abs(max - MAX_VALUE) <= 100) {
                    break;
                }
            }
            if (toTake >= toGive) {
                max = moveValue;
                break;
            }
        }
        eval = max;
        moves = allPossibleMoves.length;
        return bestMove.describe();
    }

    private long evaluate(final Board board, final int player, final int level, final long a, final long b) {
        long toTake = a, toGive = b;
        long max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            throw new RuntimeException("Time out...");
        }
        final Boolean storedVal = cache.get(board);
        if (storedVal != null) {
            max = value(storedVal ? MAX_VALUE : MIN_VALUE, player);
        } else {
            final Integer terminalValue;
            if ((terminalValue = board.terminalValue()) != null) {
                max = value(terminalValue, player);
                max += max < 0 ? level : -level;
            } else if (level <= 0) {
                max = board.heuristicValue(player);
            } else {
                final Move[] allPossibleMoves = board.getAllPossibleMoves(player);
                for (final Move move : allPossibleMoves) {
                    move.strength = board.makeMove(move).heuristicValue(player);
                    board.undo();
                }
                Arrays.sort(allPossibleMoves);
                for (final Move possibleMove : allPossibleMoves) {
                    final long moveValue;
                    final Board movedBoard = board.makeMove(possibleMove);
                    computations++;
                    moveValue = evaluate(movedBoard, flip(possibleMove.player), level - 1, toTake, toGive);
                    movedBoard.undo();
                    if (player == 1) {
                        if (toTake < moveValue) {
                            toTake = moveValue;
                        }
                    } else {
                        if (toGive > -moveValue) {
                            toGive = -moveValue;
                        }
                    }
                    if (moveValue > max) {
                        max = moveValue;
                        if (Math.abs(max - MAX_VALUE) <= 100) {
                            break;
                        }
                    }
                    if (toTake >= toGive) {
                        max = moveValue;
                        break;
                    }
                }
            }
        }
        return -max;
    }

    private long value(final long moveValue, final int player) {
        return moveValue * (player == 1 ? 1 : -1);
    }

    static int flip(final int player) {
        return ~player & 3;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}

class Move implements Comparable<Move> {
    final int x, y, player;
    int strength;

    Move(final int x, final int y, final int player) {
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

    @Override
    public int compareTo(Move other) {
        return other.strength - strength;
    }
}

/**
 * The board contains a list of all of its parents. Each time someone asks us to undo the board, we fall back to a
 * copy of the board in the previous state. Whenever a state changing move is made on the board, the current state is
 * stored as a parent in memory, and another immutable Board is returned.
 */
class Board {
    static final List<int[][][]> previousStates = new ArrayList<>();
    static Function<int[], Integer> heuristicEval;
    int[][][] board;
    private static final int BOARD_SIZE = 5;
    private static final int neighbours[][][] = new int[BOARD_SIZE][BOARD_SIZE][];

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
        previousStates.add(getCopy(board));
        return play(move);
    }

    Board play(final Move move) {
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
        if ((first + second > 1) && (first == 0 || second == 0)) {
            return first == 0 ? MinMax.MIN_VALUE : MinMax.MAX_VALUE;
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
        final StringBuilder stringBuilder = new StringBuilder("map.put(new Board(new int[][][]{");
        for (final int row[][] : board) {
            stringBuilder.append('{');
            for (final int col[] : row) {
                stringBuilder.append('{');
                for (final int content : col) {
                    stringBuilder.append(content).append(',');
                }
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                stringBuilder.append('}').append(',');
            }
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.append('}').append(',').append('\n');
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append("}),");
        return stringBuilder.toString();
    }

    int heuristicValue(final int player) {
        int orbs = 0;
        int inThreat = 0;
        int bonus = 0;
        int contiguous = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j][0] == player) {
                    orbs += board[i][j][1];
                    if (board[i][j][1] == neighbours[i][j].length - 1) {
                        ++contiguous;
                    }
                    boolean surround = false;
                    for (int k = 0; k < neighbours[i][j].length; k++) {
                        final int row = neighbours[i][j][k] / BOARD_SIZE;
                        final int col = neighbours[i][j][k] % BOARD_SIZE;
                        final int[] neighbour = board[row][col];
                        final int criticalMass = neighbours[row][col].length - 1;
                        if (neighbour[0] == MinMax.flip(player) && neighbour[1] == criticalMass) {
                            inThreat -= 5 - criticalMass;
                            surround = true;
                        }
                    }
                    if (!surround) {
                        if (neighbours[i][j].length < 4) {
                            bonus += neighbours[i][j].length == 3 ? 2 : 3;
                        }
                        if (board[i][j][1] == neighbours[i][j].length - 1) {
                            bonus += 2;
                        }
                    }
                }
            }
        }
        return (orbs + inThreat + bonus + contiguous);
    }

    static int[][][] getCopy(final int board[][][]) {
        final int copyBoard[][][] = new int[board.length][board.length][2];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        return copyBoard;
    }

    @Override
    public boolean equals(Object o) {
        final Board other = (Board) o;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < 2; k++) {
                    if (other.board[i][j][k] != board[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}

class Cache {
    private final Map<Board, Boolean> map;

    public Cache() {
        map = new ConcurrentHashMap<>();
        new Thread(this::loadCache).start();
    }

    private void loadCache() {
        map.put(new Board(new int[][][]{{{1, 1}, {1, 4}, {1, 1}, {1, 1}, {1, 1}},
                {{1, 2}, {1, 1}, {1, 1}, {1, 5}, {1, 1}},
                {{1, 1}, {1, 1}, {1, 3}, {1, 4}, {0, 0}},
                {{1, 2}, {1, 3}, {1, 1}, {1, 2}, {1, 2}},
                {{1, 1}, {1, 2}, {1, 2}, {0, 0}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 1}, {2, 2}, {2, 1}},
                {{1, 2}, {1, 3}, {2, 2}, {2, 1}, {2, 2}},
                {{1, 2}, {0, 0}, {0, 0}, {2, 3}, {2, 2}},
                {{1, 2}, {1, 3}, {0, 0}, {2, 1}, {2, 2}},
                {{1, 1}, {1, 2}, {1, 1}, {0, 0}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {0, 0}, {1, 2}, {2, 1}},
                {{1, 2}, {1, 2}, {1, 3}, {2, 1}, {2, 1}},
                {{1, 2}, {2, 1}, {1, 1}, {2, 3}, {2, 2}},
                {{1, 2}, {1, 3}, {1, 1}, {2, 1}, {2, 2}},
                {{1, 1}, {1, 2}, {1, 1}, {2, 1}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{2, 1}, {2, 2}, {0, 0}, {1, 2}, {1, 1}},
                {{2, 1}, {2, 3}, {1, 3}, {1, 1}, {1, 2}},
                {{1, 2}, {2, 2}, {2, 1}, {1, 3}, {1, 2}},
                {{2, 1}, {2, 3}, {2, 1}, {2, 1}, {0, 0}},
                {{0, 0}, {2, 1}, {2, 1}, {2, 2}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {2, 1}, {2, 2}, {2, 1}},
                {{1, 2}, {1, 3}, {1, 2}, {2, 3}, {2, 2}},
                {{1, 2}, {1, 2}, {2, 1}, {2, 1}, {2, 2}},
                {{1, 2}, {2, 1}, {0, 0}, {0, 0}, {2, 2}},
                {{1, 1}, {1, 2}, {0, 0}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{2, 1}, {2, 2}, {2, 2}, {1, 2}, {1, 1}},
                {{2, 2}, {2, 1}, {2, 2}, {2, 3}, {1, 1}},
                {{2, 2}, {2, 2}, {2, 3}, {2, 3}, {1, 1}},
                {{0, 0}, {2, 2}, {2, 1}, {2, 2}, {1, 1}},
                {{2, 1}, {2, 2}, {2, 2}, {1, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{2, 2}, {2, 1}, {0, 0}, {2, 2}, {0, 0}},
                {{2, 2}, {2, 3}, {2, 3}, {2, 2}, {2, 1}},
                {{2, 2}, {2, 1}, {2, 1}, {2, 2}, {2, 1}},
                {{2, 1}, {2, 3}, {2, 3}, {2, 3}, {2, 2}},
                {{2, 1}, {2, 1}, {2, 1}, {0, 0}, {2, 2}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 1}, {2, 2}, {2, 1}},
                {{0, 0}, {1, 3}, {1, 2}, {2, 2}, {2, 2}},
                {{1, 2}, {1, 1}, {1, 2}, {2, 2}, {2, 2}},
                {{1, 2}, {1, 1}, {2, 3}, {2, 2}, {2, 1}},
                {{1, 1}, {2, 2}, {0, 0}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {0, 0}, {1, 2}, {2, 1}},
                {{1, 1}, {1, 3}, {1, 3}, {2, 1}, {2, 2}},
                {{1, 1}, {1, 2}, {1, 2}, {2, 2}, {2, 2}},
                {{1, 2}, {1, 1}, {1, 2}, {2, 3}, {0, 0}},
                {{1, 1}, {1, 1}, {1, 2}, {1, 1}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {0, 0}, {2, 2}, {2, 1}},
                {{1, 2}, {1, 3}, {2, 1}, {2, 3}, {2, 2}},
                {{1, 2}, {1, 3}, {0, 0}, {2, 3}, {2, 2}},
                {{1, 2}, {1, 1}, {1, 2}, {2, 2}, {2, 2}},
                {{1, 1}, {1, 2}, {0, 0}, {1, 1}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {2, 1}, {2, 1}, {2, 2}, {2, 1}},
                {{1, 1}, {2, 3}, {0, 0}, {2, 3}, {2, 2}},
                {{1, 1}, {1, 3}, {2, 2}, {2, 3}, {0, 0}},
                {{1, 1}, {1, 3}, {1, 1}, {0, 0}, {2, 2}},
                {{1, 1}, {1, 2}, {0, 0}, {1, 1}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {0, 0}, {1, 2}, {2, 1}},
                {{1, 2}, {1, 1}, {1, 3}, {0, 0}, {2, 1}},
                {{1, 2}, {1, 3}, {2, 2}, {0, 0}, {2, 2}},
                {{1, 2}, {1, 3}, {1, 1}, {0, 0}, {2, 2}},
                {{1, 1}, {1, 2}, {2, 1}, {2, 2}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{2, 3}, {2, 2}, {0, 0}, {2, 2}, {2, 1}},
                {{2, 1}, {2, 1}, {2, 2}, {2, 3}, {0, 0}},
                {{2, 1}, {2, 3}, {2, 3}, {2, 2}, {2, 2}},
                {{2, 2}, {2, 1}, {2, 2}, {2, 2}, {2, 1}},
                {{2, 1}, {2, 1}, {2, 1}, {2, 1}, {0, 0}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 2}, {1, 2}, {1, 1}},
                {{1, 1}, {0, 0}, {1, 2}, {1, 3}, {1, 1}},
                {{1, 2}, {1, 3}, {1, 1}, {1, 2}, {1, 2}},
                {{1, 1}, {2, 1}, {2, 3}, {2, 1}, {1, 2}},
                {{2, 1}, {2, 2}, {2, 2}, {0, 0}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{2, 1}, {2, 2}, {0, 0}, {2, 2}, {0, 0}},
                {{2, 1}, {2, 3}, {2, 2}, {1, 1}, {1, 2}},
                {{2, 1}, {2, 2}, {2, 3}, {1, 3}, {1, 1}},
                {{2, 1}, {2, 3}, {2, 2}, {0, 0}, {1, 2}},
                {{2, 1}, {2, 1}, {0, 0}, {2, 1}, {1, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {2, 1}, {2, 2}, {2, 1}},
                {{1, 2}, {1, 3}, {1, 2}, {2, 3}, {2, 2}},
                {{1, 2}, {1, 3}, {0, 0}, {2, 2}, {2, 2}},
                {{1, 1}, {0, 0}, {1, 2}, {2, 2}, {2, 1}},
                {{1, 1}, {2, 1}, {2, 2}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {2, 1}, {2, 1}, {2, 1}},
                {{1, 2}, {1, 2}, {2, 2}, {2, 1}, {2, 2}},
                {{1, 2}, {1, 1}, {2, 1}, {2, 3}, {2, 2}},
                {{1, 1}, {0, 0}, {2, 1}, {2, 3}, {2, 2}},
                {{1, 1}, {1, 2}, {2, 1}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{
                {{1, 1}, {1, 2}, {1, 2}, {2, 1}, {2, 1}},
                {{1, 2}, {1, 2}, {1, 1}, {2, 2}, {0, 0}},
                {{2, 2}, {2, 3}, {2, 1}, {2, 1}, {2, 2}},
                {{0, 0}, {2, 1}, {2, 3}, {0, 0}, {2, 1}},
                {{2, 1}, {2, 2}, {2, 2}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {0, 0}, {1, 2}, {1, 2}, {1, 1}},
                {{1, 2}, {1, 1}, {1, 2}, {0, 0}, {1, 2}},
                {{1, 1}, {0, 0}, {1, 1}, {1, 3}, {2, 1}},
                {{1, 1}, {1, 3}, {2, 1}, {1, 1}, {1, 2}},
                {{1, 1}, {0, 0}, {1, 2}, {0, 0}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {0, 0}, {1, 3}, {1, 1}},
                {{1, 1}, {0, 0}, {1, 1}, {0, 0}, {1, 2}},
                {{0, 0}, {1, 2}, {1, 3}, {1, 1}, {1, 1}},
                {{1, 1}, {1, 3}, {1, 1}, {1, 3}, {1, 2}},
                {{1, 1}, {1, 2}, {1, 2}, {1, 1}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 3}, {1, 1}, {0, 0}},
                {{1, 2}, {0, 0}, {1, 1}, {1, 3}, {1, 3}},
                {{1, 2}, {1, 3}, {1, 3}, {1, 1}, {1, 2}},
                {{1, 2}, {1, 2}, {1, 3}, {1, 3}, {1, 2}},
                {{1, 1}, {0, 0}, {1, 2}, {1, 2}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 3}, {0, 0}, {1, 3}},
                {{1, 2}, {0, 0}, {1, 1}, {1, 3}, {1, 3}},
                {{1, 2}, {1, 3}, {1, 3}, {1, 1}, {1, 2}},
                {{1, 2}, {1, 2}, {1, 3}, {1, 3}, {1, 2}},
                {{1, 1}, {0, 0}, {1, 2}, {1, 2}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 2}, {1, 2}, {1, 3}, {1, 2}, {1, 4}},
                {{0, 0}, {1, 3}, {1, 3}, {1, 1}, {1, 2}},
                {{1, 2}, {1, 2}, {1, 1}, {1, 5}, {1, 1}},
                {{1, 2}, {1, 3}, {1, 3}, {1, 2}, {1, 2}},
                {{1, 1}, {1, 2}, {1, 2}, {1, 2}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 2}, {1, 2}, {1, 1}},
                {{1, 1}, {1, 3}, {2, 1}, {2, 2}, {2, 1}},
                {{1, 1}, {2, 2}, {2, 3}, {2, 2}, {2, 2}},
                {{2, 2}, {2, 1}, {2, 1}, {2, 2}, {2, 2}},
                {{2, 1}, {0, 0}, {2, 2}, {2, 1}, {2, 1}}}), true);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 1}, {1, 2}, {2, 2}, {0, 0}},
                {{1, 2}, {1, 2}, {2, 2}, {2, 1}, {2, 2}},
                {{1, 1}, {1, 2}, {2, 3}, {2, 2}, {0, 0}},
                {{0, 0}, {1, 3}, {2, 3}, {2, 3}, {2, 2}},
                {{1, 1}, {2, 1}, {2, 1}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{0, 0}, {2, 4}, {2, 2}, {0, 0}, {2, 1}},
                {{2, 2}, {2, 2}, {2, 3}, {2, 3}, {2, 2}},
                {{2, 2}, {0, 0}, {2, 2}, {0, 0}, {2, 2}},
                {{2, 2}, {2, 3}, {2, 3}, {2, 3}, {2, 1}},
                {{0, 0}, {2, 1}, {2, 1}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{2, 3}, {2, 3}, {2, 1}, {2, 2}, {2, 2}},
                {{2, 1}, {2, 2}, {2, 3}, {2, 2}, {2, 2}},
                {{2, 2}, {0, 0}, {2, 2}, {0, 0}, {2, 2}},
                {{2, 2}, {2, 3}, {2, 3}, {2, 3}, {2, 1}},
                {{0, 0}, {2, 1}, {2, 1}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{2, 1}, {2, 2}, {2, 1}, {2, 2}, {2, 2}},
                {{2, 2}, {0, 0}, {2, 3}, {2, 2}, {2, 2}},
                {{2, 2}, {2, 2}, {2, 1}, {2, 3}, {2, 2}},
                {{2, 1}, {2, 1}, {2, 3}, {2, 1}, {2, 2}},
                {{2, 3}, {0, 0}, {2, 1}, {2, 2}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 1}, {0, 0}, {1, 2}},
                {{1, 2}, {1, 3}, {1, 1}, {1, 4}, {1, 2}},
                {{1, 3}, {1, 2}, {1, 3}, {1, 2}, {0, 0}},
                {{1, 1}, {1, 3}, {1, 2}, {1, 3}, {1, 2}},
                {{0, 0}, {1, 1}, {1, 1}, {1, 1}, {1, 1}}}), true);
        map.put(new Board(new int[][][]{{{2, 1}, {2, 1}, {0, 0}, {2, 1}, {2, 1}},
                {{2, 1}, {2, 3}, {2, 2}, {2, 2}, {2, 1}},
                {{2, 1}, {2, 3}, {2, 2}, {2, 2}, {2, 2}},
                {{2, 2}, {2, 2}, {2, 2}, {1, 2}, {1, 1}},
                {{2, 1}, {0, 0}, {2, 1}, {1, 2}, {0, 0}}}), false);
        map.put(new Board(new int[][][]{{{0, 0}, {1, 2}, {1, 2}, {1, 1}, {1, 1}},
                {{2, 2}, {2, 1}, {2, 3}, {1, 2}, {1, 2}},
                {{2, 2}, {2, 3}, {0, 0}, {2, 2}, {1, 2}},
                {{2, 2}, {2, 3}, {2, 2}, {1, 1}, {1, 1}},
                {{2, 1}, {2, 1}, {2, 2}, {1, 1}, {2, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {0, 0}, {1, 2}, {0, 0}, {2, 1}},
                {{1, 1}, {1, 3}, {2, 3}, {2, 2}, {2, 1}},
                {{1, 1}, {1, 3}, {2, 2}, {2, 2}, {2, 1}},
                {{1, 2}, {2, 2}, {0, 0}, {2, 2}, {2, 1}},
                {{1, 1}, {0, 0}, {2, 1}, {0, 0}, {1, 1}}}), false);
        map.put(new Board(new int[][][]{{{1, 1}, {1, 2}, {1, 2}, {1, 2}, {1, 1}},
                {{1, 1}, {1, 1}, {1, 3}, {0, 0}, {1, 2}},
                {{2, 2}, {2, 3}, {1, 2}, {0, 0}, {0, 0}},
                {{2, 1}, {2, 1}, {2, 2}, {0, 0}, {2, 2}},
                {{2, 1}, {2, 2}, {2, 2}, {2, 2}, {2, 1}}}), false);
    }

    public Boolean get(Board board) {
        return map.get(board);
    }
}