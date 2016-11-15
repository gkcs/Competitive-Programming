package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Iterative deepening and alpha beta are the ways to move forward.
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
 * A DFS results in too many resources consumed for the first possibility. A breadth first search is a better choice
 * selection strategy. A queue of positions is maintained. Each position has a corresponding parent position, which
 * helps move up the tree when the current position is evaluated.
 * <p>
 * A better approach will be iterative deepening. Local search around an 'interesting' area is feasible in these
 * scenarios.
 * Alpha Beta pruning is necessary so that we do not blindly follow whatever we see.
 */
public class MinMax {
    public static int TIME_OUT = 910;
    public int computations = 0, depth = 3, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;

    static {
        Board.setMoves();
        Board.setNeighbours();
    }

    public String iterativeSearchForBestMove(final int[][][] board, final int player) {
        String bestMove = "LOL";
        while (depth < 60) {
            try {
                bestMove = findBestMove(board, player, depth);
            } catch (final Exception e) {
                if (!e.getMessage().equals("Time out...")) {
                    bestMove = e.getMessage() + " " + e.getClass().getSimpleName();
                }
                break;
            } finally {
                depth++;
            }
        }
        return bestMove;
    }

    private String findBestMove(final int[][][] rawBoard,
                                final int player,
                                final int level) {
        final Board board = new Board(rawBoard);
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        long max = MIN_VALUE;
        final Move[] allPossibleMoves = board.getAllPossibleMoves(player);
        if (board.getChoiceCount(player) == 0) {
            throw new RuntimeException("No possible moves");
        }
        final BoardMove[] possibleConfigs = new BoardMove[board.getChoiceCount(player)];
        for (int i = 0; i < possibleConfigs.length; i++) {
            possibleConfigs[i] = new BoardMove(allPossibleMoves[i], board, player);
        }
        Arrays.sort(possibleConfigs);
        Move bestMove = possibleConfigs[0].move;
        for (final BoardMove possibleConfig : possibleConfigs) {
            final long moveValue;
            moveValue = evaluate(possibleConfig.board, flip(player), level, toTake, toGive);
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
                max = moveValue;
                break;
            }
        }
        eval = max;
        moves = board.getChoiceCount(player);
        return bestMove.describe();
    }

    private long evaluate(final Board board, final int player, final int level, final long a, final long b) {
        long toTake = a, toGive = b;
        long max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            throw new RuntimeException("Time out...");
        }
        final Integer terminalValue;
        if ((terminalValue = board.terminalValue()) != null) {
            max = value(terminalValue, player);
            max += max < 0 ? level : -level;
        } else if (level <= 0) {
            max = board.heuristicValue(player);
        } else {
            final Move[] allPossibleMoves = board.getAllPossibleMoves(player);
            final BoardMove[] possibleConfigs = new BoardMove[board.getChoiceCount(player)];
            for (int i = 0; i < possibleConfigs.length; i++) {
                possibleConfigs[i] = new BoardMove(allPossibleMoves[i], board, player);
            }
            Arrays.sort(possibleConfigs);
            for (final BoardMove possibleConfig : possibleConfigs) {
                final long moveValue;
                computations++;
                moveValue = evaluate(possibleConfig.board, flip(player), level - 1, toTake, toGive);
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
                    break;
                }
            }
        }
        return -max;
    }

    private static class BoardMove implements Comparable<BoardMove> {
        final Move move;
        final Board board;

        final int strength;

        private BoardMove(final Move move, final Board board, final int player) {
            final Move moveToBeMade = Board.ALL_MOVES[player][move.x][move.y];
            this.board = board.makeMove(moveToBeMade);
            this.strength = this.board.heuristicValue(player);
            this.move = moveToBeMade;
        }

        @Override
        public int compareTo(BoardMove o) {
            return o.strength - strength;
        }

        @Override
        public String toString() {
            return "BoardMove{" +
                    "move=" + move +
                    ", board=" + board +
                    '}';
        }

    }

    private long value(final long moveValue, final int player) {
        return moveValue * (player == 1 ? 1 : -1);
    }

    static int flip(final int player) {
        return ~player & 3;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}

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
    public boolean equals(Object o) {
        if (this == o) return true;
        Move move = (Move) o;
        return x == move.x && y == move.y && player == move.player;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + player;
        return result;
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
 * The board contains a list of all of its parents. Each time someone asks us to undo the board, we fall back to a
 * copy of the board in the previous state. Whenever a state changing move is made on the board, the current state is
 * stored as a parent in memory, and another immutable Board is returned.
 */
class Board {
    Function<int[], Integer> heuristicEval = (vals) -> Arrays.stream(vals).sum();
    int[][][] board;
    private static final int BOARD_SIZE = 5;
    private static final int neighbours[][][] = new int[BOARD_SIZE][BOARD_SIZE][];
    public static final int COLORS = 3;
    private final Move[][] moves = new Move[COLORS][BOARD_SIZE * BOARD_SIZE];
    private final int choices[] = new int[COLORS];
    static final Move ALL_MOVES[][][] = new Move[COLORS][BOARD_SIZE][BOARD_SIZE];

    Board(final int[][][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                moves[board[i][j][0]][choices[board[i][j][0]]++] = ALL_MOVES[board[i][j][0]][i][j];
            }
        }
        this.board = getCopy(board);
    }

    private Board(final int[][][] board, final Move[][] moves, final int choices[]) {
        System.arraycopy(choices, 0, this.choices, 0, choices.length);
        for (int i = 0; i < COLORS; i++) {
            System.arraycopy(moves[i], 0, this.moves[i], 0, choices[i]);
        }
        this.board = getCopy(board);
    }

    Move[] getAllPossibleMoves(final int player) {
        final Move[] possibilities = new Move[choices[0] + choices[player]];
        System.arraycopy(moves[player], 0, possibilities, 0, choices[player]);
        System.arraycopy(moves[0], 0, possibilities, choices[player], choices[0]);
        return possibilities;
    }

    int getChoiceCount(final int player) {
        return choices[player] + choices[0];
    }

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

    Board makeMove(final Move move) {
        return new Board(board, moves, choices).play(move);
    }

    Board play(final Move move) {
        if (board[move.x][move.y][0] == MinMax.flip(move.player)) {
            final int opponent = MinMax.flip(move.player);
            int index;
            for (index = choices[opponent] - 1; index >= 0; index--) {
                if (moves[opponent][index].x == move.x && moves[opponent][index].y == move.y) {
                    break;
                }
            }
            System.arraycopy(moves[opponent],
                             index + 1,
                             moves[opponent],
                             index,
                             choices[opponent] - 1 - index);
            choices[opponent]--;
            moves[move.player][choices[move.player]++] = ALL_MOVES[move.player][move.x][move.y];
            //handle other players deletion
        } else if (board[move.x][move.y][0] == 0) {
            int index;
            for (index = choices[0] - 1; index >= 0; index--) {
                if (moves[0][index].x == move.x && moves[0][index].y == move.y) {
                    break;
                }
            }
            System.arraycopy(moves[0],
                             index + 1,
                             moves[0],
                             index,
                             choices[0] - 1 - index);
            choices[0]--;
            moves[move.player][choices[move.player]++] = ALL_MOVES[move.player][move.x][move.y];
        }
        board[move.x][move.y][0] = move.player;
        board[move.x][move.y][1]++;
        if (terminalValue() != null) {
            return this;
        }
        if (neighbours[move.x][move.y].length <= board[move.x][move.y][1]) {
            board[move.x][move.y][1] = board[move.x][move.y][1] - neighbours[move.x][move.y].length;
            if (board[move.x][move.y][1] == 0) {
                board[move.x][move.y][0] = 0;
                int index;
                for (index = choices[move.player] - 1; index >= 0; index--) {
                    if (moves[move.player][index].x == move.x && moves[move.player][index].y == move.y) {
                        break;
                    }
                }
                System.arraycopy(moves[move.player],
                                 index + 1,
                                 moves[move.player],
                                 index,
                                 choices[move.player] - 1 - index);
                choices[move.player]--;
                moves[0][choices[0]++] = ALL_MOVES[0][move.x][move.y];
            }
            explode(move.x, move.y, move.player);
        }
        return this;
    }

    private void explode(final int x, final int y, final int player) {
        for (final int neighbour : neighbours[x][y]) {
            play(ALL_MOVES[player][neighbour / BOARD_SIZE][neighbour % BOARD_SIZE]);
        }
    }

    Integer terminalValue() {
        if ((choices[1] + choices[2] > 1) && (choices[1] == 0 || choices[2] == 0)) {
            return choices[1] == 0 ? MinMax.MIN_VALUE : MinMax.MAX_VALUE;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("map.put(new Board(new int[][][]{");
        for (final int row[][] : board) {
            stringBuilder.append('{');
            for (final int col[] : row) {
                stringBuilder.append('{');
                for (final int content : col) {
                    stringBuilder.append(content).append(',');
                }
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                stringBuilder.append('}').append(',');
            }
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.append('}').append(',').append('\n');
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append("}),");
        return stringBuilder.toString();
    }

    int heuristicValue(final int player) {
        int orbs = 0;
        int inThreat = 0;
        int bonus = 0;
        int contiguous = 0;
        for (int m = 0; m < choices[player]; m++) {
            final int i = moves[player][m].x;
            final int j = moves[player][m].y;
            orbs += board[i][j][1];
            if (board[i][j][1] == neighbours[i][j].length - 1) {
                ++contiguous;
            }
            boolean surround = false;
            for (int k = 0; k < neighbours[i][j].length; k++) {
                final int row = neighbours[i][j][k] / BOARD_SIZE;
                final int col = neighbours[i][j][k] % BOARD_SIZE;
                final int[] neighbour = board[row][col];
                final int criticalMass = neighbours[row][col].length - 1;
                if (neighbour[0] == MinMax.flip(player) && neighbour[1] == criticalMass) {
                    inThreat -= 5 - criticalMass;
                    surround = true;
                }
            }
            if (!surround) {
                if (neighbours[i][j].length < 4) {
                    bonus += neighbours[i][j].length == 3 ? 2 : 3;
                }
                if (board[i][j][1] == neighbours[i][j].length - 1) {
                    bonus += 2;
                }
            }
        }
        contiguous <<= 1;
        return heuristicEval.apply(new int[]{orbs, inThreat, bonus, contiguous});
    }

    private int[][][] getCopy(final int board[][][]) {
        final int copyBoard[][][] = new int[board.length][board.length][2];
        for (int k = 1; k < COLORS; k++) {
            for (int l = 0; l < choices[k]; l++) {
                final int i = moves[k][l].x;
                final int j = moves[k][l].y;
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        return copyBoard;
    }

    @Override
    public boolean equals(Object o) {
        final Board other = (Board) o;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < 2; k++) {
                    if (other.board[i][j][k] != board[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    static void setMoves() {
        for (int player = 0; player < COLORS; player++) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    ALL_MOVES[player][i][j] = new Move(i, j, player);
                }
            }
        }
    }
}