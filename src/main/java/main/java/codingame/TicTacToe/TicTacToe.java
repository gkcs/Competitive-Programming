package main.java.codingame.TicTacToe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TicTacToe {
    public static void main(String args[]) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final BruteForceAlgorithm bruteForceAlgorithm = new BruteForceAlgorithm();
        final Board[][] boards = new Board[3][3];
        for (int i = 0; i < boards.length; i++) {
            for (int j = 0; j < boards[i].length; j++) {
                boards[i][j] = new Board();
            }
        }
        while (true) {
            String line[] = in.readLine().split(" ");
            final int opponentRow = Integer.parseInt(line[0]), opponentCol = Integer.parseInt(line[1]);
            if (opponentCol >= 0) {
                Board opponentBoard = boards[opponentRow / 3][opponentCol / 3];
                opponentBoard.play(0, (opponentRow % 3) * 3 + opponentCol % 3);
                System.err.println(opponentBoard);
                System.err.println((opponentRow / 3) + " " + (opponentCol / 3));
            }
            final int validActionCount = Integer.parseInt(in.readLine());
            Board board = boards[0][0];
            int bRow = 0, bCol = 0;
            for (int i = 0; i < validActionCount; i++) {
                line = in.readLine().split(" ");
                bRow = Integer.parseInt(line[0]);
                bCol = Integer.parseInt(line[1]);
                board = boards[bRow / 3][bCol / 3];
            }
            final int bestMove = bruteForceAlgorithm.findBestMove(board);
            final int row = bestMove / 3, col = bestMove % 3;
            board.play(1, bestMove);
            System.out.println(((bRow / 3) * 3 + row) + " " + ((bCol / 3) * 3 + col));
            System.err.println(board);
        }
    }
}

class BruteForceAlgorithm {
    public int findBestMove(Board board) {
        for (int i = 0; i < 9; i++) {
            if ((board.occupied & (1 << i)) == 0) {
                board.play(1, i);
                final boolean result = board.result(1);
                board.undo(1, i);
                if (result) {
                    return i;
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            if ((board.occupied & (1 << i)) == 0) {
                board.play(0, i);
                final boolean  result = board.result(0);
                board.undo(0, i);
                if (result) {
                    return i;
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            if ((board.occupied & (1 << i)) == 0) {
                board.play(1, i);
                int waysToWin = 0;
                for (int j = 0; j < 9; j++) {
                    if ((board.occupied & (1 << j)) == 0) {
                        board.play(1, j);
                        if (board.result(1)) {
                            waysToWin++;
                        }
                        board.undo(1, j);
                    }
                }
                board.undo(1, i);
                if (waysToWin > 1) {
                    return i;
                }
            }
        }
        int forks = 0;
        int block = -1;
        for (int i = 0; i < 9; i++) {
            if ((board.occupied & (1 << i)) == 0) {
                board.play(1, i);
                int waysToWin = 0;
                for (int j = 0; j < 9; j++) {
                    if ((board.occupied & (1 << j)) == 0) {
                        board.play(0, j);
                        if (board.result(0)) {
                            waysToWin++;
                        }
                        board.undo(0, j);
                    }
                }
                board.undo(1, i);
                if (waysToWin > 1) {
                    forks++;
                    block = i;
                }
            }
        }
        if (forks > 1) {
            for (int i = 0; i < 9; i++) {
                if ((board.occupied & (1 << i)) == 0) {
                    board.play(1, i);
                    int waysToWin = 0;
                    for (int j = 0; j < 9; j++) {
                        if ((board.occupied & (1 << j)) == 0) {
                            board.play(1, j);
                            if (board.result(1)) {
                                waysToWin++;
                            }
                            board.undo(1, j);
                        }
                    }
                    board.undo(1, i);
                    if (waysToWin == 1) {
                        return i;
                    }
                }
            }
        } else if (forks == 1) {
            return block;
        }
        if ((board.occupied & 16) == 0) {
            return 4;
        }
        int bestMove = -1;
        int bestValue = Integer.MIN_VALUE;
        for (int i = 0; i < 9; i++) {
            if ((board.occupied & (1 << i)) == 0) {
                final int result = i / 3 == 2 || i % 3 == 2 ? 1 : 2;
                if (result > bestValue) {
                    bestValue = result;
                    bestMove = i;
                }
            }
        }
        return bestMove;
    }
}

class Board {
    int occupied;
    int board;
    final int winningStates[] = new int[]{
            0b111_000_000,
            0b000_111_000,
            0b000_000_111,
            0b100_100_100,
            0b010_010_010,
            0b001_001_001,
            0b100_010_001,
            0b001_010_100
    };

    public void play(final int player, final int p) {
        final int position = 1 << p;
        if ((occupied & position) != 0) {
            throw new IllegalStateException("Already occupied!" + p);
        }
        if (player == 1) {
            board = board | position;
        }
        occupied = occupied | position;
    }

    public void undo(final int player, final int p) {
        final int position = 1 << p;
        if ((occupied & position) == 0) {
            throw new IllegalStateException("Not occupied!" + p);
        }
        if (player == 1) {
            board = board ^ position;
        }
        occupied = occupied ^ position;
    }

    public boolean result(final int player) {
        final int boardForPlayer = player == 1 ? board : ~board;
        for (final int winningState : winningStates) {
            if (winningState == (boardForPlayer & occupied & winningState)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Occupied:" + Integer.toBinaryString(occupied) + "\nBoard:" + Integer.toBinaryString(board);
    }
}