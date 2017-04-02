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
        final MinMax minMax = new MinMax(700, movesPlayed);
        final Board gameBoard = new Board(board);
        final Move col = minMax.iterativeSearchForBestMove(player, gameBoard);
        System.out.println(col.describe());
        minMax.metrics();
    }
}

class MinMax {
    public static final int PIECE_VALUE = 1000;
    public static int MAX_DEPTH = 60;
    public final int TIME_OUT;
    private final int movesPlayed;
    private int computations = 0, depth = 3, moves = 0;
    public long eval;
    public static final int MAX_VALUE = 1000000;
    public static final int MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test = false;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];
    private boolean timeOut;

    public MinMax(final int timeOut, final int movesPlayed) {
        TIME_OUT = timeOut;
        this.movesPlayed = movesPlayed;
        eval = 0;
    }

    public Move iterativeSearchForBestMove(final int player, final Board board) {
        if (board.options[player] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new Configuration[board.options[player]];
        for (int i = 0; i < startConfigs.length; i++) {
            startConfigs[i] = new Configuration(board.moves[player][i], board, 0);
        }
        Arrays.sort(startConfigs, getConfigurationComparator(player));
//        printStats();
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player, board);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.options[player];
//        printStats();
        return bestMove;
    }

    public void printStats() {
        System.out.println(Arrays.stream(startConfigs)
                                   .map(configuration -> configuration.move)
                                   .map(move -> "START " + move.start.toString() + " END " + move.end.toString())
                                   .collect(Collectors.joining("\n")));
        System.out.println(Arrays.stream(startConfigs)
                                   .map(configuration -> configuration.strength)
                                   .collect(Collectors.toList()));
    }

    private Move findBestMove(final int player, final Board board) {
        int toTake = startConfigs[0].strength - 2 * PIECE_VALUE, toGive = startConfigs[0].strength + 2 * PIECE_VALUE;
        int result = player == 1 ? toTake : toGive;
        Move bestMove = startConfigs[0].move;
        try {
            for (final Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(board.play(possibleConfig.move), flip(player), 0, toTake, toGive);
                possibleConfig.strength = moveValue;
                if (player == 1) {
                    if (toTake < moveValue) {
                        toTake = moveValue;
                    }
                } else if (toGive > moveValue) {
                    toGive = moveValue;
                }
                if (player == 1 && result < moveValue) {
                    result = moveValue;
                    bestMove = possibleConfig.move;
                } else if (player == 2 && result > moveValue) {
                    result = moveValue;
                    bestMove = possibleConfig.move;
                }
                if (toTake >= toGive) {
                    if (possibleConfig.killer) {
                        if (possibleConfig.move.equals(killerMoves[0][0])) {
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
                        } else if (killerMoves[0][1] == null || efficiency[0][1] < 1) {
                            killerMoves[0][1] = possibleConfig.move;
                            efficiency[0][1] = 1;
                        }
                    }
                    break;
                } else if (possibleConfig.killer) {
                    if (killerMoves[0][0].equals(possibleConfig.move)) {
                        efficiency[0][0]--;
                    } else {
                        efficiency[0][1]--;
                    }
                    if (efficiency[0][0] < efficiency[0][1] && killerMoves[0][1] != null) {
                        final Move temp = killerMoves[0][0];
                        killerMoves[0][0] = killerMoves[0][1];
                        killerMoves[0][1] = temp;
                        final int t = efficiency[0][0];
                        efficiency[0][0] = efficiency[0][1];
                        efficiency[0][1] = t;
                    }
                    if (efficiency[0][0] < 0) {
                        killerMoves[0][0] = null;
                    }
                    if (efficiency[0][1] < 0) {
                        killerMoves[0][1] = null;
                    }
                }
            }
        } catch (TimeoutException ignored) {
        }
        Arrays.sort(startConfigs, getConfigurationComparator(player));
        return bestMove;
    }

    private Comparator<Configuration> getConfigurationComparator(final int player) {
        return (first, second) -> {
            if (!first.killer && second.killer) {
                return +1;
            } else if (first.killer && !second.killer) {
                return -1;
            } else {
                return (player == 1 ? 1 : -1) * (second.strength - first.strength);
            }
        };
    }

    private int evaluate(final Board board,
                         final int player,
                         final int level,
                         final int a,
                         final int b) throws TimeoutException {
        int toTake = a, toGive = b;
        int result = player == 1 ? a : b;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new TimeoutException();
        }
        final Integer terminated = board.isTerminated(movesPlayed + level, board.pieceCount[1], board.pieceCount[2]);
        if (terminated != null) {
            result = terminated;
        } else if (level >= depth) {
            result = quietSearch(board, player, level, a, b);
        } else {
            boolean furtherProcessingRequired = true;
            if (!board.isEndGame(level) && depth > 4) {
                final int previousPlayer = MinMax.flip(player);
                final int minimumTheyWillTake = previousPlayer == 1 ? toTake : toGive - 1;
                final int nullSearchResult = nullSearch(board,
                                                        previousPlayer,
                                                        level + 3,
                                                        minimumTheyWillTake,
                                                        minimumTheyWillTake + 1);
                if (Math.abs(nullSearchResult) == MinMax.MAX_VALUE) {
                    furtherProcessingRequired = true;
                } else if (previousPlayer == 2 && nullSearchResult > minimumTheyWillTake) {
                    result = toGive;
                    furtherProcessingRequired = false;
                } else if (previousPlayer == 1 && nullSearchResult <= minimumTheyWillTake) {
                    result = toTake;
                    furtherProcessingRequired = false;
                }
            }
            if (furtherProcessingRequired) {
                final Configuration[] configurations = new Configuration[board.options[player]];
                for (int i = 0; i < configurations.length; i++) {
                    configurations[i] = new Configuration(board.moves[player][i],
                                                          board,
                                                          level);
                }
                Arrays.sort(configurations, getConfigurationComparator(player));
                for (final Configuration possibleConfig : configurations) {
                    computations++;
                    final int moveValue = evaluate(board.play(possibleConfig.move),
                                                   flip(player),
                                                   level + 1,
                                                   toTake,
                                                   toGive);
                    possibleConfig.strength = moveValue;
                    if (player == 1) {
                        if (toTake < moveValue) {
                            toTake = moveValue;
                        }
                    } else if (toGive > moveValue) {
                        toGive = moveValue;
                    }
                    if (player == 1 && result < moveValue) {
                        result = moveValue;
                    } else if (player == 2 && result > moveValue) {
                        result = moveValue;
                    }
                    if (toTake >= toGive) {
                        result = moveValue;
                        if (possibleConfig.killer) {
                            if (possibleConfig.move.equals(killerMoves[level][0])) {
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
                            } else if (killerMoves[level][1] == null || efficiency[level][1] < 1) {
                                killerMoves[level][1] = possibleConfig.move;
                                efficiency[level][1] = 1;
                            }
                        }
                        break;
                    } else if (possibleConfig.killer) {
                        if (possibleConfig.move.equals(killerMoves[level][0])) {
                            efficiency[level][0]--;
                        } else if (possibleConfig.move.equals(killerMoves[level][1])) {
                            efficiency[level][1]--;
                        }
                        if (efficiency[level][0] < efficiency[level][1] && killerMoves[level][1] != null) {
                            final Move temp = killerMoves[level][0];
                            killerMoves[level][0] = killerMoves[level][1];
                            killerMoves[level][1] = temp;
                            final int t = efficiency[level][0];
                            efficiency[level][0] = efficiency[level][1];
                            efficiency[level][1] = t;
                        }
                        if (efficiency[level][0] < 0) {
                            killerMoves[level][0] = null;
                        }
                        if (efficiency[level][1] < 0) {
                            killerMoves[level][1] = null;
                        }
                    }
                }
            }
        }
        return result;
    }

    private int nullSearch(final Board board,
                           final int player,
                           final int level,
                           final int a,
                           final int b) throws TimeoutException {
        int toTake = a, toGive = b;
        int result = player == 1 ? a : b;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new TimeoutException();
        }
        final Integer terminated = board.isTerminated(movesPlayed + level, board.pieceCount[1], board.pieceCount[2]);
        if (terminated != null) {
            result = terminated;
        } else if (level >= depth) {
            result = quietSearch(board, player, level + 1, a, b);
        } else {
            final Configuration[] configurations = new Configuration[board.options[player]];
            for (int i = 0; i < configurations.length; i++) {
                configurations[i] = new Configuration(board.moves[player][i],
                                                      board,
                                                      level);
            }
            Arrays.sort(configurations, getConfigurationComparator(player));
            for (final Configuration possibleConfig : configurations) {
                computations++;
                final int moveValue = nullSearch(board.play(possibleConfig.move),
                                                 flip(player),
                                                 level + 1,
                                                 toTake,
                                                 toGive);
                possibleConfig.strength = moveValue;
                if (player == 1) {
                    if (toTake < moveValue) {
                        toTake = moveValue;
                    }
                } else if (toGive > moveValue) {
                    toGive = moveValue;
                }
                if (player == 1 && result < moveValue) {
                    result = moveValue;
                } else if (player == 2 && result > moveValue) {
                    result = moveValue;
                }
                if (toTake >= toGive) {
                    result = moveValue;
                    break;
                }
            }
        }
        return result;
    }

    private int quietSearch(final Board board,
                            final int player,
                            final int level,
                            final int a,
                            final int b) throws TimeoutException {
        int toTake = a, toGive = b;
        int result = player == 1 ? a : b;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new TimeoutException();
        }
        final Integer terminated = board.isTerminated(movesPlayed + level, board.pieceCount[1], board.pieceCount[2]);
        if (terminated != null) {
            result = terminated;
        } else if (level >= depth + 5) {
            result = board.evaluatePosition();
        } else {
            final List<Move> captureMoves = Arrays.stream(board.moves[player])
                    .filter(Objects::nonNull)
                    .filter(Move::isACapture)
                    .collect(Collectors.toList());
            if (captureMoves.isEmpty()) {
                result = board.evaluatePosition();
            } else {
                //System.out.println("Quiet search level: " + (level - depth) + " move: " + captureMoves.size());
                final Configuration[] configurations = new Configuration[captureMoves.size()];
                for (int i = 0; i < captureMoves.size(); i++) {
                    configurations[i] = new Configuration(captureMoves.get(i),
                                                          board,
                                                          level);
                }
                Arrays.sort(configurations, getConfigurationComparator(player));
                for (final Configuration possibleConfig : configurations) {
                    computations++;
                    final int moveValue = quietSearch(board.play(possibleConfig.move),
                                                      flip(player),
                                                      level + 1,
                                                      toTake,
                                                      toGive);
                    possibleConfig.strength = moveValue;
                    if (player == 1) {
                        if (toTake < moveValue) {
                            toTake = moveValue;
                        }
                    } else if (toGive > moveValue) {
                        toGive = moveValue;
                    }
                    if (player == 1 && result < moveValue) {
                        result = moveValue;
                    } else if (player == 2 && result > moveValue) {
                        result = moveValue;
                    }
                    if (toTake >= toGive) {
                        result = moveValue;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void metrics() {
        System.out.println(eval + " " + depth + " " + moves + " " + computations);
    }

    public class Configuration {
        final Move move;
        int strength;
        final boolean killer;

        private Configuration(final Move move,
                              final Board board,
                              final int level) {
            if (move.equals(killerMoves[level][0]) || move.equals(killerMoves[level][1])) {
                killer = true;
            } else {
                strength = board.heuristicValue(movesPlayed + level,
                                                board.pieceCount[1] - (move.piece.player == 2 ? move.capturedPieces.size() : 0),
                                                board.pieceCount[2] - (move.piece.player == 1 ? move.capturedPieces.size() : 0));
                killer = false;
            }
            this.move = move;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "move=" + move +
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
    Board.Cell position;
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
        position = Board.Cell.CELLS[piece.position.x][piece.position.y];
        coin = piece.coin;
        player = piece.player;
        movingUp = piece.movingUp;
    }

    public Piece(final Piece piece, final Board.Cell position) {
        this(piece);
        this.position = position;
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

    public String toReadableString() {
        return String.valueOf(player) + String.valueOf(coin.index) + String.valueOf(movingUp ? 0 : 1);
    }
}

class Move {
    final Board.Cell start, end;
    final Piece piece;
    final List<Piece> capturedPieces;

    public Move(final Board.Cell start, final Board.Cell end, final Piece piece, final List<Piece> capturedPieces) {
        this.start = start;
        this.end = end;
        this.piece = new Piece(piece, start);
        this.capturedPieces = new ArrayList<>();
        for (final Piece material : capturedPieces) {
            this.capturedPieces.add(new Piece(material));
        }
    }

    public Move(final Move move) {
        start = move.start;
        end = move.end;
        piece = new Piece(move.piece, start);
        capturedPieces = new ArrayList<>();
        for (final Piece material : move.capturedPieces) {
            capturedPieces.add(new Piece(material));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Move move = (Move) o;
        return start.equals(move.start) && end.equals(move.end) && piece.equals(move.piece)
                && capturedPieces.equals(move.capturedPieces);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + piece.hashCode();
        result = 31 * result + capturedPieces.hashCode();
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
        return !capturedPieces.isEmpty();
    }

    public String describe() {
        return start.describe() + "\n" + end.describe();
    }
}

class Board {
    public static final int ROWS = 10;
    public static final int COLS = 4;
    private static final int PLAYERS = 3;
    public static final int MOVES_POSSIBLE = 27;
    final int[] pieceCount;
    final Piece[][] board;
    final Move moves[][];
    final int options[];

    public Board(final byte[][] board) {
        this.pieceCount = new int[PLAYERS];
        this.moves = new Move[PLAYERS][MOVES_POSSIBLE];
        this.options = new int[PLAYERS];
        this.board = convertToBoard(board);
    }

    public Board(final Board game) {
        this.board = new Piece[ROWS][COLS];
        this.moves = new Move[PLAYERS][MOVES_POSSIBLE];
        this.options = new int[PLAYERS];
        this.pieceCount = new int[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = game.board[i][j] == null ? null : new Piece(game.board[i][j], new Cell(i, j));
            }
        }
        for (int i = 0; i < PLAYERS; i++) {
            for (int j = 0; j < game.options[i]; j++) {
                moves[i][j] = new Move(game.moves[i][j]);
            }
        }
        System.arraycopy(game.options, 0, options, 0, PLAYERS);
        System.arraycopy(game.pieceCount, 0, pieceCount, 0, PLAYERS);
    }

    public Board play(final Move move) {
        final Board copy = new Board(this);
        copy.makeMove(move);
        return copy;
    }

    public void makeMove(final Move move) {
        final int player = move.piece.player;
        final int opponent = MinMax.flip(player);
        removePieceFromCaptures(opponent, move.piece);
        for (int i = 0; i < options[player]; ) {
            if (moves[player][i].end.equals(move.end) || moves[player][i].start.equals(move.start)) {
                options[player]--;
                moves[player][i] = moves[player][options[player]];
            } else {
                i++;
            }
        }
        accountForCaptures(move, player, opponent);
        changeDirectionIfRequired(move);
        movePieceOnBoard(move);
        for (int k = -2; k <= 2; k++) {
            for (int l = -2; l <= 2; l++) {
                if (!(k == 0 && l == 0)) {
                    final int i = move.end.x + k, j = move.end.y + l;
                    if (i >= 0 && i < ROWS && j >= 0 && j < COLS && board[i][j] != null && board[i][j].player == opponent) {
                        final Cell start = new Cell(i, j);
                        for (final int[] movesTo : board[i][j].coin.movesTo) {
                            if (!((j == 0 && movesTo[0] < 0)
                                    || (j == COLS - 1 && movesTo[0] > 0))) {
                                final int direction = board[i][j].movingUp ? 1 : -1;
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
                                if (x == move.end.x && y == move.end.y) {
                                    final List<Piece> captures = new ArrayList<>(2);
                                    if (board[x][y] != null) {
                                        captures.add(board[x][y]);
                                    }
                                    final Cell end = new Cell(x, y);
                                    int index = 0;
                                    for (; index < options[opponent]; index++) {
                                        Move current = moves[opponent][index];
                                        if (current.start.equals(start) && current.end.equals(end)) {
                                            break;
                                        }
                                    }
                                    if (index < options[opponent]) {
                                        moves[opponent][index].capturedPieces.add(board[x][y]);
                                    } else {
                                        moves[opponent][options[opponent]++] = new Move(start,
                                                                                        end,
                                                                                        board[i][j],
                                                                                        captures);
                                    }
                                } else if (!board[i][j].coin.equals(Coin.PAWN)) {
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
                                        if (intermediateX == move.end.x && intermediateY == move.end.y) {
                                            final int finalIntermediateX = intermediateX;
                                            final int finalIntermediateY = intermediateY;
                                            final int finalDestinationX = x;
                                            final int finalDestinationY = y;
                                            Arrays.stream(moves[opponent])
                                                    .filter(Objects::nonNull)
                                                    .filter(m -> m.start.x == i)
                                                    .filter(m -> m.start.y == j)
                                                    .filter(m -> m.end.x == finalDestinationX)
                                                    .filter(m -> m.end.y == finalDestinationY)
                                                    .findAny()
                                                    .ifPresent(m -> m.capturedPieces.add(board[finalIntermediateX][finalIntermediateY]));
                                        }
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
                        if (board[x][y] != null) {
                            captures.add(board[x][y]);
                        }
                        checkForIntermediates(board, i, j, player, movesTo, direction, captures);
                        moves[player][options[player]++] = new Move(start, new Cell(x, y), board[i][j], captures);
                    } else if (x == i && y == j) {
                        final List<Piece> captures = new ArrayList<>(2);
                        checkForIntermediates(board, i, j, player, movesTo, direction, captures);
                        moves[player][options[player]++] = new Move(start, new Cell(x, y), board[i][j], captures);
                    }
                }
            }
        }
    }

    public void movePieceOnBoard(final Move move) {
        board[move.start.x][move.start.y] = null;
        board[move.end.x][move.end.y] = move.piece;
        move.piece.position = new Cell(move.end.x, move.end.y);
    }

    public void changeDirectionIfRequired(final Move move) {
        if ((move.end.x == 0 && move.start.x != 0) || (move.end.x == ROWS - 1 && move.start.x != ROWS - 1)
                || (move.end.x == move.start.x
                && (move.piece.coin.equals(Coin.BISHOP)
                || (move.piece.coin.equals(Coin.ROOK)
                && move.end.y == move.start.y)))) {
            move.piece.movingUp = !move.piece.movingUp;
        }
    }

    public void accountForCaptures(final Move move, final int player, final int opponent) {
        pieceCount[opponent] -= move.capturedPieces.size();
        for (final Piece capture : move.capturedPieces) {
            for (int i = 0; i < options[opponent]; ) {
                if (moves[opponent][i].start.equals(capture.position)) {
                    options[opponent]--;
                    moves[opponent][i] = moves[opponent][options[opponent]];
                } else {
                    i++;
                }
            }
            removePieceFromCaptures(player, capture);
            board[capture.position.x][capture.position.y] = null;
        }
    }

    public void removePieceFromCaptures(final int player, final Piece piece) {
        for (int i = 0; i < options[player]; i++) {
            if (moves[player][i].capturedPieces.contains(piece)) {
                moves[player][i].capturedPieces.remove(piece);
            }
        }
    }

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
                        if (!((j == 0 && movesTo[0] < 0) || (j == COLS - 1 && movesTo[0] > 0))) {
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
                                if (pieces[x][y] != null) {
                                    captures.add(pieces[x][y]);
                                }
                                checkForIntermediates(pieces, i, j, player, movesTo, direction, captures);
                                moves[player][options[player]++] = new Move(start,
                                                                            new Cell(x, y),
                                                                            pieces[i][j],
                                                                            captures);
                            } else if (x == i && y == j) {
                                final List<Piece> captures = new ArrayList<>(2);
                                checkForIntermediates(pieces, i, j, player, movesTo, direction, captures);
                                moves[player][options[player]++] = new Move(start,
                                                                            new Cell(x, y),
                                                                            pieces[i][j],
                                                                            captures);
                            }
                        }
                    }
                }
            }
        }
        return pieces;
    }

    private void checkForIntermediates(final Piece[][] pieces,
                                       final int i,
                                       final int j,
                                       final int player,
                                       final int[] movesTo, final int direction, final List<Piece> captures) {
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

    public int heuristicValue(final int movesPlayed,
                              final int firstPlayerPieceCount,
                              final int secondPlayerPieceCount) {
        final Integer terminated = isTerminated(movesPlayed, firstPlayerPieceCount, secondPlayerPieceCount);
        return terminated != null ? terminated : (firstPlayerPieceCount - secondPlayerPieceCount) * MinMax.PIECE_VALUE;
    }

    public int evaluatePosition() {
        return (pieceCount[1] - pieceCount[2]) * MinMax.PIECE_VALUE;
    }

    public Integer isTerminated(final int moveNumber,
                                final int firstPlayerPieceCount,
                                final int secondPlayerPieceCount) {
        final boolean hasEnded = moveNumber >= 100 || firstPlayerPieceCount == 0 || secondPlayerPieceCount == 0;
        if (hasEnded) {
            assert firstPlayerPieceCount + secondPlayerPieceCount > 0;
            return firstPlayerPieceCount > secondPlayerPieceCount ? MinMax.MAX_VALUE : MinMax.MIN_VALUE;
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }

    @Override
    public String toString() {
        return "Board{" +
                "board=" + toReadableString() +
                ", options=" + Arrays.toString(options) +
                '}';
    }

    private String toReadableString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\n');
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                stringBuilder.append(board[i][j] == null ? "000" : board[i][j].toReadableString()).append(' ');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    public boolean isEndGame(final int level) {
        return pieceCount[1] + pieceCount[2] < 9 || level > 50;
    }

    public static class Cell {
        public static Cell[][] CELLS = new Cell[ROWS][COLS];

        static {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    CELLS[i][j] = new Cell(i, j);
                }
            }
        }

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
        return this == o || !(o == null || getClass() != o.getClass()) && Arrays.deepEquals(board, ((Board) o).board);
    }
}