package main.java.codingame.wondevwoman;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class WondevWoman {

    public static void main(String[] args) throws IOException {
        final Scanner in = new Scanner(System.in);
        final int size = in.nextInt();
        Board.COLS = Board.ROWS = size;
        final int unitsPerPlayer = in.nextInt();
        final MinMax minMax = new MinMax(30);
        while (true) {
            final byte board[][] = new byte[size][size];
            for (int i = 0; i < size; i++) {
                final char row[] = in.next().toCharArray();
                for (int j = 0; j < size; j++) {
                    if (row[j] == '.') {
                        board[i][j] = -1;
                    } else {
                        board[i][j] = (byte) (row[j] - '0');
                    }
                }
            }
            Board.setUp(board);
            final Unit[][] units = new Unit[Board.PLAYERS][2];
            for (int i = 0; i < unitsPerPlayer; i++) {
                units[0][i] = new Unit(0, i, Board.CELLS[in.nextInt()][in.nextInt()]);
            }
            for (int i = 0; i < unitsPerPlayer; i++) {
                final int x = in.nextInt();
                final int y = in.nextInt();
                if (x >= 0) {
                    units[1][i] = new Unit(1, i, Board.CELLS[x][y]);
                }
            }
            final int legalActions = in.nextInt();
            final Action[] actions = new Action[legalActions];
            for (int i = 0; i < legalActions; i++) {
                actions[i] = new Action(in.next(), in.nextInt(), in.next(), in.next());
            }
            if (legalActions == 0) {
                System.out.println("ACCEPT-DEFEAT");
            } else {
                final Board gameBoard = new Board(board, units, actions);
                final Move col = minMax.iterativeSearchForBestMove(gameBoard);
                System.out.println(col.describe());
                minMax.metrics();
            }
        }
    }
}

class Action {
    final String type;
    final int unit;
    final int[] direction1;
    final int[] direction2;
    final String dir1, dir2;

    public Action(final String type, final int unit, final String direction1, final String direction2) {
        this.type = type;
        this.unit = unit;
        dir1 = direction1;
        dir2 = direction2;
        this.direction1 = getDirection(direction1);
        this.direction2 = getDirection(direction2);
    }

    private int[] getDirection(final String direction) {
        switch (direction) {
            case "N":
                return new int[]{-1, 0};
            case "S":
                return new int[]{1, 0};
            case "E":
                return new int[]{0, 1};
            case "W":
                return new int[]{0, -1};
            case "NE":
                return new int[]{-1, 1};
            case "NW":
                return new int[]{-1, -1};
            case "SE":
                return new int[]{1, 1};
            case "SW":
                return new int[]{1, -1};
        }
        throw new RuntimeException();
    }

    public String toString() {
        return type + " " + unit + " " + dir1 + " " + dir2;
    }
}

class MinMax {
    public static final int SCORE_VALUE = 1000;
    public static final int MOVE_VALUE = 100;
    public static int MAX_DEPTH = 60;
    public final int TIME_OUT;
    private int computations = 0, depth = 1, moves = 0;
    public long eval;
    public static final int MAX_VALUE = 1000000;
    private final long startTime = System.currentTimeMillis();
    private boolean test = false;
    private MinMax.Configuration[] startConfigs;
    private boolean timeOut;

    public MinMax(final int timeOut) {
        TIME_OUT = timeOut;
        eval = 0;
    }

    public Move iterativeSearchForBestMove(final Board board) {
        final int player = 0;
        if (board.options[player] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new MinMax.Configuration[board.options[player]];
        for (int i = 0; i < startConfigs.length; i++) {
            startConfigs[i] = new MinMax.Configuration(board.moves[player][i], board);
        }
        Arrays.sort(startConfigs, getConfigurationComparator(player));
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player, board);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.options[player];
        return bestMove;
    }

    public void printStats() {
        System.out.println(Arrays.stream(startConfigs)
                .map(configuration -> configuration.move)
                .map(move -> "START " + move.unit.cell + " END " + move.destination + " BUILD " + move.build)
                .collect(Collectors.joining("\n")));
        System.out.println(Arrays.stream(startConfigs)
                .map(configuration -> configuration.strength)
                .collect(Collectors.toList()));
    }

    private Move findBestMove(final int player, final Board board) {
        int toTake = startConfigs[0].strength - 2 * SCORE_VALUE, toGive = startConfigs[0].strength + 2 * SCORE_VALUE;
        int result = player == 0 ? toTake : toGive;
        Move bestMove = startConfigs[0].move;
        try {
            for (final MinMax.Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(board.play(possibleConfig.move), flip(player), 0, toTake, toGive);
                possibleConfig.strength = moveValue;
                if (player == 0) {
                    if (toTake < moveValue) {
                        toTake = moveValue;
                    }
                } else if (toGive > moveValue) {
                    toGive = moveValue;
                }
                if (player == 0 && result < moveValue) {
                    result = moveValue;
                    bestMove = possibleConfig.move;
                } else if (player == 1 && result > moveValue) {
                    result = moveValue;
                    bestMove = possibleConfig.move;
                }
            }
        } catch (TimeoutException ignored) {
        }
        Arrays.sort(startConfigs, getConfigurationComparator(player));
        return bestMove;
    }

    private Comparator<MinMax.Configuration> getConfigurationComparator(final int player) {
        return (first, second) -> (player == 0 ? 1 : -1) * (second.strength - first.strength);
    }

    private int evaluate(final Board board,
                         final int player,
                         final int level,
                         final int a,
                         final int b) throws TimeoutException {
        int toTake = a, toGive = b;
        int result = player == 0 ? a : b;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            timeOut = true;
            throw new TimeoutException();
        }
        final boolean terminated = board.isTerminated();
        if (terminated && level >= depth) {
            result = board.evaluatePosition();
        } else {
            final Configuration[] configurations = new Configuration[board.options[player]];
            for (int i = 0; i < configurations.length; i++) {
                configurations[i] = new Configuration(board.moves[player][i],
                        board
                );
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
                if (player == 0) {
                    if (toTake < moveValue) {
                        toTake = moveValue;
                    }
                } else if (toGive > moveValue) {
                    toGive = moveValue;
                }
                if (player == 0 && result < moveValue) {
                    result = moveValue;
                } else if (player == 1 && result > moveValue) {
                    result = moveValue;
                }
                if (toTake >= toGive) {
                    result = moveValue;
                }
            }
        }
        return result;
    }

    public void metrics() {
        System.err.println(eval + " " + depth + " " + moves + " " + computations);
    }

    public class Configuration {
        final Move move;
        int strength;

        private Configuration(final Move move,
                              final Board board) {
            this.strength = board.heuristicValue();
            this.move = move;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "move=" + move +
                    ", strength=" + strength +
                    '}';
        }
    }

    static int flip(final int player) {
        return player ^ 1;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}

abstract class Move {
    final Board.Cell destination, build;
    final Unit unit;
    final String direction1, direction2;

    public Move(final Board.Cell destination,
                final Board.Cell build,
                final Unit unit,
                final String direction1,
                final String direction2) {
        this.destination = destination;
        this.build = build;
        this.unit = unit;
        this.direction1 = direction1;
        this.direction2 = direction2;
    }

    public String describe() {
        return "&BUILD " + unit.id + " " + direction1 + " " + direction2;
    }

    public static int getRow(final String type) {
        switch (type) {
            case "N":
            case "NE":
            case "NW":
                return -1;
            case "SE":
            case "SW":
            case "S":
                return 1;
            default:
                return 0;
        }
    }

    public static int getCol(final String type) {
        switch (type) {
            case "E":
            case "NE":
            case "SE":
                return 1;
            case "W":
            case "SW":
            case "NW":
                return -1;
            default:
                return 0;
        }
    }

    public abstract void playOnBoard(final Board board);
}

class Movement extends Move {

    public Movement(final Unit unit, final String direction1, final String direction2) {
        super(Board.CELLS[unit.cell.x + getRow(direction1)][unit.cell.y + getCol(direction1)],
                Board.CELLS[unit.cell.x + getRow(direction1) + getRow(direction2)][unit.cell.y + getRow(direction1) + getCol(direction2)],
                unit, direction1, direction2);
    }

    @Override
    public void playOnBoard(final Board board) {
        board.board[build.x][build.y]++;
        for (int i = 0; i < 2; i++) {
            if (board.units[unit.player][i].equals(unit)) {
                final Unit myUnit = new Unit(unit.player, unit.id, destination);
                board.units[unit.player][i] = myUnit;
            }
        }
        if (board.board[destination.x][destination.y] == 3) {
            board.score[unit.player]++;
        }
        for (final Unit coins[] : board.units) {
            for (final Unit coin : coins) {
                board.addMoves(coin);
            }
        }
    }

    @Override
    public String describe() {
        return "MOVE" + super.describe();
    }
}

class Push extends Move {

    public Push(final Unit unit, final String direction1, final String direction2) {
        super(unit.cell, Board.CELLS[unit.cell.x + getRow(direction1)][unit.cell.y + getCol(direction1)], unit, direction1, direction2);
    }

    @Override
    public void playOnBoard(final Board board) {
        board.board[build.x][build.y]++;
        for (int i = 0; i < 2; i++) {
            final Unit opponent = board.units[MinMax.flip(unit.player)][i];
            if (opponent.cell == build) {
                final Unit unit = new Unit(opponent.player, opponent.id,
                        Board.CELLS[opponent.cell.x + getRow(direction2)][opponent.cell.y + getCol(direction2)]);
                board.units[opponent.player][i] = unit;
            }
        }
    }

    @Override
    public String describe() {
        return "PUSH" + super.describe();
    }
}

class Unit {
    final int player;
    final int id;
    final Board.Cell cell;

    public Unit(final int player, final int id, final Board.Cell cell) {
        this.player = player;
        this.id = id;
        this.cell = cell;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Unit unit = (Unit) o;
        return player == unit.player && id == unit.id && cell.equals(unit.cell);
    }

    @Override
    public int hashCode() {
        int result = player;
        result = 31 * result + id;
        result = 31 * result + cell.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "player=" + player +
                ", id=" + id +
                ", cell=" + cell +
                '}';
    }
}

class Board {
    public static int ROWS;
    public static int COLS;
    final byte[][] board;
    final Unit units[][];
    final int score[];
    final Move moves[][];
    final int options[];
    public static final int PLAYERS = 2;
    public static boolean hasBeenSetUp = false;
    public static Cell CELLS[][];
    private static Cell[][][] neighbours;

    public static void setUp(final byte[][] board) {
        if (hasBeenSetUp) {
            return;
        }
        CELLS = new Cell[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                CELLS[i][j] = new Cell(i, j);
            }
        }
        neighbours = new Cell[ROWS][COLS][];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final List<Cell> cells = new ArrayList<>();
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        final int currentRow = i + x, currentCol = j + y;
                        if (currentRow >= 0 && currentRow < ROWS && currentCol >= 0 && currentCol < COLS && !(x == 0 && y == 0) && board[currentRow][currentCol] != -1) {
                            cells.add(Board.CELLS[currentRow][currentCol]);
                        }
                    }
                }
                neighbours[i][j] = cells.toArray(new Cell[cells.size()]);
            }
        }
        hasBeenSetUp = true;
    }

    public Board(final byte[][] board, final Unit[][] units, final Action[] actions) {
        this.units = units;
        this.moves = new Move[PLAYERS][128];
        this.options = new int[PLAYERS];
        this.board = board;
        score = new int[PLAYERS];
        options[0] = actions.length;
        System.err.println(Arrays.toString(actions));
        System.err.println(Arrays.deepToString(units));
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].type.contains("PUSH")) {
                moves[0][i] = new Push(units[0][actions[i].unit], actions[i].dir1, actions[i].dir2);
            } else {
                moves[0][i] = new Movement(units[0][actions[i].unit], actions[i].dir1, actions[i].dir2);
            }
        }
        for (final Unit opponent : units[1]) {
            addMoves(opponent);
        }
    }

    public void addMoves(final Unit unit) {
        for (final Cell neighbour : neighbours[unit.cell.x][unit.cell.y]) {
            if (board[neighbour.x][neighbour.y] < 4 && board[unit.cell.x][unit.cell.y] >= board[neighbour.x][neighbour.y] - 1) {
                final Unit[] opponents = units[MinMax.flip(unit.player)];
                final boolean neighbourHasOpponent = opponents[0].cell == neighbour || opponents[1].cell == neighbour;
                if (units[unit.player][unit.id ^ 1].cell != neighbour) {
                    for (final Cell distantNeighbour : neighbours[neighbour.x][neighbour.y]) {
                        if (board[distantNeighbour.x][distantNeighbour.y] < 4 && board[neighbour.x][neighbour.y] >= board[distantNeighbour.x][distantNeighbour.y] - 1) {
                            if (neighbourHasOpponent) {
                                moves[unit.player][options[unit.player]++] = new Push(unit, getDirection(unit.cell, neighbour), getDirection(neighbour, distantNeighbour));
                            } else {
                                moves[unit.player][options[unit.player]++] = new Movement(unit, getDirection(unit.cell, neighbour), getDirection(neighbour, distantNeighbour));
                            }
                        }
                    }
                }
            }
        }
    }

    private String getDirection(final Cell cell, final Cell neighbour) {
        String direction = "";
        if (cell.x < neighbour.x) {
            direction += "S";
        } else if (cell.x > neighbour.x) {
            direction += "N";
        }
        if (cell.y < neighbour.y) {
            direction += "E";
        } else if (cell.y > neighbour.y) {
            direction += "W";
        }
        return direction;
    }

    public Board(final Board game) {
        this.board = new byte[ROWS][COLS];
        this.moves = new Move[PLAYERS][128];
        this.options = new int[PLAYERS];
        this.units = game.units;
        this.score = new int[PLAYERS];
        for (int i = 0; i < ROWS; i++) {
            System.arraycopy(game.board[i], 0, board[i], 0, COLS);
        }
        System.arraycopy(game.score, 0, score, 0, PLAYERS);
    }

    public Board play(final Move move) {
        final Board copy = new Board(this);
        move.playOnBoard(copy);
        return copy;
    }

    public int heuristicValue() {
        final boolean terminated = isTerminated();
        if (terminated) {
            return evaluatePosition();
        } else {
            return evaluatePosition() + (options[0] - options[1]) * MinMax.MOVE_VALUE;
        }
    }

    public int evaluatePosition() {
        final int score = (this.score[0] - this.score[1]) * MinMax.SCORE_VALUE;
        if (isTerminated()) {
            return score > 0 ? MinMax.MAX_VALUE : -MinMax.MAX_VALUE;
        } else {
            return score;
        }
    }

    public boolean isTerminated() {
        return (options[0] + options[1] == 0) || (options[0] == 0 && score[1] > score[0]) || (options[1] == 0 && score[0] > score[1]);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && Arrays.deepEquals(board, ((Board) o).board);
    }

    public static class Cell {
        final int x, y;

        public Cell(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "[" + x + ", " + y + ']';
        }
    }
}