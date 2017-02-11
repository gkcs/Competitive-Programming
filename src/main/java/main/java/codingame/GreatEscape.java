package main.java.codingame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

class GreatEscape {

    public static void main(String args[]) throws IOException {
        final InputReader br = new InputReader(System.in);
        final int width = br.readInt();
        final int height = br.readInt();
        final int playerCount = br.readInt();
        final int id = br.readInt();
        while (true) {
            for (int i = 0; i < playerCount; i++) {
                final int x = br.readInt();
                final int y = br.readInt();
                final int wallsLeft = br.readInt();
            }
            int wallCount = br.readInt();
            for (int i = 0; i < wallCount; i++) {
                final int wallX = br.readInt();
                final int wallY = br.readInt();
                final String wallOrientation = br.readString();
            }
            // action: LEFT, RIGHT, UP, DOWN or "putX putY putOrientation" to place a wall
            System.out.println("RIGHT");
        }
    }
}


class MinMax {
    public static int MAX_DEPTH = 60;
    public final int TIME_OUT;
    private int computations = 0, depth = 3, moves = 0;
    public long eval;
    public static final int MAX_VALUE = 1000000;
    public static final int MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private MinMax.Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private final boolean nullSearchActivated = true;
    private boolean timeOut;
    private final Map<Board.BoardSituation, MinMax.Configuration[]> configurationMap;
    private int configHit;
    private int configInsert;

    public MinMax(final int timeOut) {
        TIME_OUT = timeOut;
        Board.setUp();
        configurationMap = new HashMap<>();
        eval = 0;
    }

    public Move iterativeSearchForBestMove(final int player, final Board board) {
        if (board.options == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new MinMax.Configuration[board.options];
        for (int i = 0; i < startConfigs.length; i++) {
            startConfigs[i] = new MinMax.Configuration(board.moves[player][i], board, 0, false);
        }
        Arrays.sort(startConfigs);
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.options;
        return bestMove;
    }

    private Move findBestMove(final int player) {
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        try {
            for (final MinMax.Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(possibleConfig.board,
                                               flip(player),
                                               0,
                                               toTake,
                                               toGive,
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
                    if (max == MAX_VALUE) {
                        break;
                    }
                }
                if (toTake >= toGive) {
                    if (possibleConfig.killer) {
                        if (killerMoves[0][0] == possibleConfig.move) {
                            efficiency[0][0]++;
                        } else {
                            efficiency[0][1]++;
                            if (efficiency[0][0] < efficiency[0][1]) {
                                final Move temp = killerMoves[0][0];
                                killerMoves[0][0] = killerMoves[0][1];
                                killerMoves[0][1] = temp;
                            }
                        }
                    } else {
                        if (killerMoves[0][0] == null) {
                            killerMoves[0][0] = possibleConfig.move;
                            efficiency[0][0] = 1;
                        } else if (killerMoves[0][1] == null) {
                            killerMoves[0][1] = possibleConfig.move;
                            efficiency[0][1] = 1;
                        }
                    }
                    break;
                } else if (possibleConfig.killer) {
                    if (killerMoves[0][0] == possibleConfig.move) {
                        efficiency[0][0]--;
                    } else {
                        efficiency[0][1]--;
                    }
                    if (efficiency[0][0] < efficiency[0][1]) {
                        final Move temp = killerMoves[0][0];
                        killerMoves[0][0] = killerMoves[0][1];
                        killerMoves[0][1] = temp;
                    }
                    if (efficiency[0][1] <= 0) {
                        efficiency[0][1] = 0;
                        killerMoves[0][1] = null;
                    }
                }
            }
        } catch (TimeoutException ignored) {
        }
        Arrays.sort(startConfigs);
        return bestMove;
    }

    private int evaluate(final Board board,
                         final int player,
                         final int level,
                         final long a,
                         final long b,
                         final boolean isNullSearch) throws TimeoutException {
        long toTake = a, toGive = b;
        int max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new TimeoutException();
        }
        if (board.isTerminated()) {
            max = board.isTerminated == player ? MAX_VALUE : board.isTerminated == 0 ? 0 : MIN_VALUE;
        } else if (level >= depth) {
            max = board.evaluatePosition(player);
        } else {
            final Board.BoardSituation boardSituation = new Board.BoardSituation(board, player);
            final MinMax.Configuration[] configurations;
            if (level < 8 && configurationMap.containsKey(boardSituation)) {
                configurations = configurationMap.get(boardSituation);
                configHit++;
            } else {
                configurations = new MinMax.Configuration[board.options];
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i] = new MinMax.Configuration(board.moves[player][i],
                                                                 board,
                                                                 level,
                                                                 isNullSearch);
                }
                if (level < 8) {
                    configInsert++;
                    configurationMap.put(boardSituation, configurations);
                }
            }
            Arrays.sort(configurations);
            for (final MinMax.Configuration possibleConfig : configurations) {
                computations++;
                if (nullSearchActivated && !isNullSearch && board.empty > 30 && level + 2 < depth) {
                    final int nullMoveValue = -evaluate(possibleConfig.board,
                                                        player,
                                                        level + 2,
                                                        player == 1 ? toTake : toGive - 1,
                                                        player == 1 ? toTake + 1 : toGive,
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
                                               isNullSearch);
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
                    if (max == MAX_VALUE) {
                        break;
                    }
                }
                if (toTake >= toGive) {
                    max = moveValue;
                    if (!isNullSearch) {
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
                    }
                    break;
                } else if (possibleConfig.killer && !isNullSearch) {
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
        System.out.println(eval + " " + depth + " " + moves + " " + computations + " " + configHit + " " + configInsert);
    }

    public class Configuration implements Comparable<MinMax.Configuration> {
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
                if (board.isASave(move)) {
                    strength += 1000;
                }
                strength -= Math.abs(Board.COLS / 2.0 - move.cell.y);
                killer = false;
            }
            this.move = move;
        }

        @Override
        public int compareTo(MinMax.Configuration o) {
            if (strength + o.strength >= 1000) {
                return o.strength - strength;
            } else if (!killer && o.killer) {
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
    final Board.Cell cell;
    final byte player;

    public Move(final Board.Cell cell, final byte player) {
        this.cell = cell;
        this.player = player;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Move move = (Move) o;
        return cell.equals(move.cell) && player == move.player;
    }

    @Override
    public int hashCode() {
        return 31 * cell.hashCode() + player;
    }

    String describe() {
        return cell.describe();
    }

    @Override
    public String toString() {
        return "Move{" +
                "cell=" + cell +
                ", player=" + player +
                '}';
    }
}

class Board {
    private static final int ROWS = 7;
    public static final int COLS = 8;
    private static final int PLAYERS = 3;
    final byte[][] board;
    private final byte threes[];
    int options;
    final Move moves[][];
    public static final Move MOVES[][][] = new Move[ROWS][COLS][PLAYERS];
    public long[] hashCode;
    int isTerminated;
    int empty;

    public static void setUp() {
        Board.setMoves();
    }

    Board(final byte[][] board) {
        this.board = board;
        moves = new Move[PLAYERS][COLS];
        options = 0;
        threes = new byte[PLAYERS];
        for (int i = 0; i < COLS; i++) {
            if (board[ROWS - 1][i] == 0) {
                moves[1][options] = MOVES[ROWS - 1][i][1];
                moves[2][options] = MOVES[ROWS - 1][i][2];
                options++;
                empty += ROWS;
            } else {
                for (int j = 0; j < ROWS; j++) {
                    final byte player = board[j][i];
                    if (player != 0) {
                        empty += j;
                        if (j != 0) {
                            moves[1][options] = MOVES[j - 1][i][1];
                            moves[2][options] = MOVES[j - 1][i][2];
                            options++;
                        }
                        final int[] streaks = findStreaks(player, j, i);
                        if (streaks[4] > 0) {
                            isTerminated = player;
                        } else {
                            threes[player] += streaks[3];
                        }
                        break;
                    }
                }
            }
        }
        this.hashCode = getHashCode();
    }

    Board(final Board input, final Move move) {
        this.board = new byte[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(input.board[i], 0, this.board[i], 0, COLS);
        }
        this.board[move.cell.x][move.cell.y] = move.player;
        options = input.options;
        moves = new Move[PLAYERS][options];
        empty = input.empty - 1;
        threes = new byte[PLAYERS];
        hashCode = new long[2];
        System.arraycopy(input.threes, 0, this.threes, 0, threes.length);
        System.arraycopy(input.hashCode, 0, this.hashCode, 0, hashCode.length);
        int index = 0;
        for (; index < options && !move.equals(input.moves[move.player][index]); index++) {
            moves[1][index] = input.moves[1][index];
            moves[2][index] = input.moves[2][index];
        }
        if (move.cell.x == 0) {
            moves[1][index] = input.moves[1][options - 1];
            moves[2][index] = input.moves[2][options - 1];
            options--;
        } else {
            moves[1][index] = MOVES[move.cell.x - 1][move.cell.y][1];
            moves[2][index] = MOVES[move.cell.x - 1][move.cell.y][2];
        }
        index++;
        for (; index < options; index++) {
            moves[1][index] = input.moves[1][index];
            moves[2][index] = input.moves[2][index];
        }
        final int opponent = MinMax.flip(move.player);
        final int[] initial = findStreaks(opponent, move.cell.x, move.cell.y);
        threes[opponent] -= initial[4];
        final int[] later = findStreaks(move.player, move.cell.x, move.cell.y);
        if (later[4] > 0) {
            isTerminated = move.player;
        }
        threes[move.player] += later[3];
        final int digit = move.cell.x * COLS + move.cell.y;
        //hashCode[digit >> 5] &= (63 ^ (3L << ((digit << 1) & 63)));
        final long b = board[move.cell.x][move.cell.y];
        hashCode[digit >> 5] |= b << ((digit << 1) & 63);
    }

    private int[] findStreaks(final int player, final int x, final int y) {
        final int[] result = new int[5];
        int vertical = 1;
        for (int i = x + 1; i < ROWS && vertical < 4; i++) {
            if (board[i][y] == player) {
                vertical++;
            } else {
                break;
            }
        }
        result[vertical]++;
        int horizontal = 1;
        boolean open = false;
        for (int i = y - 1; i >= 0 && horizontal < 4; i--) {
            if (board[x][i] == player) {
                horizontal++;
            } else {
                if (board[x][i] == 0) {
                    open = true;
                }
                break;
            }
        }
        for (int i = y + 1; i < COLS && horizontal < 4; i++) {
            if (board[x][i] == player) {
                horizontal++;
            } else {
                if (board[x][i] == 0) {
                    open = true;
                }
                break;
            }
        }
        if (horizontal != 3 || open) {
            result[horizontal]++;
        }
        int diagonal = 1;
        open = false;
        for (int i = x - 1, j = y - 1; i >= 0 && j >= 0 && diagonal < 4; i--, j--) {
            if (board[i][j] == player) {
                diagonal++;
            } else {
                if (board[i][j] == 0) {
                    open = true;
                }
                break;
            }
        }
        for (int i = x + 1, j = y + 1; i < ROWS && j < COLS && diagonal < 4; i++, j++) {
            if (board[i][j] == player) {
                diagonal++;
            } else {
                if (board[i][j] == 0) {
                    open = true;
                }
                break;
            }
        }
        if (diagonal != 3 || open) {
            result[diagonal]++;
        }
        int reverse = 1;
        open = false;
        for (int i = x - 1, j = y + 1; i >= 0 && j < COLS && reverse < 4; i--, j++) {
            if (board[i][j] == player) {
                reverse++;
            } else {
                if (board[i][j] == 0) {
                    open = true;
                }
                break;
            }
        }
        for (int i = x + 1, j = y - 1; i < ROWS && j >= 0 && reverse < 4; i++, j--) {
            if (board[i][j] == player) {
                reverse++;
            } else {
                if (board[i][j] == 0) {
                    open = true;
                }
                break;
            }
        }
        if (reverse != 3 || open) {
            result[reverse]++;
        }
        return result;
    }

    private long[] getHashCode() {
        final long hashCode[] = new long[2];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int digit = i * COLS + j;
                final long b = board[i][j];
                hashCode[digit >> 5] |= b << ((digit << 1) & 63);
            }
        }
        return hashCode;
    }

    public Board play(final Move move) {
        return new Board(this, move);
    }

    public boolean isTerminated() {
        return isTerminated != 0 || empty == 0;
    }

    public int heuristicValue(final int player) {
        return isTerminated()
                ? isTerminated == player
                ? MinMax.MAX_VALUE
                : isTerminated == 0
                ? 0
                : MinMax.MIN_VALUE
                : evaluatePosition(player) + 10 * (threes[player] - threes[MinMax.flip(player)]);
    }

    public int evaluatePosition(final int player) {
        final int opponent = MinMax.flip(player);
        int immediateThreats = 0;
        for (int i = 0; i < options; i++) {
            final Move move = moves[player][i];
            if (findStreaks(player, move.cell.x, move.cell.y)[4] > 0) {
                return MinMax.MAX_VALUE;
            } else if (findStreaks(opponent, move.cell.x, move.cell.y)[4] > 0) {
                if (immediateThreats > 0) {
                    return MinMax.MIN_VALUE;
                } else {
                    immediateThreats++;
                }
            }
        }
        int threats = 0;
        for (int i = 0; i < options; i++) {
            final Move move = moves[player][i];
            if (move != null) {
                for (int row = move.cell.x - 1; row >= 0; row--) {
                    final int streaks[] = findStreaks(player, row, move.cell.y);
                    final int other[] = findStreaks(opponent, row, move.cell.y);
                    if (streaks[4] > 0 && other[4] == 0) {
                        threats++;
                        break;
                    } else if (streaks[4] == 0 && other[4] > 0) {
                        threats--;
                        break;
                    }
                }
            }
        }
        return 1000 * threats;
    }

    private static void setMoves() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                for (byte k = 0; k < PLAYERS; k++) {
                    MOVES[i][j][k] = new Move(new Board.Cell(i, j), k);
                }
            }
        }
    }

    public boolean isASave(final Move move) {
        return findStreaks(MinMax.flip(move.player), move.cell.x, move.cell.y)[4] > 0;
    }

    public static class Cell {
        final int x, y;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Board.Cell cell = (Board.Cell) o;
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
            return "place_disc " + y;
        }

        @Override
        public String toString() {
            return x + " " + y;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Board board = (Board) o;
        return hashCode[0] == board.hashCode[0] && hashCode[1] == board.hashCode[1];
    }

    @Override
    public int hashCode() {
        return Long.hashCode(31 * hashCode[0] + hashCode[1]);
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.deepToString(board) +
                ", threes=" + Arrays.toString(threes) +
                ", options=" + options +
                ", moves=" + Arrays.deepToString(moves) +
                ", hashCode=" + Arrays.toString(hashCode) +
                ", isTerminated=" + isTerminated +
                ", empty=" + empty +
                '}';
    }

    public static class BoardSituation {
        private final long[] board;
        private final int player;

        public BoardSituation(final Board board, final int player) {
            this.board = board.hashCode;
            this.player = player;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Board.BoardSituation that = (Board.BoardSituation) o;
            return player == that.player && board[0] == that.board[0] && board[1] == that.board[1];
        }

        @Override
        public int hashCode() {
            return Long.hashCode(961 * board[0] + 31 * board[1] + player);
        }
    }
}

class InputReader {
    private InputStream stream;
    private byte[] buf = new byte[1024];

    private int curChar;

    private int numChars;

    public InputReader(InputStream stream) {
        this.stream = stream;
    }

    public int read() {
        if (numChars == -1)
            throw new RuntimeException();
        if (curChar >= numChars) {
            curChar = 0;
            try {
                numChars = stream.read(buf);
            } catch (IOException e) {
                throw new RuntimeException();
            }
            if (numChars <= 0)
                return -1;
        }
        return buf[curChar++];
    }

    public String readString() {
        final StringBuilder stringBuilder = new StringBuilder();
        int c = read();
        while (isSpaceChar(c))
            c = read();
        do {
            stringBuilder.append(c);
            c = read();
        } while (!isSpaceChar(c));
        return stringBuilder.toString();
    }

    public int readInt() {
        int c = read();
        while (isSpaceChar(c))
            c = read();
        int sgn = 1;
        if (c == '-') {
            sgn = -1;
            c = read();
        }
        int res = 0;
        do {
            res *= 10;
            res += c - '0';
            c = read();
        } while (!isSpaceChar(c));
        return res * sgn;
    }

    public long readLong() {
        int c = read();
        while (isSpaceChar(c))
            c = read();
        int sgn = 1;
        if (c == '-') {
            sgn = -1;
            c = read();
        }
        long res = 0;
        do {
            res *= 10;
            res += c - '0';
            c = read();
        } while (!isSpaceChar(c));
        return res * sgn;
    }

    public boolean isSpaceChar(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
    }
}