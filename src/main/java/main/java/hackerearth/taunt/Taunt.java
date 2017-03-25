package main.java.hackerearth.taunt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Taunt {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final byte[][] board = new byte[Board.ROWS][Board.COLS];
        for (int i = 0; i < board.length; i++) {
            final String cols[] = bufferedReader.readLine().split(" ");
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] |= cols[j].charAt(2) - '0';
                board[i][j] |= cols[j].charAt(1) - '0' << 1;
                board[i][j] |= cols[j].charAt(0) - '0' << 3;
            }
        }
        final int movesPlayed = Integer.parseInt(bufferedReader.readLine());
        final int player = Integer.parseInt(bufferedReader.readLine());
        final MinMax minMax = new MinMax(900, movesPlayed);
        final int col = minMax.iterativeSearchForBestMove(player, new Board(board)).start.y;
        System.out.println(col);
        minMax.metrics();
    }
}

class MinMax {
    public static int MAX_DEPTH = 60;
    public final int TIME_OUT;
    private final int movesPlayed;
    private int computations = 0, depth = 3, moves = 0;
    public long eval;
    public static final int MAX_VALUE = 1000000;
    public static final int MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private final boolean nullSearchActivated = true;
    private boolean timeOut;
    private final Map<Board.BoardSituation, Configuration[]> configurationMap;
    private int configHit;
    private int configInsert;

    public MinMax(final int timeOut, final int movesPlayed) {
        TIME_OUT = timeOut;
        this.movesPlayed = movesPlayed;
        configurationMap = new HashMap<>();
        eval = 0;
    }

    public Move iterativeSearchForBestMove(final int player, final Board board) {
        if (board.options[player] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new Configuration[board.options[player]];
        for (int i = 0; i < startConfigs.length; i++) {
            startConfigs[i] = new Configuration(board.moves[player][i], board, 0, false);
        }
        Arrays.sort(startConfigs);
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.options[player];
        return bestMove;
    }

    private Move findBestMove(final int player) {
        int toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        try {
            final boolean hasSingleBranch = startConfigs.length == 1;
            for (final Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(possibleConfig.board,
                                               possibleConfig.move,
                                               flip(player),
                                               0,
                                               toTake,
                                               toGive,
                                               hasSingleBranch,
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
                         final Move move,
                         final int player,
                         final int level,
                         final int a,
                         final int b,
                         final boolean isTheOnlyBranch,
                         final boolean isNullSearch) throws TimeoutException {
        board.play(move);
        int toTake = a, toGive = b;
        int max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new TimeoutException();
        }
        final Integer terminated = board.isTerminated(player, movesPlayed + level);
        if (terminated != null) {
            max = terminated;
        } else if (level >= depth) {
            max = board.evaluatePosition(player);
        } else {
            final Board.BoardSituation boardSituation = new Board.BoardSituation(board, player);
            final Configuration[] configurations;
            if (level < 8 && configurationMap.containsKey(boardSituation)) {
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
                if (level < 8) {
                    configInsert++;
                    configurationMap.put(boardSituation, configurations);
                }
            }
            Arrays.sort(configurations);
            final boolean hasSingleBranch = configurations.length == 1;
            for (final Configuration possibleConfig : configurations) {
                computations++;
                if (nullSearchActivated && !isNullSearch && !board.isEndGame(movesPlayed + level) && level + 2 < depth) {
                    final int nullMoveValue = -evaluate(possibleConfig.board,
                                                        possibleConfig.move, player,
                                                        level + 2,
                                                        player == 1 ? toTake : toGive - 1,
                                                        player == 1 ? toTake + 1 : toGive,
                                                        hasSingleBranch,
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
                                               possibleConfig.move, flip(player),
                                               level + 1,
                                               toTake,
                                               toGive,
                                               hasSingleBranch,
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
                    max = isTheOnlyBranch ? moveValue : player == 1 ? toTake : toGive;
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
        board.undo(move);
        return -max;
    }

    public void metrics() {
        System.out.println(eval + " " + depth + " " + moves + " " + computations + " " + configHit + " " + configInsert);
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
            if (!resultsFromNullSearch && (move.equals(killerMoves[level][0]) || move.equals(killerMoves[level][1]))) {
                killer = true;
            } else {
                strength = this.board.heuristicValue(move.piece.player, movesPlayed + level);
                strength -= Math.abs(Board.COLS / 2.0 - move.start.y);
                killer = false;
            }
            board.undo(move);
            this.move = move;
        }

        @Override
        public int compareTo(Configuration o) {
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

enum Coin {
    PAWN(0, new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}}),
    ROOK(0, new int[][]{{0, 1}, {0, -1}, {2, 0}}),
    BISHOP(0, new int[][]{{-2, 2}, {2, 2}});

    final int value;
    final int[][] movesTo;

    Coin(final int value, final int[][] movesTo) {
        this.value = value;
        this.movesTo = movesTo;
    }
}

class Piece {
    final Coin coin;
    final int player;
    boolean movingUp;

    public Piece(final Coin coin, final int player, final boolean movingUp) {
        this.coin = coin;
        this.player = player;
        this.movingUp = movingUp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Piece piece = (Piece) o;
        return player == piece.player && movingUp == piece.movingUp && coin == piece.coin;
    }

    @Override
    public int hashCode() {
        int result = coin.hashCode();
        result = 31 * result + player;
        result = 31 * result + (movingUp ? 1 : 0);
        return result;
    }
}

class Move {
    final Board.Cell start, end;
    final Piece piece;
    final List<Piece> capturedPieces;

    public Move(final Board.Cell start, final Board.Cell end, final Piece piece, final List<Piece> capturedPieces) {
        this.start = start;
        this.end = end;
        this.piece = piece;
        this.capturedPieces = capturedPieces;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Move move = (Move) o;
        return start.equals(move.start) && end.equals(move.end) && piece.equals(move.piece);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + piece.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "start=" + start +
                ", end=" + end +
                ", piece=" + piece +
                '}';
    }

    public boolean isACapture() {
        //TODO: Add quiet search
        return capturedPieces.isEmpty();
    }

    public String describe() {
        return start.describe();
    }
}

class Board {
    public static final int ROWS = 10;
    public static final int COLS = 4;
    private static final int PLAYERS = 3;
    public static final int HASH_CODE_SIZE = 3;
    private final int pieceCount[];
    public final long[] hashCode;
    final Piece[][] board;
    final Move moves[][];
    final int options[];

    public Board(final byte[][] board) {
        this.board = convertToBoard(board);
        moves = new Move[PLAYERS][];
        options = new int[PLAYERS];
        this.hashCode = getHashCode(board);
        pieceCount = new int[PLAYERS];
    }

    public Board play(final Move move) {
        return this;
    }

    public Board undo(final Move move) {
        return this;
    }

    private Piece[][] convertToBoard(final byte[][] board) {
        final Piece[][] pieces = new Piece[0][];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int player = (board[i][j] >> 3) & 3;
                if (player != 0) {
                    final int coinType = (board[i][j] >> 1) & 3;
                    final boolean movingUp = (board[i][j] & 1) == 1;
                    final Coin coin = coinType == 1 ? Coin.PAWN : coinType == 2 ? Coin.ROOK : Coin.BISHOP;
                    pieces[i][j] = new Piece(coin, player, movingUp);
                    pieceCount[player]++;
                    for (final int[] movesTo : coin.movesTo) {
                        final int x = i + movesTo[0];
                        final int y = j + movesTo[1];
                        if (x >= 0 && x < ROWS && y >= 0 && y < COLS) {
                            if (((board[x][y] >> 3) & 3) != player) {
                                //add possible position to move to
                                if (((board[x][y] >> 3) & 3) == MinMax.flip(player)) {
                                    //add capture piece
                                }
                            }
                        } else {
                            //rebound
                        }
                    }
                }
            }
        }
        return pieces;
    }

    private long[] getHashCode(final byte[][] board) {
        final long hashCode[] = new long[HASH_CODE_SIZE];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int digit = i * COLS + j;
                final long b = board[i][j];
                hashCode[digit >> 4] |= b << ((digit << 2) & 63);
            }
        }
        return hashCode;
    }

    public int heuristicValue(final int player, final int movesPlayed) {
        final Integer terminated = isTerminated(player, movesPlayed);
        return terminated != null ? terminated : evaluatePosition(player);
    }

    public int evaluatePosition(final int player) {
        return pieceCount[player] - pieceCount[MinMax.flip(player)];
    }

    public Integer isTerminated(final int player, final int moveNumber) {
        final boolean hasEnded = moveNumber >= 100 || pieceCount[player] == 0 || pieceCount[MinMax.flip(player)] == 0;
        if (hasEnded) {
            assert pieceCount[player] + pieceCount[MinMax.flip(player)] != 0;
            return pieceCount[player] > pieceCount[MinMax.flip(player)] ? MinMax.MAX_VALUE : MinMax.MIN_VALUE;
        } else {
            return null;
        }
    }

    public boolean isEndGame(final int moveNumber) {
        return moveNumber > 50 || pieceCount[1] + pieceCount[2] < 8;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hashCode);
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + Arrays.deepToString(board) +
                ", options=" + options +
                ", moves=" + Arrays.deepToString(moves) +
                ", hashCode=" + Arrays.toString(hashCode) +
                '}';
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
            return x + " " + y;
        }
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && Arrays.equals(hashCode, ((Board) o).hashCode);
    }

    public static class BoardSituation {
        private final long[] board;
        private final int player;

        public BoardSituation(final Board board, final int player) {
            this.board = new long[board.hashCode.length];
            System.arraycopy(board.hashCode, 0, this.board, 0, this.board.length);
            this.player = player;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final BoardSituation that = (BoardSituation) o;
            return player == that.player && Arrays.equals(board, that.board);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(board);
        }
    }
}