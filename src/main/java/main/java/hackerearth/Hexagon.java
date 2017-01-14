package main.java.hackerearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Hexagon {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final int[][] board = new int[6][7];
        for (int i = 0; i < board.length; i++) {
            final String cols[] = bufferedReader.readLine().split(" ");
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = cols[j].charAt(0) - '0';
            }
        }
        final MinMax minMax = new MinMax(Integer.parseInt(bufferedReader.readLine()));
        System.out.println(minMax.iterativeSearchForBestMove(board, Integer.parseInt(bufferedReader.readLine())));
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
    }
}

class MinMax {
    private static final int MAX_DEPTH = 60, TERMINAL_DEPTH = 100;
    public static int TIME_OUT = 1000;
    public int computations = 0, depth = 1, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private static final boolean nullSearchActivated = false;
    private final int currentDepth;
    public int cacheHits;
    private boolean timeOut;

    MinMax(final int currentDepth) {
        this.currentDepth = currentDepth;
    }

    public String iterativeSearchForBestMove(final int[][] game, final int player) {
        Board.setThoseWithinSight();
        final Board board = new Board(game);
        if (board.places[player] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new Configuration[board.options[player]];
        for (int i = 0; i < startConfigs.length; i++) {
            startConfigs[i] = new Configuration(board.moves[player][i], board, 0, false);
        }
        Arrays.sort(startConfigs);
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player, 0);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.places[player];
        return bestMove.describe();
    }

    private Move findBestMove(final int player, final int level) {
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        try {
            final HashMap<Board, Integer> boards = new HashMap<>();
            for (final Configuration possibleConfig : startConfigs) {
                final Integer storedValue = boards.get(possibleConfig.board);
                final int moveValue;
                if (storedValue != null) {
                    cacheHits++;
                    moveValue = storedValue;
                } else {
                    moveValue = evaluate(possibleConfig.board,
                                         flip(player),
                                         level,
                                         toTake,
                                         toGive,
                                         -possibleConfig.strength,
                                         false);
                }
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
        if (board.isTerminated(player) || level >= depth || currentDepth + level > TERMINAL_DEPTH) {
            max = heuristicValue;
        } else {
            final Configuration[] configurations = new Configuration[board.options[player]];
            for (int i = 0; i < configurations.length; i++) {
                configurations[i] = new Configuration(board.moves[player][i],
                                                      board,
                                                      level,
                                                      isNullSearch);
            }
            Arrays.sort(configurations);
            final HashMap<Board, Integer> boards = new HashMap<>();
            for (final Configuration possibleConfig : configurations) {
                computations++;
                if (nullSearchActivated && !isNullSearch && !isEndGame(possibleConfig)) {
                    final int nullMoveValue = -evaluate(possibleConfig.board,
                                                        player,
                                                        level + 2,
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
                        if (nullMoveValue >= toGive) {
                            if (nullMoveValue > max) {
                                max = nullMoveValue;
                            }
                            continue;
                        }
                    }
                }
                final Integer storedValue = boards.get(possibleConfig.board);
                final int moveValue;
                if (storedValue != null) {
                    cacheHits++;
                    moveValue = storedValue;
                } else {
                    moveValue = evaluate(possibleConfig.board,
                                         flip(player),
                                         level + 1,
                                         toTake,
                                         toGive,
                                         -possibleConfig.strength,
                                         isNullSearch);
                }
                boards.put(possibleConfig.board, moveValue);
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

    private boolean isEndGame(Configuration configuration) {
        return configuration.board.places[configuration.move.player] < 5;
    }

    private class Configuration implements Comparable<Configuration> {
        final Move move;
        final Board board;
        int strength;
        final boolean killer;

        private Configuration(final Move move,
                              final Board board,
                              final int level,
                              final boolean resultsFromNullSearch) {
            this.board = board.getCopy().play(move);
            if (!resultsFromNullSearch
                    && (move.equals(killerMoves[level][0])
                    || move.equals(killerMoves[level][1]))) {
                killer = true;
            } else {
                this.strength = this.board.heuristicValue(move.player);
                killer = false;
            }
            this.move = move;
        }

        @Override
        public int compareTo(Configuration o) {
            if (killer && o.killer) {
                return 0;
            } else if (!killer && o.killer) {
                return +1;
            } else if (killer) {
                return -1;
            } else {
                return o.strength - strength;
            }
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "move=" + move +
                    ", board=" + board +
                    ", strength=" + strength +
                    ", killer=" + killer +
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

class Move {
    public static final int PRIME = 31;
    final int startX, startY, x, y, player;
    final boolean isAJump;

    public Move(final int startX, final int startY, final int x, final int y, final int player, final boolean isAJump) {
        this.startX = startX;
        this.startY = startY;
        this.x = x;
        this.y = y;
        this.player = player;
        this.isAJump = isAJump;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Move move = (Move) o;
        return startX == move.startX && startY == move.startY && x == move.x && y == move.y && player == move.player;
    }

    @Override
    public int hashCode() {
        return PRIME * (PRIME * (PRIME * (PRIME * startX + startY) + x) + y) + player;
    }

    String describe() {
        return startX + " " + startY + "\n" + x + " " + y;
    }

    @Override
    public String toString() {
        return "Move{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", x=" + x +
                ", y=" + y +
                ", player=" + player +
                ", isAJump=" + isAJump +
                '}';
    }
}

class Board {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int PLAYERS = 3;
    final int[][] board;
    final int places[];
    final int options[];
    final Move moves[][];
    private static final int neighbours[][][][] = new int[ROWS][COLS][2][];
    private static final int jumpables[][][][] = new int[ROWS][COLS][2][];
    private final int hashCode[];

    Board(final int[][] board) {
        this.board = board;
        places = new int[PLAYERS];
        moves = new Move[PLAYERS][756];
        options = new int[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int player = board[i][j];
                if (player != 0) {
                    places[player]++;
                    for (int k = 0; k < neighbours[i][j][0].length; k++) {
                        if (board[neighbours[i][j][0][k]][neighbours[i][j][1][k]] == 0) {
                            moves[player][options[player]++] = new Move(i,
                                                                        j,
                                                                        neighbours[i][j][0][k],
                                                                        neighbours[i][j][1][k],
                                                                        player,
                                                                        false);
                        }
                    }
                    for (int k = 0; k < jumpables[i][j][0].length; k++) {
                        if (board[jumpables[i][j][0][k]][jumpables[i][j][1][k]] == 0) {
                            moves[player][options[player]++] = new Move(i,
                                                                        j,
                                                                        jumpables[i][j][0][k],
                                                                        jumpables[i][j][1][k],
                                                                        player,
                                                                        true);
                        }
                    }
                }
            }
        }
        this.hashCode = getHashCode();
    }

    private Board(final int[][] board, final int[] places, final int options[], final Move[][] moves) {
        this.board = new int[ROWS][COLS];
        this.places = new int[PLAYERS];
        this.options = new int[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, COLS);
        }
        System.arraycopy(options, 0, this.options, 0, options.length);
        System.arraycopy(places, 0, this.places, 0, places.length);
        this.moves = new Move[PLAYERS][];
        for (int i = 1; i < PLAYERS; i++) {
            this.moves[i] = new Move[moves[i].length];
            System.arraycopy(moves[i], 0, this.moves[i], 0, options[i]);
        }
        this.hashCode = getHashCode();
    }

    private int[] getHashCode() {
        int hashCode[] = new int[3];
        for (int box = 0; box < hashCode.length; box++) {
            for (int i = 0; i < 14; i++) {
                hashCode[box] |= board[(box << 1) + i / COLS][i % COLS] << (i << 1);
            }
        }
        return hashCode;
    }

    public Board undo(final Move move) {
        return this;
    }

    public Board play(final Move move) {
        final int removeToHere[][] = new int[2][ROWS * COLS];
        final int removeFromHere[][] = new int[2][ROWS * COLS];
        final int addConnectionsToHere[][] = new int[2][ROWS * COLS];
        final int addConnectionsToElsewhere[][] = new int[2][ROWS * COLS];
        if (move.isAJump) {
            board[move.startX][move.startY] = 0;
            places[move.player]--;
            //remove all connections from here to elsewhere
            //add all connections within range to this point
        }
        final int opponent = MinMax.flip(move.player);
        board[move.x][move.y] = move.player;
        places[move.player]++;
        //add all connections from this point to elsewhere
        //remove all connections to here
        final int[][] neighbour = neighbours[move.x][move.y];
        for (int i = 0; i < neighbour[0].length; i++) {
            if (board[neighbour[0][i]][neighbour[1][i]] == opponent) {
                places[opponent]--;
                board[neighbour[0][i]][neighbour[1][i]] = move.player;
                places[move.player]++;
                //remove all connections from here to elsewhere
                //add all connections from this point to elsewhere
            }
        }
        return new Board(board);
    }

    public boolean isTerminated(final int player) {
        return options[player] == 0;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(board);
    }

    int heuristicValue(final int player) {
        return (isTerminated(player) ? 100 : 1) * places[player] - places[MinMax.flip(player)];
    }

    public static void setThoseWithinSight() {
        final int temps[][] = new int[2][6];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int count = 0;
                if (i > 0) {
                    temps[0][count] = i - 1;
                    temps[1][count] = j;
                    count++;
                    if (j % 2 == 0) {
                        if (j > 0) {
                            temps[0][count] = i - 1;
                            temps[1][count] = j - 1;
                            count++;
                        }
                        if (j < COLS - 1) {
                            temps[0][count] = i - 1;
                            temps[1][count] = j + 1;
                            count++;
                        }
                    }
                }
                if (i < ROWS - 1) {
                    temps[0][count] = i + 1;
                    temps[1][count] = j;
                    count++;
                    if (j % 2 == 1) {
                        if (j > 0) {
                            temps[0][count] = i + 1;
                            temps[1][count] = j - 1;
                            count++;
                        }
                        if (j < COLS - 1) {
                            temps[0][count] = i + 1;
                            temps[1][count] = j + 1;
                            count++;
                        }
                    }
                }
                if (j > 0) {
                    temps[0][count] = i;
                    temps[1][count] = j - 1;
                    count++;
                }
                if (j < COLS - 1) {
                    temps[0][count] = i;
                    temps[1][count] = j + 1;
                    count++;
                }
                neighbours[i][j][0] = new int[count];
                neighbours[i][j][1] = new int[count];
                System.arraycopy(temps[0], 0, neighbours[i][j][0], 0, count);
                System.arraycopy(temps[1], 0, neighbours[i][j][1], 0, count);
            }
        }

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final Set<Cell> tooClose = new HashSet<>();
                tooClose.add(new Cell(i, j));
                for (int k = 0; k < neighbours[i][j][0].length; k++) {
                    tooClose.add(new Cell(neighbours[i][j][0][k], neighbours[i][j][1][k]));
                }
                final Set<Cell> distantNeighbours = new HashSet<>();
                for (int k = 0; k < neighbours[i][j][0].length; k++) {
                    final int x = neighbours[i][j][0][k];
                    final int y = neighbours[i][j][1][k];
                    for (int l = 0; l < neighbours[x][y][0].length; l++) {
                        final Cell current = new Cell(neighbours[x][y][0][l], neighbours[x][y][1][l]);
                        if (!tooClose.contains(current)) {
                            distantNeighbours.add(current);
                        }
                    }
                }
                jumpables[i][j][0] = new int[distantNeighbours.size()];
                jumpables[i][j][1] = new int[distantNeighbours.size()];
                final List<Cell> distantNeighboursList = distantNeighbours.stream().collect(Collectors.toList());
                for (int k = 0; k < distantNeighboursList.size(); k++) {
                    jumpables[i][j][0][k] = distantNeighboursList.get(k).x;
                    jumpables[i][j][1][k] = distantNeighboursList.get(k).y;
                }
            }
        }
    }

    private static class Cell {
        final int x, y;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Cell cell = (Cell) o;
            return x == cell.x && y == cell.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        private Cell(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Board board = (Board) o;
        return hashCode[0] == board.hashCode[0] && hashCode[1] == board.hashCode[1] && hashCode[2] == board.hashCode[2];
    }

    @Override
    public int hashCode() {
        return 961 * hashCode[0] + 31 * hashCode[1] + hashCode[2];
    }

    public Board getCopy() {
        return new Board(board, places, options, moves);
    }
}