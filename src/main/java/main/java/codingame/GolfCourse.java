package main.java.codingame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GolfCourse {
    public static void main(String args[]) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final String dimensions[] = in.readLine().split(" ");
        final int width = Integer.parseInt(dimensions[0]);
        final int height = Integer.parseInt(dimensions[1]);
        final char[][] grid = new char[height][];
        for (int i = 0; i < height; i++) {
            grid[i] = in.readLine().toCharArray();
        }
        System.err.println(printRaw(grid));
        final String solution = run(grid);
        System.err.println(solution);
        System.out.println(solution);
    }

    public static String run(char[][] grid) {
        final Game game = new Game(grid);
        if (!game.play(0)) {
            throw new RuntimeException("No possible solution");
        }
        return game.solutionBoard();
    }

    public static String printRaw(final char[][] grid) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (char[] row : grid) {
            for (char col : row) {
                stringBuilder.append(col);
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}

class Game {
    final Board board;

    Game(final char[][] grid) {
        board = new Board(grid);
    }

    boolean play(final int ballIndex) {
        if (ballIndex == board.ballCount) {
            return true;
        }
        char[][] grid = board.grid;
        int direction = 0;
        for (; direction < Move.symbols.length; direction++) {
            final int x = board.balls[ballIndex][0];
            final int y = board.balls[ballIndex][1];
            final Move move = new Move(x, y, direction, grid[y][x] - '0');
            if (board.isPossibleMove(move)) {
                final char[] previous = board.makeMove(move, ballIndex);
                if (play(grid[board.balls[ballIndex][1]][board.balls[ballIndex][0]] == 'H' ? ballIndex + 1 : ballIndex)) {
                    return true;
                } else {
                    board.undo(move, previous, ballIndex);
                }
            }
        }
        return false;
    }

    String solutionBoard() {
        return board.print();
    }
}

class Move {
    @Override
    public String toString() {
        return "Move{" +
                "x=" + x +
                ", y=" + y +
                ", direction=" + direction +
                ", power=" + power +
                '}';
    }

    final int x, y, direction, power;
    static final char symbols[] = {'v', '^', '>', '<'};
    static final int diff[][] = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

    public Move(final int x, final int y, final int direction, final int power) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.power = power;
    }

    static boolean isASymbol(char character) {
        for (char symbol : symbols) {
            if (character == symbol) {
                return true;
            }
        }
        return false;
    }
}

class Board {
    final char[][] grid;
    private final boolean[][] filled;
    final int balls[][];
    int ballCount;

    Board(final char[][] grid) {
        this.grid = grid;
        this.balls = new int[((grid.length * grid[0].length) + 1) >> 1][2];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] > '0' && grid[i][j] <= '9') {
                    balls[ballCount][0] = j;
                    balls[ballCount++][1] = i;
                }
            }
        }
        this.filled = new boolean[grid.length][grid[0].length];
    }

    boolean isPossibleMove(final Move move) {
        boolean flag = true;
        if (move.power == 0) {
            return false;
        }
        final int xDest = move.x + move.power * Move.diff[move.direction][0];
        final int yDest = move.y + move.power * Move.diff[move.direction][1];
        if (xDest < 0 || xDest >= grid[0].length || yDest < 0 || yDest >= grid.length) {
            return false;
        }
        int xTrajectory = move.x, yTrajectory = move.y;
        xTrajectory += Move.diff[move.direction][0];
        yTrajectory += Move.diff[move.direction][1];
        for (int i = 1; i < move.power; i++) {
            if (grid[yTrajectory][xTrajectory] == '.' || grid[yTrajectory][xTrajectory] == 'X') {
                xTrajectory += Move.diff[move.direction][0];
                yTrajectory += Move.diff[move.direction][1];
            } else {
                flag = false;
                break;
            }
        }
        if (move.power == 1 || grid[yTrajectory][xTrajectory] != '.') {
            if (grid[yTrajectory][xTrajectory] != 'H' || filled[yTrajectory][xTrajectory]) {
                flag = false;
            }
        }
        return flag;
    }

    Board undo(final Move move, final char[] previous, final int ballIndex) {
        grid[move.y][move.x] = (char) ('0' + move.power);
        balls[ballIndex][0] = move.x;
        balls[ballIndex][1] = move.y;
        int xTrajectory = move.x, yTrajectory = move.y;
        for (int i = 0; i < move.power; i++) {
            xTrajectory += Move.diff[move.direction][0];
            yTrajectory += Move.diff[move.direction][1];
            grid[yTrajectory][xTrajectory] = previous[i];
        }
        filled[yTrajectory][xTrajectory] = false;
        return this;
    }

    char[] makeMove(final Move move, int ballIndex) {
        final char[] copy = new char[move.power];
        makeMove(move, copy, ballIndex);
        return copy;
    }

    Board makeMove(final Move move, final char[] copy, final int ballIndex) {
        int xTrajectory = move.x, yTrajectory = move.y;
        final boolean isAHole = grid[move.y + move.power * Move.diff[move.direction][1]][move.x + move.power * Move.diff[move.direction][0]] == 'H';
        for (int i = 0; i < move.power; i++) {
            grid[yTrajectory][xTrajectory] = Move.symbols[move.direction];
            xTrajectory += Move.diff[move.direction][0];
            yTrajectory += Move.diff[move.direction][1];
            copy[i] = grid[yTrajectory][xTrajectory];
        }
        if (!isAHole) {
            grid[yTrajectory][xTrajectory] = (char) ('0' + move.power - 1);
        } else {
            filled[yTrajectory][xTrajectory] = true;
        }
        balls[ballIndex][0] = xTrajectory;
        balls[ballIndex][1] = yTrajectory;
        return this;
    }

    String print() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (char[] row : grid) {
            for (char col : row) {
                if (Move.isASymbol(col)) {
                    stringBuilder.append(col);
                } else {
                    stringBuilder.append('.');
                }
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}