package main.java;

import java.util.Arrays;

public class Simulator {
    private static int count = 0;

    public static void main(String[] args) {
        final Board board = new Board(new int[5][5][2]);
        while (true) {
            playMove(board, new MinMax(), 1);
            playMove(board, new MinMax(), 2);
        }
    }

    private static void playMove(final Board board, final MinMax minMax, final int player) {
        final String[] split = minMax.iterativeSearchForBestMove(board.board, player).split(" ");
        board.play(new Move(Integer.parseInt(split[0]), Integer.parseInt(split[1]), player));
        System.out.println("MOVE: " + (count++) + " " + player);
        System.out.println(Arrays.toString(split));
        System.out.println(board);
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves);
    }
}
