package main.java;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulator {

    public static void main(String[] args) {
        final int processors = Runtime.getRuntime().availableProcessors();
        System.out.println(processors);
        final ExecutorService executorService = Executors.newFixedThreadPool(processors);
        for (int i = 0; i < processors; i++) {
            executorService.submit(() -> playGame(new Board(new int[5][5][2])));
        }
    }

    private static void playGame(Board board) {
        int count = 0;
        while (true) {
            MinMax.TIME_OUT = 10000;
//            Board.heuristicEval = (vals) -> Arrays.stream(vals).sum();
            playMove(board, new MinMax(), 1, count);
            MinMax.TIME_OUT = 1000;
//            Board.heuristicEval = (vals) -> (int) (vals[0] * Math.random() + vals[1] * Math.random() + vals[2] * Math.random() + vals[3] * Math.random()) * 2;
            playMove(board, new MinMax(), 2, count);
            count++;
        }
    }

    private static void playMove(final Board board, final MinMax minMax, final int player, int count) {
        final String[] split = minMax.iterativeSearchForBestMove(board.board, player).split(" ");
        board.play(new Move(Integer.parseInt(split[0]), Integer.parseInt(split[1]), player));
        System.out.println("MOVE: " + count + " " + player);
        System.out.println(Arrays.toString(split));
        System.out.println(board + (player == 1 ? "true);" : "false);"));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves);
    }
}
