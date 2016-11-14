package main.java;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Simulator {

    public static void main(String[] args) {
        final int processors = 1;
        System.out.println(processors);
        MinMax.TIME_OUT = 1000;
        final double weights[] = new double[]{0.8981021545478284, 0.11315068151712826, 0.28796079678263675,
                0.4523222133863911, 2.0};
        final ExecutorService executorService = Executors.newFixedThreadPool(processors);
        for (int i = 0; i < processors; i++) {
            executorService.submit(() -> playGame(new Board(new int[5][5][2]),
                                                  (vals) -> (int) ((weights[0] * vals[0]
                                                          + weights[1] * vals[1]
                                                          + weights[2] * vals[2]
                                                          + weights[3] * vals[3]) * weights[4]),
                                                  getHeuristicFunction()));
        }
    }

    private static Function<int[], Integer> getHeuristicFunction() {
        final double randoms[] = new double[]{Math.random(), Math.random(), Math.random(), Math.random(), 2};
        System.out.println("STARTING:" + Arrays.toString(randoms));
        return (vals) -> (int) ((vals[0] * randoms[0]
                + vals[1] * randoms[1]
                + vals[2] * randoms[2]
                + vals[3] * randoms[3]) * randoms[4]);
    }

    private static void playGame(final Board board,
                                 final Function<int[], Integer> sums,
                                 final Function<int[], Integer> random) {
        int count = 0;
        try {
            while (true) {
                Board.heuristicEval = sums;
                playMove(board, new MinMax(), 1, count);
                count++;
                Board.heuristicEval = random;
                playMove(board, new MinMax(), 2, count);
                count++;
            }
        } catch (Exception ignore) {
            System.out.println("End of play");
            if (count % 2 == 0) {
                System.out.println("First player lost. Changing First function: ");
                playGame(new Board(new int[5][5][2]), getHeuristicFunction(), random);
            } else {
                System.out.println("Second player lost. Changing second function");
                playGame(new Board(new int[5][5][2]), sums, getHeuristicFunction());
            }
            System.exit(1);
        }
    }

    private static void playMove(final Board board, final MinMax minMax, final int player, int count) {
        final String[] split = minMax.iterativeSearchForBestMove(board.board, player).split(" ");
        board.play(new Move(Integer.parseInt(split[0]), Integer.parseInt(split[1]), player));
        System.out.println("MOVE: " + count + " " + player);
        System.out.println(Arrays.toString(split));
        if (Math.abs(minMax.eval - MinMax.MAX_VALUE) < 100) {
            System.out.println(board + (player == 1 ? "true);" : "false);"));
        }
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves);
    }
}
