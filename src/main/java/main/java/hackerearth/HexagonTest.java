package main.java.hackerearth;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class HexagonTest {
    @Test
    public void dontJumpLikeAMoron4() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {1, 0, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}
        };
        //minMax.setTest(true);
        final String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
    }

    @Test
    public void doNotJumpLikeAMoron() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {1, 1, 0, 0, 0, 2, 2},
                {1, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 2, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1}
        };
        //minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
    }

    @Test
    public void doNotJumpLikeAMoron2() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {1, 0, 0, 0, 0, 2, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 1, 1}
        };
//        minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
    }

    @Test
    public void doNotJumpLikeAMoron3() {
        final MinMax minMax = new MinMax(0);
        String s = minMax.iterativeSearchForBestMove(new int[][]{
                {1, 1, 0, 0, 0, 0, 2},
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 0},
                {2, 0, 0, 0, 0, 0, 1},
                {2, 0, 0, 0, 0, 0, 1}
        }, 1);
        minMax.setTest(true);
        System.out.println(s);
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
    }

    @Test
    public void dontGoBeserk() {
        final MinMax minMax = new MinMax(0);
        final int[][] game = new int[][]{
                {2, 2, 2, 2, 0, 0, 2},
                {0, 2, 2, 2, 2, 0, 0},
                {2, 2, 2, 2, 2, 0, 0},
                {2, 1, 2, 2, 2, 2, 0},
                {1, 2, 2, 1, 2, 1, 0},
                {2, 2, 1, 1, 1, 1, 0}
        };
        //minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 1);
        System.out.println(s);
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
        assertNotEquals("3 1\n1 0", s);
    }

    @Test
    public void learnToAcceptThings() {
        Board.setThoseWithinSightAndMoves();
        final int[][] game = new int[][]{
                {2, 2, 2, 2, 1, 1, 2},
                {1, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 2, 2, 2},
                {2, 1, 2, 2, 2, 2, 2},
                {1, 2, 2, 1, 2, 1, 1},
                {2, 2, 1, 1, 1, 1, 2}
        };
        //minMax.setTest(true);
        final Board board = new Board(game);
        assertTrue(board.isTerminated(1, 1, 2));
        assertTrue(board.isTerminated(2, 1, 2));
        System.out.println(Arrays.stream(board.hashCode)
                                   .mapToObj(c -> c)
                                   .map(Integer::toBinaryString)
                                   .collect(Collectors.toList()));
    }

    @Test
    public void takeAsMuchAsPossible() {
        final MinMax minMax = new MinMax(99);
        final int[][] game = new int[][]{
                {1, 1, 2, 1, 1, 1, 1},
                {1, 1, 2, 1, 1, 1, 1},
                {1, 2, 2, 2, 1, 1, 1},
                {2, 1, 2, 2, 2, 2, 1},
                {2, 1, 1, 1, 2, 2, 2},
                {2, 1, 1, 1, 0, 0, 2}
        };
        //minMax.setTest(true);
        String s = minMax.iterativeSearchForBestMove(game, 2);
        System.out.println(minMax.eval + " " + minMax.depth + " "
                                   + minMax.moves + " " + minMax.computations + " " + minMax.cacheHits);
        System.out.println(s);
        assertNotEquals("5 6\n5 4", s);
    }
}
