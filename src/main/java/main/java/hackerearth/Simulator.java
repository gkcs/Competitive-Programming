//package main.java.hackerearth;
//
//public class Simulator {
//
//    public static void main(String[] args) {
//        new Simulator().playGame();
//    }
//
//    private void playGame() {
//        final byte game[][] = new byte[][]{
//                {1, 0, 0, 0, 0, 0, 2},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0},
//                {2, 0, 0, 0, 0, 0, 1}
//        };
//        Board.setUp();
//        MinMax.MAX_DEPTH = 7;
//        Board board = new Board(game);
//        int player = 1;
//        for (int moveNumber = 0;
//             moveNumber < 10 && !board.isTerminated();
//             moveNumber++, player = player == 1 ? 2 : 1) {
//            MinMax minMax = new MinMax((int) (Math.pow(10, 8) * 9));
//            minMax.setTest(true);
//            Move move = minMax.iterativeSearchForBestMove(player, board);
//            board = board.play(move);
//            System.out.println(board.heuristicValue(player));
//            if (minMax.eval == MinMax.MAX_VALUE) {
//                break;
//            }
//        }
//        //System.out.println(toString(board.board) + (player == 1 ? "true);" : "false);"));
//    }
//
//    private String toString(byte[][] board) {
//        final StringBuilder stringBuilder = new StringBuilder("cache.put(new Board(new int[][]{");
//        stringBuilder.append('\n');
//        for (final byte row[] : board) {
//            stringBuilder.append('{');
//            for (final int col : row) {
//                stringBuilder.append(col).append(',');
//            }
//            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
//            stringBuilder.append('}').append(',').append('\n');
//        }
//        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length()).append("}),");
//        return stringBuilder.toString();
//    }
//}
