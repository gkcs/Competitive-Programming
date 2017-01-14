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
        final int player = Integer.parseInt(bufferedReader.readLine());
        System.out.println(minMax.iterativeSearchForBestMove(board, player));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }
}

class MinMax {
    private static final int MAX_DEPTH = 60, TERMINAL_DEPTH = 100;
    public static int TIME_OUT = 1280;
    public int computations = 0, depth = 4, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private static final boolean nullSearchActivated = false;
    private final int currentDepth;
    private boolean timeOut;

    MinMax(final int currentDepth) {
        this.currentDepth = currentDepth;
    }

    public String iterativeSearchForBestMove(final int[][] game, final int player) {
        Board.setThoseWithinSight();
        final Board board = new Board(game);
        if (board.places[0] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new Configuration[board.moves[player].length];
        for (int i = 0; i < board.places[0]; i++) {
            startConfigs[i] = new Configuration(board.moves[player][i], board, 0, false);
        }
        Arrays.sort(startConfigs);
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player, 0);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.places[0];
        return bestMove.describe();
    }

    private Move findBestMove(final int player, final int level) {
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        try {
            for (final Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(possibleConfig.board,
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
        if ((terminalValue = board.terminalValue(player)) != 0) {
            max = terminalValue * ((-player << 1) + 3);
            max += max < 0 ? level : -level;
        } else if (level >= depth || currentDepth + level > TERMINAL_DEPTH) {
            max = heuristicValue;
        } else {
            final Configuration[] configurations = new Configuration[board.moves[player].length];
            for (int i = 0; i < configurations.length; i++) {
                configurations[i] = new Configuration(board.moves[player][i],
                                                      board,
                                                      level,
                                                      isNullSearch);
            }
            Arrays.sort(configurations);
            for (final Configuration possibleConfig : configurations) {
                computations++;
                if (nullSearchActivated && !isNullSearch && !isEndGame(possibleConfig)) {
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
                        if (nullMoveValue >= toGive) {
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

    private boolean isEndGame(Configuration configuration) {
        return configuration.board.places[0] < 5;
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
                    && (killerMoves[level][0].equals(move)
                    || killerMoves[level][1].equals(move))) {
                killer = true;
            } else {
                this.strength = board.heuristicValue(move.player);
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
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int PLAYERS = 3;
    final int[][] board;
    final int places[];
    final int options[];
    final Move moves[][];
    private static final int neighbours[][][][] = new int[ROWS][COLS][2][];
    private static final int jumpables[][][][] = new int[ROWS][COLS][2][];

    Board(final int[][] board) {
        this.board = board;
        places = new int[PLAYERS];
        moves = new Move[PLAYERS][756];
        options = new int[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int player = board[i][j];
                places[player]++;
                if (player > 0) {
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
    }

    private Board(final int[][] board, int[] places, int options[], final Move[][] moves) {
        this.board = board;
        this.places = new int[PLAYERS];
        this.options = new int[PLAYERS];
        System.arraycopy(options, 0, this.options, 0, options.length);
        System.arraycopy(places, 0, this.places, 0, places.length);
        this.moves = new Move[PLAYERS][];
        for (int i = 0; i < PLAYERS; i++) {
            this.moves[i] = new Move[moves.length];
            for (int j = 0; j < this.moves[i].length; j++) {
                final Move move = moves[i][j];
                this.moves[i][j] = new Move(move.startX, move.startY, move.x, move.y, move.player, move.isAJump);
            }
        }
    }

    public Board undo(final Move move) {
        return this;
    }

    public Board play(final Move move) {
        if (move.isAJump) {
            board[move.startX][move.startY] = 0;
        }
        board[move.x][move.y] = move.player;
        return this;
    }

    public Integer terminalValue(final int player) {
        final int opponent = MinMax.flip(player);
        if (places[player] == ROWS * COLS) {
            return MinMax.MAX_VALUE;
        } else if (places[opponent] == ROWS * COLS) {
            return MinMax.MIN_VALUE;
        } else if (places[0] > 0) {
            return 0;
        } else {
            return MinMax.MIN_VALUE;
        }
    }

    @Override
    public String toString() {
        return Arrays.deepToString(board);
    }

    int heuristicValue(final int player) {
        return 100 * (places[player] - places[MinMax.flip(player)]);
    }

    public static void setThoseWithinSight() {
        final int temps[][] = new int[2][6];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int count = 0;
                if (i > 0) {
                    if (j > 0) {
                        temps[0][count] = i - 1;
                        temps[1][count] = j - 1;
                        count++;
                    }
                    temps[0][count] = i - 1;
                    temps[1][count] = j;
                    count++;
                    if (j < COLS - 1) {
                        temps[0][count] = i - 1;
                        temps[1][count] = j + 1;
                        count++;
                    }
                }
                if (i < ROWS - 1) {
                    temps[0][count] = i + 1;
                    temps[1][count] = j;
                    count++;
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

    public Board getCopy() {
        return new Board(board, places, options, moves);
    }
}