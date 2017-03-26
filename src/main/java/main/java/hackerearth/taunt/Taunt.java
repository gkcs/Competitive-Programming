package main.java.hackerearth.taunt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class Taunt {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final byte[][] board = new byte[Board.ROWS][Board.COLS];
        for (int i = 0; i < board.length; i++) {
            final String cols[] = bufferedReader.readLine().split(" ");
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] |= (cols[j].charAt(2) - '0');
                board[i][j] |= ((cols[j].charAt(1) - '0') & 3) << 1;
                board[i][j] |= ((cols[j].charAt(0) - '0') & 3) << 3;
            }
        }
        final int movesPlayed = Integer.parseInt(bufferedReader.readLine());
        final int player = Integer.parseInt(bufferedReader.readLine());
        final MinMax minMax = new MinMax(400, movesPlayed);
        final Board gameBoard = new Board(board);
        final Move col = minMax.iterativeSearchForBestMove(player, gameBoard);
        System.out.println(col.describe());
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
        printStats();
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.options[player];
        printStats();
        return bestMove;
    }

    public void printStats() {
        System.out.println(Arrays.stream(startConfigs)
                                   .map(configuration -> configuration.move)
                                   .map(move -> "START " + move.start.toString() + " END " + move.end.toString())
                                   .collect(Collectors.joining("\n")));
        System.out.println(Arrays.stream(startConfigs)
                                   .map(configuration -> Arrays.toString(configuration.board.pieceCount))
                                   .collect(Collectors.toList()));
        System.out.println(Arrays.stream(startConfigs)
                                   .map(configuration -> configuration.strength)
                                   .collect(Collectors.toList()));
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
        //board.undo(move);
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
                killer = false;
            }
            //board.undo(move);
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

enum Coin {
    PAWN(0, new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}}, 1),
    ROOK(0, new int[][]{{1, 0}, {-1, 0}, {0, 2}}, 2),
    BISHOP(0, new int[][]{{-2, 2}, {2, 2}}, 3);

    final int value;
    final int[][] movesTo;
    final int index;

    Coin(final int value, final int[][] movesTo, final int index) {
        this.value = value;
        this.movesTo = movesTo;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Coin{" + name() + '}';
    }
}

class Piece {
    final Board.Cell position;
    final Coin coin;
    final int player;
    boolean movingUp;

    public Piece(final Board.Cell position, final Coin coin, final int player, final boolean movingUp) {
        this.position = position;
        this.coin = coin;
        this.player = player;
        this.movingUp = movingUp;
    }

    public Piece(final Piece piece) {
        position = piece.position;
        coin = piece.coin;
        player = piece.player;
        movingUp = piece.movingUp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Piece piece = (Piece) o;
        return player == piece.player && movingUp == piece.movingUp && coin == piece.coin && position.equals(piece.position);
    }

    @Override
    public int hashCode() {
        int result = coin.hashCode();
        result = 31 * result + player;
        result = 31 * result + (movingUp ? 1 : 0);
        result = 31 * result + position.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "position=" + position +
                ", coin=" + coin +
                ", player=" + player +
                ", movingUp=" + movingUp +
                '}';
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

    public Move(final Move move) {
        start = move.start;
        end = move.end;
        piece = new Piece(move.piece);
        capturedPieces = new ArrayList<>();
        for (final Piece material : move.capturedPieces) {
            capturedPieces.add(new Piece(material));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Move move = (Move) o;
        return start.equals(move.start) && end.equals(move.end);
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
                ", capturedPieces=" + capturedPieces +
                '}';
    }

    public boolean isACapture() {
        //TODO: Add quiet search
        return capturedPieces.isEmpty();
    }

    public String describe() {
        return start.describe() + "\n" + end.describe();
    }
}

class Board {
    public static final int ROWS = 10;
    public static final int COLS = 4;
    private static final int PLAYERS = 3;
    public static final int HASH_CODE_SIZE = 3;
    public static final int MOD = 63;
    public static final int MOVES_POSSIBLE = 27;
    final int[] pieceCount;
    public final long[] hashCode;
    final Piece[][] board;
    final Move moves[][];
    final int options[];

    public Board(final byte[][] board) {
        this.pieceCount = new int[PLAYERS];
        this.moves = new Move[PLAYERS][MOVES_POSSIBLE];
        this.options = new int[PLAYERS];
        this.board = convertToBoard(board);
        this.hashCode = getHashCode(board);
    }

    public Board(final Board game) {
        this.board = new Piece[ROWS][COLS];
        this.moves = new Move[PLAYERS][MOVES_POSSIBLE];
        this.options = new int[PLAYERS];
        this.pieceCount = new int[PLAYERS];
        this.hashCode = new long[HASH_CODE_SIZE];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = game.board[i][j] == null ? null : new Piece(game.board[i][j]);
            }
        }
        for (int i = 0; i < PLAYERS; i++) {
            for (int j = 0; j < game.options[i]; j++) {
                moves[i][j] = new Move(game.moves[i][j]);
            }
        }
        System.arraycopy(game.options, 0, options, 0, PLAYERS);
        System.arraycopy(game.pieceCount, 0, pieceCount, 0, PLAYERS);
        System.arraycopy(game.hashCode, 0, hashCode, 0, HASH_CODE_SIZE);
    }

    public Board play(final Move move) {
        final Board copy = new Board(this);
        copy.makeMove(move);
        return copy;
    }

    public void makeMove(final Move move) {
        final int[][] freeSpace = new int[PLAYERS][20];
        final int[] gaps = new int[PLAYERS];
        final int player = move.piece.player;
        final int opponent = MinMax.flip(player);
        for (int i = 0; i < options[opponent]; i++) {
            if (moves[opponent][i].capturedPieces.contains(move.piece)) {
                moves[opponent][i].capturedPieces.remove(move.piece);
            }
        }
        for (int i = 0; i < options[player]; i++) {
            if (moves[player][i].end.equals(move.end) || moves[player][i].start.equals(move.start)) {
                moves[player][i] = null;
                freeSpace[player][gaps[player]++] = i;
            }
        }
        pieceCount[opponent] -= move.capturedPieces.size();
        for (final Piece capture : move.capturedPieces) {
            for (int i = 0; i < options[opponent]; i++) {
                if (moves[opponent][i] != null && moves[opponent][i].start.equals(capture.position)) {
                    moves[opponent][i] = null;
                    freeSpace[opponent][gaps[opponent]++] = i;
                }
            }
            for (int i = 0; i < options[player]; i++) {
                if (moves[player][i] != null && moves[player][i].capturedPieces.contains(capture)) {
                    moves[player][i].capturedPieces.remove(capture);
                }
            }
            board[capture.position.x][capture.position.y] = null;
            final int digit = capture.position.x * COLS + capture.position.y;
            final int index = (digit << 2) & MOD;
            hashCode[digit >> 4] &= ~(((1 << (index + 4)) - 1) & (1 << index));
        }
        if (move.end.x == 0 || move.end.x == ROWS - 1
                || (move.end.x == move.start.x
                && (move.piece.coin.equals(Coin.BISHOP)
                || (move.piece.coin.equals(Coin.ROOK)
                && move.end.y == move.start.y)))) {
            move.piece.movingUp = !move.piece.movingUp;
        }
        board[move.start.x][move.start.y] = null;
        final int digit = move.start.x * COLS + move.start.y;
        final int index = (digit << 2) & MOD;
        hashCode[digit >> 4] &= ~(((1 << (index + 4)) - 1) & (1 << index));
        board[move.end.x][move.end.y] = move.piece;
        {
            final int moveBox = move.end.x * COLS + move.end.y;
            final int moveIndex = (moveBox << 2) & MOD;
            hashCode[moveBox >> 4] &= ~(((1 << (moveIndex + 4)) - 1) & (1 << moveIndex));
            long hashValue = move.piece.movingUp ? 1 : 0;
            hashValue |= move.piece.coin.index << 1;
            hashValue |= (player == 2 ? 1 : 0) << 3;
            hashCode[moveBox >> 4] |= (hashValue << moveIndex);
        }
        final int fills[] = new int[PLAYERS];
        for (int k = -2; k <= 2; k++) {
            for (int l = -2; l <= 2; l++) {
                final int i = move.end.x + k, j = move.end.y + l;
                if (i > 0 && i < ROWS && j > 0 && j < COLS && board[i][j] != null && board[i][j].player == opponent) {
                    final Cell start = new Cell(i, j);
                    for (final int[] movesTo : board[i][j].coin.movesTo) {
                        final int direction = board[i][j].movingUp ? 1 : -1;
                        if (!((j == 0 && movesTo[0] < 0)
                                || (j == COLS - 1 && movesTo[0] > 0))) {
                            int x = i + movesTo[1] * direction;
                            int y = j + movesTo[0];
                            if (x >= ROWS) {
                                x = ROWS - (x + 1 - ROWS) - 1;
                            } else if (x < 0) {
                                x = -x;
                            }
                            if (y >= COLS) {
                                y = COLS - (y + 1 - COLS) - 1;
                            } else if (y < 0) {
                                y = -y;
                            }
                            final List<Piece> captures = new ArrayList<>(2);
                            if (x == move.end.x && y == move.end.y) {
                                if (fills[opponent] < gaps[opponent]) {
                                    moves[opponent][freeSpace[opponent][fills[opponent]++]] = new Move(start,
                                                                                                       new Cell(x, y),
                                                                                                       board[i][j],
                                                                                                       captures);
                                } else {
                                    moves[opponent][options[opponent]++] = new Move(start,
                                                                                    new Cell(x, y),
                                                                                    board[i][j],
                                                                                    captures);
                                }
                                if (board[x][y] != null) {
                                    captures.add(board[x][y]);
                                }
                            } else if (board[x][y] != null && !board[x][y].coin.equals(Coin.PAWN)) {
                                if (Math.abs(movesTo[1]) == 2) {
                                    int intermediateX = x + movesTo[1] / 2 * direction;
                                    int intermediateY = y + movesTo[0] / 2;
                                    if (intermediateX >= ROWS) {
                                        intermediateX = ROWS - (intermediateX + 1 - ROWS) - 1;
                                    } else if (intermediateX < 0) {
                                        intermediateX = -intermediateX;
                                    }
                                    if (intermediateY >= COLS) {
                                        intermediateY = COLS - (intermediateY + 1 - COLS) - 1;
                                    } else if (intermediateY < 0) {
                                        intermediateY = -intermediateY;
                                    }
                                    if (intermediateX == move.end.x && intermediateY == move.end.y) {
                                        captures.add(board[intermediateX][intermediateY]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        {
            final int i = move.end.x, j = move.end.y;
            final Cell start = new Cell(i, j);
            for (final int[] movesTo : board[i][j].coin.movesTo) {
                final int direction = board[i][j].movingUp ? 1 : -1;
                if (!((j == 0 && movesTo[0] < 0)
                        || (j == COLS - 1 && movesTo[0] > 0))) {
                    int x = i + movesTo[1] * direction;
                    int y = j + movesTo[0];
                    if (x >= ROWS) {
                        x = ROWS - (x + 1 - ROWS) - 1;
                    } else if (x < 0) {
                        x = -x;
                    }
                    if (y >= COLS) {
                        y = COLS - (y + 1 - COLS) - 1;
                    } else if (y < 0) {
                        y = -y;
                    }
                    if (board[x][y] == null || board[x][y].player != player) {
                        final List<Piece> captures = new ArrayList<>(2);
                        if (fills[player] < gaps[player]) {
                            moves[player][freeSpace[player][fills[player]++]] = new Move(start, new Cell(x, y),
                                                                                         board[i][j], captures);
                        } else {
                            moves[player][options[player]++] = new Move(start, new Cell(x, y), board[i][j], captures);
                        }
                        if (board[x][y] != null) {
                            captures.add(board[x][y]);
                        }
                        if (!board[i][j].coin.equals(Coin.PAWN)) {
                            if (Math.abs(movesTo[1]) == 2) {
                                int intermediateX = i + movesTo[1] / 2 * direction;
                                int intermediateY = j + movesTo[0] / 2;
                                if (intermediateX >= ROWS) {
                                    intermediateX = ROWS - (intermediateX + 1 - ROWS) - 1;
                                } else if (intermediateX < 0) {
                                    intermediateX = -intermediateX;
                                }
                                if (intermediateY >= COLS) {
                                    intermediateY = COLS - (intermediateY + 1 - COLS) - 1;
                                } else if (intermediateY < 0) {
                                    intermediateY = -intermediateY;
                                }
                                if (board[intermediateX][intermediateY] != null
                                        && board[intermediateX][intermediateY].player == MinMax.flip(player)) {
                                    captures.add(board[intermediateX][intermediateY]);
                                }
                            }
                        }
                    }
                }
            }
        }
        //shuffle the moves to blank spots
        options[player] = shift(moves[player], freeSpace[player], gaps[player], fills[player], options[player]) + 1;
        options[opponent] = shift(moves[opponent],
                                  freeSpace[opponent],
                                  gaps[opponent],
                                  fills[opponent],
                                  options[opponent]) + 1;
    }

    private int shift(final Move[] move, final int[] freeSpace, final int end, final int start, int pointer) {
        while (pointer >= 0 && move[pointer] == null) {
            pointer--;
        }
        for (int i = start; i < end && pointer > 0; i++) {
            final Move temp = move[freeSpace[i]];
            move[freeSpace[i]] = move[pointer];
            move[pointer] = temp;
            while (pointer >= 0 && move[pointer] == null) {
                pointer--;
            }
        }
        return pointer;
    }
    //todo:implement this
//    public Board undo(final Move move) {
//        return this;
//    }

    private Piece[][] convertToBoard(final byte[][] board) {
        final Piece[][] pieces = new Piece[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final int player = (board[i][j] >> 3) & 3;
                if (player != 0) {
                    final int coinType = (board[i][j] >> 1) & 3;
                    final boolean movingUp = (board[i][j] & 1) == 1;
                    final Coin coin = coinType == 1 ? Coin.PAWN : coinType == 2 ? Coin.ROOK : Coin.BISHOP;
                    pieces[i][j] = new Piece(new Cell(i, j), coin, player, movingUp);
                    pieceCount[player]++;
                }
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (pieces[i][j] != null) {
                    final int player = pieces[i][j].player;
                    final Cell start = new Cell(i, j);
                    for (final int[] movesTo : pieces[i][j].coin.movesTo) {
                        final int direction = pieces[i][j].movingUp ? 1 : -1;
                        if (!((j == 0 && movesTo[0] < 0)
                                || (j == COLS - 1 && movesTo[0] > 0))) {
                            int x = i + movesTo[1] * direction;
                            int y = j + movesTo[0];
                            if (x >= ROWS) {
                                x = ROWS - (x + 1 - ROWS) - 1;
                            } else if (x < 0) {
                                x = -x;
                            }
                            if (y >= COLS) {
                                y = COLS - (y + 1 - COLS) - 1;
                            } else if (y < 0) {
                                y = -y;
                            }
                            if (pieces[x][y] == null || pieces[x][y].player != player) {
                                final List<Piece> captures = new ArrayList<>(2);
                                moves[player][options[player]++] = new Move(start,
                                                                            new Cell(x, y),
                                                                            pieces[i][j],
                                                                            captures);
                                if (pieces[x][y] != null) {
                                    captures.add(pieces[x][y]);
                                }
                                if (!pieces[i][j].coin.equals(Coin.PAWN)) {
                                    if (Math.abs(movesTo[1]) == 2) {
                                        int intermediateX = i + movesTo[1] / 2 * direction;
                                        int intermediateY = j + movesTo[0] / 2;
                                        if (intermediateX >= ROWS) {
                                            intermediateX = ROWS - (intermediateX + 1 - ROWS) - 1;
                                        } else if (intermediateX < 0) {
                                            intermediateX = -intermediateX;
                                        }
                                        if (intermediateY >= COLS) {
                                            intermediateY = COLS - (intermediateY + 1 - COLS) - 1;
                                        } else if (intermediateY < 0) {
                                            intermediateY = -intermediateY;
                                        }
                                        if (pieces[intermediateX][intermediateY] != null
                                                && pieces[intermediateX][intermediateY].player == MinMax.flip(player)) {
                                            captures.add(pieces[intermediateX][intermediateY]);
                                        }
                                    }
                                }
                            }
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
                hashCode[digit >> 4] |= b << ((digit << 2) & MOD);
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
            assert pieceCount[1] + pieceCount[2] > 0;
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
                ", options=" + Arrays.toString(options) +
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

        public Cell(final int x, final int y) {
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
        return this == o || !(o == null || getClass() != o.getClass()) && Arrays.deepEquals(board, ((Board) o).board);
    }

    public static class BoardSituation {
        private final Board board;
        private final int player;

        public BoardSituation(final Board board, final int player) {
            this.board = board;
//            System.arraycopy(board.hashCode, 0, this.board, 0, this.board.length);
            this.player = player;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final BoardSituation that = (BoardSituation) o;
            return player == that.player && board.equals(that.board);
        }

        @Override
        public int hashCode() {
            return board.hashCode();
        }
    }
}