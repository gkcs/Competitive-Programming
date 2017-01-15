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
    public static final int MAX_DEPTH = 60, TERMINAL_DEPTH = 100;
    public static int TIME_OUT = 1000;
    public int computations = 0, depth = 3, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private static final boolean nullSearchActivated = true;
    private final int currentDepth;
    public int cacheHits;
    private boolean timeOut;

    MinMax(final int currentDepth) {
        Board.setCells();
        Board.setThoseWithinSight();
        this.currentDepth = currentDepth;
    }

    public String iterativeSearchForBestMove(final int[][] game, final int player) {
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
        while (depth < MAX_DEPTH && !timeOut && depth + currentDepth <= TERMINAL_DEPTH) {
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
            final Map<Board, Integer> boards = new HashMap<>();
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
                    boards.put(possibleConfig.board, moveValue);
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
        if (board.isTerminated(player, level, currentDepth)) {
            max = (board.places[player] - board.places[MinMax.flip(player)]) * MAX_VALUE;
        } else if (level >= depth) {
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
            final Map<Board, Integer> boards = new HashMap<>();
            for (final Configuration possibleConfig : configurations) {
                computations++;
                if (nullSearchActivated && !isNullSearch && !isEndGame(possibleConfig.board.stable) && level + 2 < depth) {
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
                        if (-nullMoveValue >= toGive) {
                            if (-nullMoveValue > max) {
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
                    boards.put(possibleConfig.board, moveValue);
                }
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

    private boolean isEndGame(final int[] stableSquares) {
        return stableSquares[1] + stableSquares[2] > 33;
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
            if (!killer && o.killer) {
                return +1;
            } else if (killer && !o.killer) {
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
    final int stable[];
    final int edges[];
    final Move moves[][];
    private static final Cell neighbours[][][] = new Cell[ROWS][COLS][];
    private static final Cell jumpables[][][] = new Cell[ROWS][COLS][];
    private static final Cell CELLS[][] = new Cell[ROWS][COLS];
    public final int[] hashCode;

    Board(final int[][] board) {
        this.board = board;
        places = new int[PLAYERS];
        moves = new Move[PLAYERS][710];
        options = new int[PLAYERS];
        stable = new int[PLAYERS];
        edges = new int[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int player = board[i][j];
                if (player != 0) {
                    places[player]++;
                    final Cell[] neighbour = neighbours[i][j];
                    edges[player] = edges[player] + (6 - neighbour.length);
                    boolean isStable = true;
                    for (final Cell aNeighbour : neighbour) {
                        if (board[aNeighbour.x][aNeighbour.y] == 0) {
                            moves[player][options[player]++] = new Move(i,
                                                                        j,
                                                                        aNeighbour.x,
                                                                        aNeighbour.y,
                                                                        player,
                                                                        false);
                            isStable = false;
                        }
                    }
                    if (isStable) {
                        stable[player]++;
                    }
                    final Cell[] extendedNeighbour = jumpables[i][j];
                    for (final Cell anExtendedNeighbour : extendedNeighbour) {
                        if (board[anExtendedNeighbour.x][anExtendedNeighbour.y] == 0) {
                            moves[player][options[player]++] = new Move(i,
                                                                        j,
                                                                        anExtendedNeighbour.x,
                                                                        anExtendedNeighbour.y,
                                                                        player,
                                                                        true);
                        }
                    }
                }
            }
        }
        this.hashCode = getHashCode();
    }

    private Board(final int[][] board,
                  final int[] places,
                  final int options[],
                  final Move[][] moves,
                  final int[] hashCode,
                  final int[] stable,
                  final int[] edges) {
        this.stable = new int[PLAYERS];
        this.edges = new int[PLAYERS];
        this.board = new int[ROWS][COLS];
        this.places = new int[PLAYERS];
        this.options = new int[PLAYERS];
        this.hashCode = new int[hashCode.length];
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
        System.arraycopy(hashCode, 0, this.hashCode, 0, hashCode.length);
        System.arraycopy(stable, 0, this.stable, 0, stable.length);
        System.arraycopy(edges, 0, this.edges, 0, edges.length);
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
        if (move.isAJump) {
            board[move.startX][move.startY] = 0;
            places[move.player]--;
        }
        final int opponent = MinMax.flip(move.player);
        board[move.x][move.y] = move.player;
        places[move.player]++;
        final Cell[] neighbour = neighbours[move.x][move.y];
        for (final Cell aNeighbour : neighbour) {
            if (board[aNeighbour.x][aNeighbour.y] == opponent) {
                places[opponent]--;
                board[aNeighbour.x][aNeighbour.y] = move.player;
                places[move.player]++;
            }
        }
        return new Board(board);
    }

    public boolean isTerminated(final int player, final int level, final int currentDepth) {
        return options[player] == 0 || level + currentDepth >= MinMax.TERMINAL_DEPTH;
    }

    int heuristicValue(final int player) {
        final int opponent = MinMax.flip(player);
        return (int) Math.round(100 * (3 * (stable[player] - stable[opponent])
                + 0.6 * (places[player] - places[opponent])
                + 0.4 * (edges[player] - edges[opponent])
                + 0.1 * (options[player] - options[opponent])));
    }

    public static void setThoseWithinSight() {
        final Cell temps[] = new Cell[6];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                int count = 0;
                if (i > 0) {
                    temps[count] = CELLS[i - 1][j];
                    count++;
                    if (j % 2 == 0) {
                        if (j > 0) {
                            temps[count] = CELLS[i - 1][j - 1];
                            count++;
                        }
                        if (j < COLS - 1) {
                            temps[count] = CELLS[i - 1][j + 1];
                            count++;
                        }
                    }
                }
                if (i < ROWS - 1) {
                    temps[count] = CELLS[i + 1][j];
                    count++;
                    if (j % 2 == 1) {
                        if (j > 0) {
                            temps[count] = CELLS[i + 1][j - 1];
                            count++;
                        }
                        if (j < COLS - 1) {
                            temps[count] = CELLS[i + 1][j + 1];
                            count++;
                        }
                    }
                }
                if (j > 0) {
                    temps[count] = CELLS[i][j - 1];
                    count++;
                }
                if (j < COLS - 1) {
                    temps[count] = CELLS[i][j + 1];
                    count++;
                }
                neighbours[i][j] = new Cell[count];
                System.arraycopy(temps, 0, neighbours[i][j], 0, count);
                System.arraycopy(temps, 0, neighbours[i][j], 0, count);
            }
        }

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final Set<Cell> tooClose = new HashSet<>();
                tooClose.add(new Cell(i, j));
                final Cell[] neighbour = neighbours[i][j];
                Collections.addAll(tooClose, neighbour);
                final Set<Cell> distantNeighbours = new HashSet<>();
                for (final Cell cell : neighbour) {
                    for (final Cell current : neighbours[cell.x][cell.y]) {
                        if (!tooClose.contains(current)) {
                            distantNeighbours.add(current);
                        }
                    }
                }
                jumpables[i][j] = new Cell[distantNeighbours.size()];
                final List<Cell> distantNeighboursList = distantNeighbours.stream().collect(Collectors.toList());
                for (int k = 0; k < distantNeighboursList.size(); k++) {
                    jumpables[i][j][k] = distantNeighboursList.get(k);
                }
            }
        }
    }

    public static void setCells() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                CELLS[i][j] = new Cell(i, j);
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
        return new Board(board, places, options, moves, hashCode, stable, edges);
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.deepToString(board) +
                ", hashCode=" + Arrays.toString(hashCode) +
                '}';
    }
}