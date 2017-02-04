package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A game playing bot for Chain Reaction. Takes as input an array of {@link Board#BOARD_SIZE}*{@link Board#BOARD_SIZE}.
 * Each cell is represented by (ORB_COUNT,PLAYER). Takes time = {@link MinMax#TIME_OUT} to return an answer.
 *
 * @author Gaurav Sen
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
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }
}

/**
 * Contains a lot of objects for metrics. Should ideally be separated from those responsibilities.
 */
public class MinMax {
    private static final int MAX_DEPTH = 60;
    public static int TIME_OUT = 1280;
    public int computations = 0, depth = 4, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private boolean nullSearchActivated = false;

    public MinMax() {
        Board.setMoves();
        Board.setNeighbours();
    }

    private boolean timeOut;

    /**
     * Iterative deepening is implemented for flexible depth search. Also, it allows us to rearrange all moves as per
     * (known) optimal ordering after each iteration. This is important because alpha-beta
     * performs best when given a good move order.
     * On the final iteration, when an exception is thrown, the best move will be propagated upwards from the
     * {@link #findBestMove} method.
     */
    public String iterativeSearchForBestMove(final int[][][] game, final int player) {
        final Board board = new Board(game);
        if (board.choices[player] + board.choices[0] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new Configuration[board.choices[player] + board.choices[0]];
        for (int i = 0; i < board.choices[0]; i++) {
            startConfigs[i] = new Configuration(board.moves[0][i], board, player, 0, false);
        }
        for (int i = 0; i < board.choices[player]; i++) {
            startConfigs[i + board.choices[0]] = new Configuration(board.moves[player][i], board, player, 0, false);
        }
        Arrays.sort(startConfigs);
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player, 0);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.choices[player] + board.choices[0];
        return bestMove.describe();
    }

    /**
     * Returns the best known move till now for the entire board.
     *
     * @param player Player to play
     * @param level  Current Level
     * @return Best move found
     */
    private Move findBestMove(final int player, final int level) {
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        try {
            for (final Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(possibleConfig.board.getCopy(),
                                               flip(player),
                                               level,
                                               toTake,
                                               toGive,
                                               -possibleConfig.strength,
                                               false);
                possibleConfig.strength = moveValue;
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
                    bestMove = possibleConfig.move;
                    if (Math.abs(max - MAX_VALUE) <= 100) {
                        break;
                    }
                }
                if (toTake >= toGive) {
                    if (possibleConfig.killer) {
                        if (killerMoves[level][0] == possibleConfig.move) {
                            efficiency[level][0]++;
                        } else {
                            efficiency[level][1]++;
                            if (efficiency[level][0] < efficiency[level][1]) {
                                final Move temp = killerMoves[level][0];
                                killerMoves[level][0] = killerMoves[level][1];
                                killerMoves[level][1] = temp;
                            }
                        }
                    } else {
                        if (killerMoves[level][0] == null) {
                            killerMoves[level][0] = possibleConfig.move;
                            efficiency[level][0] = 1;
                        } else if (killerMoves[level][1] == null) {
                            killerMoves[level][1] = possibleConfig.move;
                            efficiency[level][1] = 1;
                        }
                    }
                    break;
                } else if (possibleConfig.killer) {
                    if (killerMoves[level][0] == possibleConfig.move) {
                        efficiency[level][0]--;
                    } else {
                        efficiency[level][1]--;
                    }
                    if (efficiency[level][0] < efficiency[level][1]) {
                        final Move temp = killerMoves[level][0];
                        killerMoves[level][0] = killerMoves[level][1];
                        killerMoves[level][1] = temp;
                    }
                    if (efficiency[level][1] <= 0) {
                        efficiency[level][1] = 0;
                        killerMoves[level][1] = null;
                    }
                }
            }
        } catch (TimeoutException e) {
            timeOut = true;
        }
        Arrays.sort(startConfigs);
        return bestMove;
    }

    /**
     * Min Max tree generator and traverse.  Implements Alpha Beta along with the killer heuristic.
     *
     * @param board          Input Board. All branches in the Min Max Tree from this node are possible moves from this board.
     * @param player         Player making the move.
     * @param level          Depth on which this tree is now.
     * @param a              Alpha
     * @param b              Beta
     * @param heuristicValue The heuristic value of board
     * @param isNullSearch   Specifies if the current search had a null move in it
     * @return The value of current board position
     * @throws TimeoutException if it runs out of time.
     */
    private int evaluate(final Board board,
                         final int player,
                         final int level,
                         final long a,
                         final long b,
                         final int heuristicValue,
                         final boolean isNullSearch) throws TimeoutException {
        long toTake = a, toGive = b;
        int max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            throw new TimeoutException("Time out...");
        }
        final Integer terminalValue;
        if ((terminalValue = board.terminalValue()) != null) {
            max = terminalValue * ((-player << 1) + 3);
            max += max < 0 ? level : -level;
        } else if (level >= depth) {
            max = heuristicValue;
        } else {
            final Configuration[] configurations = new Configuration[board.choices[player] + board.choices[0]];
            for (int i = 0; i < board.choices[0]; i++) {
                configurations[i] = new Configuration(board.moves[0][i], board, player, level, isNullSearch);
            }
            for (int i = 0; i < board.choices[player]; i++) {
                configurations[i + board.choices[0]] = new Configuration(board.moves[player][i],
                                                                         board,
                                                                         player,
                                                                         level,
                                                                         isNullSearch);
            }
            Arrays.sort(configurations);
            int index = 0;
            for (; index < configurations.length; index++) {
                final Configuration possibleConfig = configurations[index];
                computations++;
                if (nullSearchActivated && !isNullSearch && isNotEndGame(possibleConfig)) {
                    final int nullMoveValue = -evaluate(possibleConfig.board,
                                                        player,
                                                        level + 3,
                                                        player == 1 ? toTake : toGive - 1,
                                                        player == 1 ? toTake + 1 : toGive,
                                                        possibleConfig.strength,
                                                        true);
                    if (player == 1) {
                        if (nullMoveValue <= toTake) {
                            if (nullMoveValue > max) {
                                max = nullMoveValue;
                            }
                            continue;
                        }
                    } else {
                        if (-nullMoveValue >= toGive) {
                            if (nullMoveValue > max) {
                                max = nullMoveValue;
                            }
                            continue;
                        }
                    }
                }
                final int moveValue = evaluate(possibleConfig.board,
                                               flip(player),
                                               level + 1,
                                               toTake,
                                               toGive,
                                               -possibleConfig.strength,
                                               isNullSearch);
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
                    if (possibleConfig.killer) {
                        if (killerMoves[level][0] == possibleConfig.move) {
                            efficiency[level][0]++;
                        } else {
                            efficiency[level][1]++;
                            if (efficiency[level][0] < efficiency[level][1]) {
                                final Move temp = killerMoves[level][0];
                                killerMoves[level][0] = killerMoves[level][1];
                                killerMoves[level][1] = temp;
                            }
                        }
                    } else {
                        if (killerMoves[level][0] == null) {
                            killerMoves[level][0] = possibleConfig.move;
                            efficiency[level][0] = 1;
                        } else if (killerMoves[level][1] == null) {
                            killerMoves[level][1] = possibleConfig.move;
                            efficiency[level][1] = 1;
                        }
                    }
                    break;
                } else if (possibleConfig.killer) {
                    if (killerMoves[level][0] == possibleConfig.move) {
                        efficiency[level][0]--;
                    } else {
                        efficiency[level][1]--;
                    }
                    if (efficiency[level][0] < efficiency[level][1]) {
                        final Move temp = killerMoves[level][0];
                        killerMoves[level][0] = killerMoves[level][1];
                        killerMoves[level][1] = temp;
                    }
                    if (efficiency[level][1] <= 0) {
                        efficiency[level][1] = 0;
                        killerMoves[level][1] = null;
                    }
                }
            }
        }
        return -max;
    }

    private boolean isNotEndGame(Configuration configuration) {
        return configuration.board.choices[0] > 5;
    }

    /**
     * A board and move combination.
     */
    private class Configuration implements Comparable<Configuration> {
        final Move move;
        final Board board;
        /**
         * Represents how good the move is for the player making the move
         */
        int strength;
        /**
         * True only if the move is considered a 'killer' move as per the killer heuristic.
         */
        final boolean killer;

        private Configuration(final Move move,
                              final Board board,
                              final int player,
                              final int level,
                              boolean resultsFromNullSearch) {
            final Move moveToBeMade = Board.ALL_MOVES[player][move.x][move.y];
            this.board = board.makeMove(moveToBeMade);
            if (!resultsFromNullSearch && killerMoves[level][0] == moveToBeMade || killerMoves[level][1] == moveToBeMade) {
                killer = true;
            } else {
                this.strength = this.board.heuristicValue(player);
                killer = false;
            }
            this.move = moveToBeMade;
        }

        @Override
        public int compareTo(Configuration o) {
            if (killer && o.killer) {
                return 0;
            } else if (!killer && o.killer) {
                return +1;
            } else if (killer) {
                return -1;
            }
            return o.strength - strength;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "move=" + move +
                    ", board=" + board +
                    '}';
        }
    }

    static int flip(final int player) {
        return ~player & 3;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}

/**
 * Represents a move on the board.
 */
class Move {
    final int x, y, player;

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
}

/**
 * A representation of the board as bit array would be better. Some analysis states that only 7 configurations are
 * possible for each cell:
 * <p>
 * Config Orbs Player
 * <p>
 * 0    0      0
 * <p>
 * 1    1      1
 * <p>
 * 2    2      1
 * <p>
 * 3    3      1
 * <p>
 * 4    1      2
 * <p>
 * 5    2      2
 * <p>
 * 6    3      2
 * <p>
 * So each board cell can be represented by log(7) base 2 => 3 bits. As there are 25 cells in a 5*5 board, each board
 * should require just 75 bits, or three integers.
 * However, due to performance and complexity considerations, I believe 4 bits per position is better. 2 for player
 * info and 2 for orb count. The practical reality was that none of these considerations worked well enough to reach
 * the final submission. However, if the bugs were fewer and I had more time, this was a good place to work on
 * efficiency.
 */
class Board {
    Function<int[], Integer> heuristicEval = (vals) -> Arrays.stream(vals).sum();
    int[][][] board;
    private static final int BOARD_SIZE = 5;
    private static final int neighbours[][][] = new int[BOARD_SIZE][BOARD_SIZE][];
    private static final int PLAYERS = 3;
    final Move[][] moves = new Move[PLAYERS][BOARD_SIZE * BOARD_SIZE];
    final int[] choices = new int[PLAYERS];
    static final Move ALL_MOVES[][][] = new Move[PLAYERS][BOARD_SIZE][BOARD_SIZE];

    /**
     * Creates a new board using the given board array to initialize move lists and counters.
     *
     * @param board the game board
     */
    Board(final int[][][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                moves[board[i][j][0]][choices[board[i][j][0]]++] = ALL_MOVES[board[i][j][0]][i][j];
            }
        }
        this.board = getCopy(board);
    }

    /**
     * Completely copies a board onto another.
     *
     * @param board   Original Board
     * @param moves   Original Move list
     * @param choices Original Player Cell counter
     */
    private Board(final int[][][] board, final Move[][] moves, final int choices[]) {
        System.arraycopy(choices, 0, this.choices, 0, choices.length);
        for (int i = 0; i < PLAYERS; i++) {
            System.arraycopy(moves[i], 0, this.moves[i], 0, choices[i]);
        }
        this.board = getCopy(board);
    }

    /**
     * Sets all the neighbours of each possible cell in the chain reaction board. This method runs only once for each
     * game.
     */
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

    /**
     * Make a move returning a new board. Method <b>is</b> idempotent.
     *
     * @param move Move to be played
     * @return New board with move played.
     */
    Board makeMove(final Move move) {
        return getCopy().play(move);
    }

    /**
     * Plays a move on the current board, updating the state and respective variables. If it looks complicated, thats
     * because it is.
     *
     * @param move The move played on the board.
     * @return The changed board. This operation is <b>NOT</b> idempotent.
     */
    private Board play(final Move move) {
        if (board[move.x][move.y][0] == MinMax.flip(move.player)) {
            //We just captured an opponents block. Updating move list and counters
            final int opponent = MinMax.flip(move.player);
            int index;
            for (index = choices[opponent] - 1; index >= 0; index--) {
                if (moves[opponent][index].x == move.x && moves[opponent][index].y == move.y) {
                    break;
                }
            }
            moves[opponent][index] = moves[opponent][choices[opponent] - 1];
            choices[opponent]--;
            moves[move.player][choices[move.player]++] = ALL_MOVES[move.player][move.x][move.y];
        } else if (board[move.x][move.y][0] == 0) {
            //We just captured an an empty block. Updating move list and counters
            int index;
            for (index = choices[0] - 1; index >= 0; index--) {
                if (moves[0][index].x == move.x && moves[0][index].y == move.y) {
                    break;
                }
            }
            moves[0][index] = moves[0][choices[0] - 1];
            choices[0]--;
            moves[move.player][choices[move.player]++] = ALL_MOVES[move.player][move.x][move.y];
        }
        //Else we played in our own cell. No updates needed, except to increment cell count as always
        board[move.x][move.y][0] = move.player;
        board[move.x][move.y][1]++;
        if (terminalValue() != null) {
            return this;
        }
        /*
         * Checks if an explosion needed.
         */
        if (neighbours[move.x][move.y].length <= board[move.x][move.y][1]) {
            board[move.x][move.y][1] = board[move.x][move.y][1] - neighbours[move.x][move.y].length;
            if (board[move.x][move.y][1] == 0) {
                //Set he cell to blank and update move lists
                board[move.x][move.y][0] = 0;
                int index;
                for (index = choices[move.player] - 1; index >= 0; index--) {
                    if (moves[move.player][index].x == move.x && moves[move.player][index].y == move.y) {
                        break;
                    }
                }
                moves[move.player][index] = moves[move.player][choices[move.player] - 1];
                choices[move.player]--;
                moves[0][choices[0]++] = ALL_MOVES[0][move.x][move.y];
            }
            explode(move.x, move.y, move.player);
        }
        return this;
    }

    /**
     * Explode the cell at the specified position. All neighbouring cells are acted upon as if a move was played on
     * them.
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param player Player who caused the explosion
     */
    private void explode(final int x, final int y, final int player) {
        for (final int neighbour : neighbours[x][y]) {
            play(ALL_MOVES[player][neighbour / BOARD_SIZE][neighbour % BOARD_SIZE]);
        }
    }

    /**
     * Used to check if the given board position is terminal.
     *
     * @return An integer value if the position is a terminal position. Else return null.
     */
    Integer terminalValue() {
        if (((choices[1] | choices[2]) > 1) && (choices[1] == 0 || choices[2] == 0)) {
            return choices[1] == 0 ? MinMax.MIN_VALUE : MinMax.MAX_VALUE;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return Arrays.deepToString(board);
    }

    /**
     * It takes the difference in number of cells and add the difference in explosives.
     *
     * @param player Player to move
     * @return Heuristic value of the board
     */

    int heuristicValue(final int player) {
        final int opponent = MinMax.flip(player);
        int orbs = choices[player] - choices[opponent];
        int explosives = 0;
        for (int m = 0; m < choices[player]; m++) {
            final int i = moves[player][m].x;
            final int j = moves[player][m].y;
            if (board[i][j][1] == neighbours[i][j].length - 1) {
                explosives++;
            }
        }
        for (int m = 0; m < choices[opponent]; m++) {
            final int i = moves[opponent][m].x;
            final int j = moves[opponent][m].y;
            if (board[i][j][1] == neighbours[i][j].length - 1) {
                explosives--;
            }
        }
        return orbs + explosives;
    }

    /**
     * Returns a copy of the board state. Skips copying the zeros of the original.
     *
     * @param board The original board representation
     * @return A new board array having all the copied elements
     */

    private int[][][] getCopy(final int board[][][]) {
        final int copyBoard[][][] = new int[board.length][board.length][2];
        for (int k = 1; k < PLAYERS; k++) {
            for (int l = 0; l < choices[k]; l++) {
                final int i = moves[k][l].x;
                final int j = moves[k][l].y;
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        return copyBoard;
    }

    /**
     * Initializes the moves array with static objects. These are the only move objects created in the entire game.
     */
    static void setMoves() {
        for (int player = 0; player < PLAYERS; player++) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    ALL_MOVES[player][i][j] = new Move(i, j, player);
                }
            }
        }
    }

    /**
     * Necessary to keep the preserve the state of the board when searching in the min-max tree.
     *
     * @return A copy of the board. The copy refers to none of the mutable objects being referred to by the original.
     */
    Board getCopy() {
        return new Board(board, moves, choices);
    }
}
