package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.cacheHits + " " + minMax.cacheSize);
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
    private static final int COMPUTATION_LIMIT = 65000;
    private static final int SIZE_THRESHOLD = 200000;
    public int computations = 0, cacheHits = 0, depth = 3, moves = 0, cacheSize = 0;
    public long eval = 0;
    private final Map<Representation, Long> boards = new HashMap<>();
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;

    public String iterativeSearchForBestMove(int[][][] board, int player) {
        String bestMove = "LOL";
        while (depth < 60 && computations < COMPUTATION_LIMIT) {
            try {
                bestMove = findBestMove(Board.getCopy(board), player, depth);
            } catch (Exception e) {
                if (!e.getMessage().equals("Time out...")) {
                    bestMove = e.getMessage() + " " + e.getClass().getSimpleName();
                }
                break;
            } finally {
                cacheSize = boards.size();
                boards.clear();
                Board.previousStates.clear();
                depth++;
            }
        }
        return bestMove;
    }

    static {
        Board.setNeighbours();
        Board.fillInCoordinates();
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
            populateMap(moveValue, movedBoard);
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

    private void populateMap(long moveValue, final Board movedBoard) {
        if (boards.size() < SIZE_THRESHOLD) {
            movedBoard.getOrientations().forEach(orientation -> boards.put(orientation, moveValue));
        }

    }

    private long evaluate(final Board board, final int player, final int level, final long a, final long b) {
        long toTake = a, toGive = b;
        long max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= 900) {
            throw new RuntimeException("Time out...");
        }
        final Integer terminalValue;
        if (boards.containsKey(board.representation())) {
            max = boards.get(board.representation());
            cacheHits++;
        } else if ((terminalValue = board.terminalValue()) != null) {
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
                populateMap(moveValue, movedBoard);
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
        final StringBuilder stringBuilder = new StringBuilder();
        for (final int row[][] : board) {
            stringBuilder.append('(');
            for (final int col[] : row) {
                stringBuilder.append('(');
                for (final int content : col) {
                    stringBuilder.append(content).append(',');
                }
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                stringBuilder.append(')').append(',');
            }
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.append(')').append(',').append('\n');
        }
        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        return stringBuilder.toString();
    }

    private static final int[][][][] coordinates = new int[5][BOARD_SIZE][BOARD_SIZE][2];
    private static final int[][] order = new int[][]{
            {0, 1}, {0, 2}, {0, 3},
            {1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4},
            {2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4},
            {3, 0}, {3, 1}, {3, 2}, {3, 3}, {3, 4},
            {4, 1}, {4, 2}, {4, 3}
    };

    private static final int[][] cornerOrder = new int[][]{
            {0, 0}, {0, 4}, {4, 0}, {4, 4}
    };

    static void fillInCoordinates() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                coordinates[0][i][j][0] = i;
                coordinates[0][i][j][1] = j;
                coordinates[1][i][j][0] = BOARD_SIZE - i - 1;
                coordinates[1][i][j][1] = j;
                coordinates[2][i][j][0] = i;
                coordinates[2][i][j][1] = BOARD_SIZE - j - 1;
                coordinates[3][i][j][0] = j;
                coordinates[3][i][j][1] = i;
                coordinates[4][i][j][0] = BOARD_SIZE - i - 1;
                coordinates[4][i][j][1] = BOARD_SIZE - j - 1;
            }
        }
    }

    List<Representation> getOrientations() {
        final long[] middle = new long[5];
        final byte[] corners = new byte[middle.length];

        for (int k = 0; k < middle.length; k++) {
            for (int i = 0; i < order.length; i++) {
                final int[] coordinate = coordinates[k][order[i][0]][order[i][1]];
                middle[k] |= (long) mapping(board[coordinate[0]][coordinate[1]]) << i * 3;
            }
            for (int i = 0; i < cornerOrder.length; i++) {
                int[] cornerCoordinate = coordinates[k][cornerOrder[i][0]][cornerOrder[i][1]];
                corners[k] |= cornerMapping(board[cornerCoordinate[0]][cornerCoordinate[1]]) << (i << 1);
            }
        }
        final List<Representation> representations = new ArrayList<>();
        for (int i = 0; i < middle.length; i++) {
            representations.add(new Representation(middle[i], corners[i]));
        }
        return representations;
    }

    private int mapping(final int[] col) {
        return col[0] == 0 ? 0 : (col[0] - 1) * 3 + col[1];
    }

    private int cornerMapping(final int[] col) {
        return col[0] == 0 ? 0 : (col[0] - 1) + col[1];
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
        return orbs + inThreat + bonus + (contiguous << 1);
    }

    Representation representation() {
        long middle = 0;
        byte corners = 0;
        for (int i = 0; i < order.length; i++) {
            middle |= (long) mapping(board[order[i][0]][order[i][1]]) << i * 3;
        }
        for (int i = 0; i < cornerOrder.length; i++) {
            corners |= cornerMapping(board[cornerOrder[i][0]][cornerOrder[i][1]]) << (i << 1);
        }
        return new Representation(middle, corners);
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
}

class Representation {
    private final long middle;
    private final byte corners;

    Representation(final long middle, final byte corners) {
        this.middle = middle;
        this.corners = corners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        Representation that = (Representation) o;

        return middle == that.middle && corners == that.corners;

    }

    @Override
    public int hashCode() {
        int result = (int) (middle ^ (middle >>> 32));
        result = 31 * result + (int) corners;
        return result;
    }
}