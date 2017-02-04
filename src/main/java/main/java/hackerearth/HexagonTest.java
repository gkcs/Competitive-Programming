//package main.java.hackerearth;
//
//import main.java.MinMax;
//import org.junit.Test;
//
//import java.util.Arrays;
//import java.util.stream.Collectors;
//
//import static org.junit.Assert.*;
//
//public class HexagonTest {
//    @Test
//    public void dontJumpLikeAMoron4() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        final byte[][] game = new byte[][]{
//                {1, 0, 0, 0, 0, 0, 2},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 0, 1}
//        };
//        //minMax.setTest(true);
//        final String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
//    }
//
//    @Test
//    public void doNotJumpLikeAMoron() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        MinMax.MAX_DEPTH = 6;
//        final byte[][] game = new byte[][]{
//                {1, 1, 0, 0, 0, 2, 2},
//                {1, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 2, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 0, 1}
//        };
//        minMax.setTest(true);
//        String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
//    }
//
//    @Test
//    public void doNotJumpLikeAMoron2() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        MinMax.MAX_DEPTH = 6;
//        final byte[][] game = new byte[][]{
//                {1, 0, 0, 0, 0, 2, 2},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 1, 1}
//        };
//        minMax.setTest(true);
//        String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
//    }
//
//    @Test
//    public void doNotJumpLikeAMoron3() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        //MinMax.MAX_DEPTH=7;
//        final byte[][] game = new byte[][]{
//                {1, 1, 0, 0, 0, 0, 2},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 0, 1},
//                {2, 0, 0, 0, 0, 0, 1}
//        };
//        //minMax.setTest(true);
//        String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
//    }
//
//    @Test
//    public void dontTimeOut() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        final byte[][] game = new byte[][]{
//                {0, 0, 2, 2, 0, 0, 2},
//                {2, 2, 2, 2, 0, 0, 0},
//                {2, 2, 2, 1, 1, 1, 2},
//                {0, 0, 0, 0, 1, 1, 2},
//                {2, 0, 0, 0, 2, 1, 1},
//                {2, 2, 0, 0, 2, 1, 1}
//        };
//        String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//    }
//
//    @Test
//    public void dontJumpLikeAMoron5() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        //minMax.setTest(true);
//        //MinMax.MAX_DEPTH=7;
//        final byte[][] game = new byte[][]{
//                {1, 1, 0, 0, 0, 0, 2},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 0, 1}
//        };
//        String s = minMax.iterativeSearchForBestMove(2, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//        assertTrue(Math.abs(s.charAt(0) - s.charAt(4)) + Math.abs(s.charAt(2) - s.charAt(6)) < 2);
//    }
//
//    @Test
//    public void dontGoBeserk() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        final byte[][] game = new byte[][]{
//                {2, 2, 2, 2, 0, 0, 2},
//                {0, 2, 2, 2, 2, 0, 0},
//                {2, 2, 2, 2, 2, 0, 0},
//                {2, 1, 2, 2, 2, 2, 0},
//                {1, 2, 2, 1, 2, 1, 0},
//                {2, 2, 1, 1, 1, 1, 0}
//        };
//        //minMax.setTest(true);
//        String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//        assertNotEquals("3 1\n1 0", s);
//    }
//
//    @Test
//    public void learnToAcceptThings() {
//        Board.setUp();
//        final byte[][] game = new byte[][]{
//                {2, 2, 2, 2, 1, 1, 2},
//                {1, 2, 2, 2, 2, 2, 2},
//                {2, 2, 2, 2, 2, 2, 2},
//                {2, 1, 2, 2, 2, 2, 2},
//                {1, 2, 2, 1, 2, 1, 1},
//                {2, 2, 1, 1, 1, 1, 2}
//        };
//        //minMax.setTest(true);
//        final Board board = new Board(game);
//        assertTrue(board.isTerminated());
//        assertTrue(board.isTerminated());
//        System.out.println(Arrays.stream(board.hashCode)
//                                   .mapToObj(c -> c)
//                                   .map(Integer::toBinaryString)
//                                   .collect(Collectors.toList()));
//    }
//
//    @Test
//    public void takeAsMuchAsPossible() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        final byte[][] game = new byte[][]{
//                {1, 1, 2, 1, 1, 1, 1},
//                {1, 1, 2, 1, 1, 1, 1},
//                {1, 2, 2, 2, 1, 1, 1},
//                {2, 1, 2, 2, 2, 2, 1},
//                {2, 1, 1, 1, 2, 2, 2},
//                {2, 1, 1, 1, 0, 0, 2}
//        };
//        //minMax.setTest(true);
//        String s = minMax.iterativeSearchForBestMove(2, new Board(game)).describe();
//        minMax.metrics();
//        System.out.println(s);
//        assertNotEquals("5 6\n5 4", s);
//    }
//
//    /*
//     * 1 2 2 2 0 2 2
//1 2 2 2 2 1 1
//1 2 2 1 1 1 1
//2 2 2 2 1 2 1
//2 2 1 1 1 1 1
//0 2 2 1 1 1 1
//68
//1
//     */
//    @Test
//    public void playWell() {
//        int currentDepth = 68;
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        final byte[][] game = new byte[][]{
//                {1, 2, 2, 2, 0, 2, 2},
//                {1, 2, 2, 2, 2, 1, 1},
//                {1, 2, 2, 1, 1, 1, 1},
//                {2, 2, 2, 2, 1, 2, 1},
//                {2, 2, 1, 1, 1, 1, 1},
//                {0, 2, 2, 1, 1, 1, 1}
//        };
//        //minMax.setTest(true);
//        Board board = new Board(game);
//        String s = minMax.iterativeSearchForBestMove(1, board).describe();
//        System.out.println(s);
//        minMax.metrics();
//    }
//
//    @Test
//    public void playWell2() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        //MinMax.MAX_DEPTH = 25;
//        final byte[][] game = new byte[][]{
//                {1, 2, 2, 2, 0, 2, 2},
//                {1, 2, 2, 2, 2, 1, 1},
//                {1, 2, 2, 1, 1, 1, 1},
//                {2, 2, 2, 2, 1, 2, 1},
//                {2, 2, 1, 1, 1, 1, 1},
//                {0, 2, 2, 1, 1, 1, 1}
//        };
//        //minMax.setTest(true);
//        Board original = new Board(game).play(new Move(Board.CELLS[1][6], (byte) 1));
////        Board oneMove = original.play(new Move(Board.CELLS[0][6], Board.CELLS[1][6], (byte) 2, false));
////        System.out.println(Arrays.deepToString(oneMove.board));
////        Board twoMoves = oneMove.play(new Move(Board.CELLS[4][2], Board.CELLS[5][0], (byte) 1, true));
////        System.out.println(Arrays.deepToString(twoMoves.board));
//        String s = minMax.iterativeSearchForBestMove(2, original).describe();
//        System.out.println(s);
//        minMax.metrics();
//    }
//
//    /*
//    0 1 1 2 2 2 2
//2 2 1 2 2 0 0
//2 2 2 2 2 0 0
//1 1 2 1 2 0 0
//1 1 1 1 1 0 0
//2 1 1 0 0 0 2
//     */
//    @Test
//    public void playWell3() {
//        final MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//        //MinMax.MAX_DEPTH = 7;
//        final byte[][] game = new byte[][]{
//                {0, 1, 1, 2, 2, 2, 2},
//                {2, 2, 1, 2, 2, 0, 0},
//                {2, 2, 2, 2, 2, 0, 0},
//                {1, 1, 2, 1, 2, 0, 0},
//                {1, 1, 1, 1, 1, 0, 0},
//                {2, 1, 1, 0, 0, 0, 2}
//        };
//        //minMax.setTest(true);
//        String s = minMax.iterativeSearchForBestMove(1, new Board(game)).describe();
//        System.out.println(s);
//        minMax.metrics();
//    }
//}
