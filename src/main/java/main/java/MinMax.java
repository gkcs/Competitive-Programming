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
        System.out.println(minMax.computations + " " + minMax.cacheHits + " " + minMax.depth);
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
    private static final int COMPUTATION_LIMIT = 50000;
    public int computations = 0, cacheHits = 0, depth = 2;
    private final Map<Representation, Long> boards = new HashMap<>();
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;

    public String iterativeSearchForBestMove(int[][][] board, int player) {
        String bestMove = "LOL";
        for (; depth < 60 && computations < COMPUTATION_LIMIT; depth++) {
            try {
                bestMove = findBestMove(Board.getCopy(board), player, depth);
            } catch (Exception ignore) {
                break;
            } finally {
                boards.clear();
                Board.previousStates.clear();
            }
        }
        return bestMove;
    }

    static {
        Board.setNeighbours();
    }

    public MinMax() {
    }

    private String findBestMove(final int[][][] rawBoard, final int player, final int level) {
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
            final Representation representation = movedBoard.representation();
            if (boards.containsKey(representation)) {
                moveValue = boards.get(representation);
            } else {
                final Integer terminalValue = movedBoard.terminalValue();
                if (terminalValue != null) {
                    moveValue = value(terminalValue, possibleMove.player);
                } else {
                    moveValue = evaluate(movedBoard, flip(possibleMove.player), level);
                }
                populateMap(moveValue, movedBoard);
            }
            movedBoard.undo();
            if (moveValue > max) {
                max = moveValue;
                bestMove = possibleMove;
                if (max == MAX_VALUE) {
                    break;
                }
            }
        }
        return bestMove.describe();
    }

    private void populateMap(long moveValue, final Board movedBoard) {
        Arrays.stream(movedBoard.getOrientations()).forEach(orientation -> boards.put(orientation, moveValue));
    }

    private long evaluate(final Board board, final int player, final int level) {
        long max = MIN_VALUE;
        if (computations > COMPUTATION_LIMIT) {
            throw new RuntimeException("Time out...");
        }
        if (level <= 0) {
            max = board.heuristicValue(player);
        } else {
            for (final Move possibleMove : board.getAllPossibleMoves(player)) {
                final long moveValue;
                final Board movedBoard = board.makeMove(possibleMove);
                final Representation representation = movedBoard.representation();
                if (boards.containsKey(representation)) {
                    moveValue = boards.get(representation);
                    cacheHits++;
                } else {
                    final Integer terminalValue = movedBoard.terminalValue();
                    if (terminalValue != null) {
                        moveValue = value(terminalValue, possibleMove.player);
                    } else {
                        computations++;
                        moveValue = evaluate(movedBoard, flip(possibleMove.player), level - 1);
                    }
                    populateMap(moveValue, movedBoard);
                }
                movedBoard.undo();
                if (moveValue > max) {
                    max = moveValue;
                    if (max == MAX_VALUE) {
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
    static final List<int[][][]> previousStates = new ArrayList<>();
    private int[][][] board;
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
        return Arrays.deepToString(board);
    }

    Representation[] getOrientations() {
        final byte[] representation = new byte[13];
        final byte[] flipAlongX = new byte[13];
        final byte[] flipAlongY = new byte[13];
        final byte[] flipAlongDiag = new byte[13];
        final byte[] flipAlongRevDiag = new byte[13];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int offset = (BOARD_SIZE * i + j) << 2;
                int offsetX = (BOARD_SIZE * (BOARD_SIZE - i - 1) + j) << 2;
                int offsetY = (BOARD_SIZE * i + (BOARD_SIZE - j - 1)) << 2;
                int offsetDiag = (BOARD_SIZE * j + i) << 2;
                int offsetRevDiag = (BOARD_SIZE * (BOARD_SIZE - i - 1) + (BOARD_SIZE - j - 1)) << 2;
                representation[offset >> 3] |= board[i][j][0] << (offset & 7);
                flipAlongX[offsetX >> 3] |= board[i][j][0] << (offsetX & 7);
                flipAlongY[offsetY >> 3] |= board[i][j][0] << (offsetY & 7);
                flipAlongDiag[offsetDiag >> 3] |= board[i][j][0] << (offsetDiag & 7);
                flipAlongRevDiag[offsetRevDiag >> 3] |= board[i][j][0] << (offsetRevDiag & 7);
                offset += 2;
                offsetX += 2;
                offsetY += 2;
                offsetDiag += 2;
                offsetRevDiag += 2;
                representation[offset >> 3] |= board[i][j][1] << (offset & 7);
                flipAlongX[offsetX >> 3] |= board[i][j][1] << (offsetX & 7);
                flipAlongY[offsetY >> 3] |= board[i][j][1] << (offsetY & 7);
                flipAlongDiag[offsetDiag >> 3] |= board[i][j][1] << (offsetDiag & 7);
                flipAlongRevDiag[offsetRevDiag >> 3] |= board[i][j][1] << (offsetRevDiag & 7);
            }
        }
        return new Representation[]{
                new Representation(representation),
                new Representation(flipAlongX),
                new Representation(flipAlongY),
                new Representation(flipAlongDiag),
                new Representation(flipAlongRevDiag)};
    }

    int heuristicValue(final int player) {
        final Integer terminalValue = terminalValue();
        if (terminalValue != null) {
            return terminalValue;
        }
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
        return compactRepresentation();
    }

    private Representation compactRepresentation() {
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
        return new Representation(representation);
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
    private final byte[] representation;

    @Override
    public boolean equals(Object o) {
        return Arrays.equals(representation, ((Representation) o).representation);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(representation);
    }

    Representation(final byte[] representation) {
        this.representation = representation;
    }
}