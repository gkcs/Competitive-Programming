package main.java.game;

public class TicTacToe {
    public static void main(String args[]) {
        final Board board = new Board();
        board.makeMove(new Move(1, 1, Player.X));
        board.makeMove(new Move(1, 2, Player.O));
        final Move move = new Move(0, 0, Player.X);
        board.makeMove(move);
        System.out.println(board);
        board.undo(move);
        System.out.println(board);
    }
}

class Board {
    public static final int BOARD_SIZE = 3;
    private final Player[][] grid = new Player[BOARD_SIZE][BOARD_SIZE];

    public Board makeMove(final Move move) {
        grid[move.x][move.y] = move.player;
        return this;
    }

    public Board undo(final Move move) {
        grid[move.x][move.y] = null;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                stringBuilder.append(grid[i][j] == null ? "-" : grid[i][j]).append(' ');
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}

class Move {
    final int x, y;
    final Player player;

    Move(final int x, final int y, final Player player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }
}

enum Player {
    X, O
}