package main.java.hackerearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Hexagon {
    public static void main(String[] args) throws IOException {
        final long startTime = System.nanoTime();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final byte[][] board = new byte[6][7];
        for (int i = 0; i < board.length; i++) {
            final String cols[] = bufferedReader.readLine().split(" ");
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = (byte) (cols[j].charAt(0) - '0');
            }
        }
        final MinMax minMax = new MinMax(Integer.parseInt(bufferedReader.readLine()));
        final int mb = 1048576;
        System.out.println(minMax.iterativeSearchForBestMove(
                Integer.parseInt(bufferedReader.readLine()), new Board(board)).describe());
        System.gc();
        minMax.metrics();
        System.out.println((System.nanoTime() - startTime) / 1000000000d + " " + Runtime.getRuntime().maxMemory() / mb
                                   + " " + Runtime.getRuntime().freeMemory() / mb + " " + Runtime.getRuntime().totalMemory() / mb);
    }
}

class MinMax {
    public static int MAX_DEPTH = 60;
    public static final int TERMINAL_DEPTH = 100;
    public static int TIME_OUT = (int) (Math.pow(10, 8) * 9);
    private int computations = 0, depth = 3, moves = 0;
    public long eval;
    public static final int MAX_VALUE = 1000000;
    private static final int MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.nanoTime();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private final boolean nullSearchActivated = true;
    private int currentDepth;
    private int cacheHits;
    private boolean timeOut;
    private final Map<Board.BoardSituation, Configuration[]> configurationMap;
    private int configHit;
    private int configInsert;
    private PositionCache positionCache;

    MinMax(final int currentDepth) {
        Board.setUp();
        this.currentDepth = currentDepth;
        configurationMap = new HashMap<>();
        eval = 0;
        positionCache = new PositionCache();
    }

    public Move iterativeSearchForBestMove(final int player, final Board board) {
        if (board.options[player] == 0) {
            throw new RuntimeException("No possible moves");
        }
        if (positionCache.containsPosition(new Board.BoardSituation(board, player))) {
            return positionCache.get(new Board.BoardSituation(board, player));
        }
        positionCache = null;
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
        return bestMove;
    }

    private Move findBestMove(final int player, final int level) {
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        final Map<Board, Integer> boards = new HashMap<>();
        try {
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
                    if (max == MAX_VALUE) {
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
        } catch (Exception ignored) {
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
                         final boolean isNullSearch) {
        long toTake = a, toGive = b;
        int max = MIN_VALUE;
        if (!test && System.nanoTime() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new RuntimeException();
        }
        if (board.isTerminated(player, level, currentDepth)) {
            max = (board.places[player] > board.places[MinMax.flip(player)]) ? MAX_VALUE
                    : (board.places[player] < board.places[MinMax.flip(player)])
                    ? MIN_VALUE : 0;
        } else if (level >= depth) {
            max = heuristicValue;
        } else {
            final Board.BoardSituation boardSituation = new Board.BoardSituation(board, player);
            final Configuration[] configurations;
            if (level < 2 && configurationMap.containsKey(boardSituation)) {
                configurations = configurationMap.get(boardSituation);
                configHit++;
            } else {
                configurations = new Configuration[board.options[player]];
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i] = new Configuration(board.moves[player][i],
                                                          board,
                                                          level,
                                                          isNullSearch);
                }
                Arrays.sort(configurations);
                if (level < 2) {
                    configInsert++;
                    configurationMap.put(boardSituation, configurations);
                }
            }
            final Map<Board, Integer> boards = new HashMap<>();
            for (final Configuration possibleConfig : configurations) {
                computations++;
                if (nullSearchActivated && !isNullSearch && !isEndGame(possibleConfig.board.stable,
                                                                       currentDepth + level) && level + 2 < depth) {
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
                    if (max == MAX_VALUE) {
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

    public void metrics() {
        System.out.println(eval + " " + depth + " " + moves + " " + computations + " " + cacheHits + " " + configHit + " " + configInsert);
    }

    public boolean isEndGame(final byte[] stableSquares, final int depth) {
        return stableSquares[1] + stableSquares[2] > 25 || depth > 45;
    }

    public class Configuration implements Comparable<Configuration> {
        final Move move;
        final Board board;
        int strength;
        final boolean killer;

        private Configuration(final Move move,
                              final Board board,
                              final int level,
                              final boolean resultsFromNullSearch) {
            this.board = board.play(move);
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
    final Board.Cell start, end;
    final byte player;
    final boolean isAJump;

    public Move(final Board.Cell start, final Board.Cell end, final byte player, final boolean isAJump) {
        this.start = start;
        this.end = end;
        this.player = player;
        this.isAJump = isAJump;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Move move = (Move) o;
        return start.equals(move.start) && end.equals(move.end) && player == move.player;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * start.hashCode() + end.hashCode()) + player;
    }

    String describe() {
        return start.describe() + "\n" + end.describe();
    }

    @Override
    public String toString() {
        return "Move{" +
                "start=" + start +
                ", end=" + end +
                ", player=" + player +
                ", isAJump=" + isAJump +
                '}';
    }
}

class Board {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int PLAYERS = 3;
    final byte[][] board;
    final byte places[];
    final int options[];
    byte stable[];
    byte edges[];
    Move moves[][];
    private static final Cell neighbours[][][] = new Cell[ROWS][COLS][];
    private static final Cell jumpables[][][] = new Cell[ROWS][COLS][];
    public static final Cell CELLS[][] = new Cell[ROWS][COLS];
    public int[] hashCode;
    private static int[] weights;

    public static void setUp() {
        Board.setCells();
        Board.setThoseWithinSight();
        final int[] weights = new int[4];
        weights[0] = 300;
        weights[1] = 60;
        weights[2] = 20;
        weights[3] = 10;
        Board.setWeights(weights);
    }

    Board(final byte[][] board) {
        this.board = board;
        places = new byte[PLAYERS];
        moves = new Move[PLAYERS][];
        moves[1] = new Move[300];
        moves[2] = new Move[300];
        options = new int[PLAYERS];
        stable = new byte[PLAYERS];
        edges = new byte[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final byte player = board[i][j];
                if (player != 0) {
                    places[player]++;
                    final Cell[] neighbour = neighbours[i][j];
                    edges[player] += 6 - neighbour.length;
                    boolean isStable = true;
                    for (final Cell aNeighbour : neighbour) {
                        if (board[aNeighbour.x][aNeighbour.y] == 0) {
                            moves[player][options[player]++] = new Move(CELLS[i][j],
                                                                        aNeighbour,
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
                            moves[player][options[player]++] = new Move(CELLS[i][j],
                                                                        anExtendedNeighbour,
                                                                        player,
                                                                        true);
                        }
                    }
                }
            }
        }
        this.hashCode = getHashCode();
    }

    public static void setWeights(final int[] weights) {
        Board.weights = weights;
    }

    private int[] getHashCode() {
        final int hashCode[] = new int[3];
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
        final byte[][] bytes = new byte[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(board[i], 0, bytes[i], 0, COLS);
        }
        if (move.isAJump) {
            bytes[move.start.x][move.start.y] = 0;
        }
        final int opponent = MinMax.flip(move.player);
        bytes[move.end.x][move.end.y] = move.player;
        for (final Cell aNeighbour : neighbours[move.end.x][move.end.y]) {
            if (bytes[aNeighbour.x][aNeighbour.y] == opponent) {
                bytes[aNeighbour.x][aNeighbour.y] = move.player;
            }
        }
        return new Board(bytes);
    }

    public boolean isTerminated(final int player, final int level, final int currentDepth) {
        return options[player] == 0 || level + currentDepth >= MinMax.TERMINAL_DEPTH;
    }

    public int heuristicValue(final int player) {
        final int opponent = MinMax.flip(player);
        final int params[] = new int[4];
        //todo Divide by sum
        params[0] = stable[player] - stable[opponent];
        params[1] = places[player] - places[opponent];
        params[2] = edges[player] - edges[opponent];
        params[3] = options[player] - options[opponent];
        stable = null;
        edges = null;
        return weights[0] * params[0]
                + weights[1] * params[1]
                + weights[2] * params[2]
                + weights[3] * params[3];
    }

    private static void setThoseWithinSight() {
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

    private static void setCells() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                CELLS[i][j] = new Cell(i, j);
            }
        }
    }

    public static class Cell {
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

        public String describe() {
            return x + " " + y;
        }

        @Override
        public String toString() {
            return describe();
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

    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.deepToString(board) +
                ", hashCode=" + Arrays.toString(hashCode) +
                '}';
    }

    public static class BoardSituation {
        private final int[] board;
        private final int player;

        public BoardSituation(final Board board, final int player) {
            this.board = board.hashCode;
            this.player = player;
        }

        public BoardSituation(final int[] board, final int player) {
            this.board = board;
            this.player = player;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final BoardSituation that = (BoardSituation) o;
            return player == that.player && board[0] == that.board[0] && board[1] == that.board[1] && board[2] == that.board[2];
        }

        @Override
        public int hashCode() {
            return 31 * (31 * 31 * board[0] + 31 * board[1] + board[2]) + player;
        }
    }
}

class PositionCache {
    private final Map<Board.BoardSituation, Move> cache = new HashMap<>();

    public PositionCache() {
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 1, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}
        }), 2), new Move(Board.CELLS[0][6], Board.CELLS[1][6], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 1, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1},
                {2, 0, 0, 0, 0, 0, 1}
        }), 1), new Move(Board.CELLS[0][0], Board.CELLS[1][0], (byte) 1, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}}), 1), new Move(Board.CELLS[0][0], Board.CELLS[1][0], (byte) 1, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}}), 2), new Move(Board.CELLS[5][0], Board.CELLS[4][0], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}}), 1), new Move(Board.CELLS[5][6], Board.CELLS[5][5], (byte) 1, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 1, 1}}), 2), new Move(Board.CELLS[5][0], Board.CELLS[5][1], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 1, 1}}), 1), new Move(Board.CELLS[5][6], Board.CELLS[4][6], (byte) 1, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1},
                {2, 2, 0, 0, 0, 1, 1}}), 2), new Move(Board.CELLS[4][0], Board.CELLS[4][1], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 0, 1},
                {2, 2, 0, 0, 0, 1, 1}}), 1), new Move(Board.CELLS[0][0], Board.CELLS[0][1], (byte) 1, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 1, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 0, 1},
                {2, 2, 0, 0, 0, 1, 1}}), 2), new Move(Board.CELLS[0][6], Board.CELLS[1][6], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 1, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 0, 1},
                {2, 2, 0, 0, 0, 1, 1}}), 1), new Move(Board.CELLS[5][5], Board.CELLS[5][4], (byte) 1, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 1, 0, 0, 0, 0, 2},
                {1, 0, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 2, 0, 0, 0, 0, 1},
                {2, 2, 0, 0, 1, 1, 1}}), 2), new Move(Board.CELLS[4][1], Board.CELLS[5][2], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 1, 1, 2, 0, 2, 2},
                {1, 1, 1, 2, 2, 0, 0},
                {2, 1, 1, 0, 2, 0, 0},
                {1, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 2, 0},
                {0, 0, 0, 0, 0, 0, 2}}), 2), new Move(Board.CELLS[1][3], Board.CELLS[2][3], (byte) 2, false));
        cache.put(new Board.BoardSituation(new Board(new byte[][]{
                {1, 0, 0, 0, 0, 2, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 1, 1}
        }), 2), new Move(Board.CELLS[1][3], Board.CELLS[2][3], (byte) 2, false));
    }

    public boolean containsPosition(final Board.BoardSituation boardSituation) {
        return cache.containsKey(boardSituation);
    }

    public Move get(final Board.BoardSituation boardSituation) {
        return cache.get(boardSituation);
    }
}