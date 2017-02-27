package main.java.videos;

class Board {
    private int[][] game;

    Board(final int[][] game) {
        this.game = game;
    }

    public Board makeMove(final Move move) {
        return this;
    }

    public Board undoMove(final Move move) {
        return this;
    }
}
