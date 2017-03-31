package main.java.hackerearth.taunt;

import org.junit.Assert;
import org.junit.Test;

public class TauntTest {

    @Test
    public void test0() {
        final MinMax minMax = new MinMax(700, 10);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 000\n" +
                                                                                      "000 121 111 111\n" +
                                                                                      "111 131 131 121\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "121 000 230 000\n" +
                                                                                      "000 000 220 000\n" +
                                                                                      "000 230 000 210\n" +
                                                                                      "210 000 000 220\n" +
                                                                                      "220 210 000 230"));
        Assert.assertNotEquals(Board.Cell.CELLS[7][0], move.end);
    }

    @Test
    public void test1() {
        final MinMax minMax = new MinMax(700, 28);
//        minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("131 000 000 000\n" +
                                                                                      "121 230 000 111\n" +
                                                                                      "111 000 000 121\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "210 000 000 000\n" +
                                                                                      "220 000 000 000\n" +
                                                                                      "000 000 000 220\n" +
                                                                                      "000 000 230 000\n" +
                                                                                      "210 210 220 000\n" +
                                                                                      "000 230 000 000"));
        System.out.println(move);
        Assert.assertTrue(Board.Cell.CELLS[1][1].equals(move.end) || (Board.Cell.CELLS[2][2].equals(move.end)
                && Board.Cell.CELLS[0][0].equals(move.start)));
    }

    @Test
    public void test2() {
        final MinMax minMax = new MinMax(700, 12);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("131 131 000 121\n" +
                                                                                      "121 000 111 230\n" +
                                                                                      "000 111 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 220 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 210\n" +
                                                                                      "210 210 000 220\n" +
                                                                                      "220 000 230 230"));
        Assert.assertEquals(Board.Cell.CELLS[0][3], move.start);
        Assert.assertEquals(Board.Cell.CELLS[2][3], move.end);
    }

    @Test
    public void test3() {
        final MinMax minMax = new MinMax(700, 4);
//        minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("131 131 131 121\n" +
                                                                                      "121 000 111 111\n" +
                                                                                      "111 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 121 000 000\n" +
                                                                                      "000 000 220 000\n" +
                                                                                      "220 000 000 210\n" +
                                                                                      "210 210 000 220\n" +
                                                                                      "000 230 230 230"));
        Assert.assertNotEquals(Board.Cell.CELLS[5][1], move.start);
    }

    @Test
    public void test4() {
        final MinMax minMax = new MinMax(700, 0);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("131 131 131 121\n" +
                                                                                      "121 121 111 111\n" +
                                                                                      "111 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 210\n" +
                                                                                      "210 210 220 220\n" +
                                                                                      "220 230 230 230"));
    }

    @Test
    public void test5() {
        final MinMax minMax = new MinMax(700, 44);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 111 000 121\n" +
                                                                                      "000 121 000 111\n" +
                                                                                      "131 111 131 000\n" +
                                                                                      "121 000 000 000\n" +
                                                                                      "000 131 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 220 000\n" +
                                                                                      "220 230 230 210\n" +
                                                                                      "210 210 000 220\n" +
                                                                                      "000 230 000 000"));
        Assert.assertNotEquals(Board.Cell.CELLS[4][1], move.start);
    }

    @Test
    public void test6() {
        final MinMax minMax = new MinMax(700, 45);
        final Move move = minMax.iterativeSearchForBestMove(2, constructBoard("000 111 000 121\n" +
                                                                                      "000 121 000 111\n" +
                                                                                      "131 111 131 000\n" +
                                                                                      "121 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 131 220 000\n" +
                                                                                      "220 230 230 210\n" +
                                                                                      "210 210 000 220\n" +
                                                                                      "000 230 000 000"));
        Assert.assertEquals(Board.Cell.CELLS[6][2], move.start);
        Assert.assertEquals(Board.Cell.CELLS[6][1], move.end);
    }

    @Test
    public void test7() {
        final MinMax minMax = new MinMax(700, 30);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 000\n" +
                                                                                      "121 000 111 111\n" +
                                                                                      "111 131 131 121\n" +
                                                                                      "230 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 230 000 210\n" +
                                                                                      "210 220 000 220\n" +
                                                                                      "000 000 220 000\n" +
                                                                                      "000 210 000 000\n" +
                                                                                      "000 000 000 230"));
        Assert.assertEquals(Board.Cell.CELLS[3][0], move.end);
    }

    @Test
    public void test8() {
        final MinMax minMax = new MinMax(700, 8);
        //minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 000 121\n" +
                                                                                      "121 121 111 111\n" +
                                                                                      "111 131 131 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 131 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "220 230 000 210\n" +
                                                                                      "210 210 220 220\n" +
                                                                                      "000 230 230 000"));
        Assert.assertNotEquals(Board.Cell.CELLS[4][2], move.start);
    }

    @Test
    public void test9() {
        final MinMax minMax = new MinMax(700, 5);
        final Move move = minMax.iterativeSearchForBestMove(2, constructBoard("131 131 131 121\n" +
                                                                                      "121 000 111 111\n" +
                                                                                      "111 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "121 000 000 000\n" +
                                                                                      "000 000 220 000\n" +
                                                                                      "220 000 000 210\n" +
                                                                                      "210 210 000 220\n" +
                                                                                      "000 230 230 230"));
        Assert.assertEquals(Board.Cell.CELLS[7][0], move.start);
        Assert.assertEquals(Board.Cell.CELLS[5][0], move.end);
    }

    @Test
    public void test10() {
        final MinMax minMax = new MinMax(700, 22);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 000\n" +
                                                                                      "000 000 111 111\n" +
                                                                                      "111 131 131 121\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "121 230 230 000\n" +
                                                                                      "000 220 000 210\n" +
                                                                                      "000 230 000 000\n" +
                                                                                      "210 000 000 220\n" +
                                                                                      "000 210 000 000"));
        Assert.assertNotEquals(Board.Cell.CELLS[7][0], move.end);
    }

    @Test
    public void test11() {
        final MinMax minMax = new MinMax(700, 24);
//        minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 000\n" +
                                                                                      "000 111 000 111\n" +
                                                                                      "111 131 131 121\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "121 230 230 000\n" +
                                                                                      "000 220 000 210\n" +
                                                                                      "000 230 000 000\n" +
                                                                                      "210 000 000 220\n" +
                                                                                      "210 000 000 000"));
        Assert.assertNotEquals(Board.Cell.CELLS[7][0], move.end);
    }

    @Test
    public void test13() {
        final MinMax minMax = new MinMax(700, 10);
//        minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 121\n" +
                                                                                      "121 121 111 111\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "111 000 000 000\n" +
                                                                                      "131 220 000 131\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "210 000 000 000\n" +
                                                                                      "000 000 000 210\n" +
                                                                                      "000 210 000 220\n" +
                                                                                      "220 230 230 230"));
        minMax.metrics();
        System.out.println(move);
        Assert.assertNotEquals(Board.Cell.CELLS[4][0], move.start);
    }

    @Test
    public void test14() {
        final MinMax minMax = new MinMax(7000, 24);
        //minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 000\n" +
                                                                                      "121 000 111 111\n" +
                                                                                      "111 131 000 121\n" +
                                                                                      "121 230 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 000 230 230\n" +
                                                                                      "000 000 000 220\n" +
                                                                                      "220 210 000 210\n" +
                                                                                      "210 000 220 000\n" +
                                                                                      "000 000 000 000"));
        minMax.metrics();
        System.out.println(move);
        Assert.assertNotEquals(Board.Cell.CELLS[1][3], move.start);
    }

    @Test
    public void test15() {
        final MinMax minMax = new MinMax(7500, 22);
        //minMax.setTest(true);
        final Move move = minMax.iterativeSearchForBestMove(1, constructBoard("000 000 131 000\n" +
                                                                                      "121 000 111 111\n" +
                                                                                      "111 131 131 121\n" +
                                                                                      "121 000 000 000\n" +
                                                                                      "000 000 000 000\n" +
                                                                                      "000 230 230 230\n" +
                                                                                      "000 000 000 220\n" +
                                                                                      "220 210 000 210\n" +
                                                                                      "210 000 220 000\n" +
                                                                                      "000 000 000 000"));
        minMax.metrics();
        System.out.println(move);
        Assert.assertNotEquals(Board.Cell.CELLS[2][2], move.start);
    }


    private Board constructBoard(final String input) {
        final String rows[] = input.split("\n");
        final byte[][] board = new byte[Board.ROWS][Board.COLS];
        for (int i = 0; i < Board.ROWS; i++) {
            final String cols[] = rows[i].split(" ");
            for (int j = 0; j < Board.COLS; j++) {
                board[i][j] |= (cols[j].charAt(2) - '0');
                board[i][j] |= ((cols[j].charAt(1) - '0') & 3) << 1;
                board[i][j] |= ((cols[j].charAt(0) - '0') & 3) << 3;
            }
        }
        return new Board(board);
    }
}
