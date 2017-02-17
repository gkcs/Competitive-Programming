package main.java.codingame;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class GreatEscape {

    public static void main(String args[]) throws IOException {
        final InputReader br = new InputReader(System.in);
        final int width = br.readInt();
        final int height = br.readInt();
        final int playerCount = br.readInt();
        final int id = br.readInt();
        while (true) {
            for (int i = 0; i < playerCount; i++) {
                final int x = br.readInt();
                final int y = br.readInt();
                final int wallsLeft = br.readInt();
            }
            int wallCount = br.readInt();
            for (int i = 0; i < wallCount; i++) {
                final int wallX = br.readInt();
                final int wallY = br.readInt();
                final String wallOrientation = br.readString();
            }
            // action: LEFT, RIGHT, UP, DOWN or "putX putY putOrientation" to place a wall
            System.out.println("RIGHT");
        }
    }
}

class Move {
    final Board.Cell cell;
    final byte player;

    public Move(final Board.Cell cell, final byte player) {
        this.cell = cell;
        this.player = player;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Move move = (Move) o;
        return cell.equals(move.cell) && player == move.player;
    }

    @Override
    public int hashCode() {
        return 31 * cell.hashCode() + player;
    }

    @Override
    public String toString() {
        return "Move{" +
                "cell=" + cell +
                ", player=" + player +
                '}';
    }
}

class Wall {
    final Board.Cell cell;
    final boolean horizontal;

    public Wall(final Board.Cell cell, final boolean horizontal) {
        this.cell = cell;
        this.horizontal = horizontal;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass()) return false;
        final Wall placement = (Wall) o;
        return cell.equals(placement.cell) && horizontal == placement.horizontal;
    }

    @Override
    public int hashCode() {
        return 31 * cell.hashCode() + (horizontal ? 1 : 0);
    }

    @Override
    public String toString() {
        return "Move{" +
                "cell=" + cell +
                ", horizontal=" + horizontal +
                '}';
    }
}

class Board {
    private static final int ROWS = 9;
    public static final int COLS = 9;
    private static final int PLAYERS = 4;
    final Cell[] playerPositions;
    final Wall[] wallPositions;
    final Move moves[][];
    final Wall emptyPlaces[];
    final int bricksRemaining[];
    int wallCount;
    public static final Move MOVES[][][] = new Move[ROWS][COLS][PLAYERS];
    public static final Wall WALLS[][][] = new Wall[ROWS][COLS][PLAYERS];
    public final long[] smallRepresentation;

    public static void setUp() {
        Board.setBoard();
    }

    public Board(final Cell[] playerPositions, final Wall[] wallPositions, final int[] bricksRemaining) {
        this.playerPositions = playerPositions;
        this.wallPositions = wallPositions;
        moves = new Move[PLAYERS][COLS];
        emptyPlaces = new Wall[ROWS * COLS];
        this.bricksRemaining = bricksRemaining;
        smallRepresentation = getSmallRepresentation();
    }

    public Board(final Board input, final Move move) {
        this.playerPositions = new Cell[PLAYERS];
        this.wallPositions = new Wall[ROWS * COLS];
        moves = new Move[PLAYERS][COLS];
        emptyPlaces = new Wall[ROWS * COLS];
        bricksRemaining = new int[PLAYERS];
        smallRepresentation = getSmallRepresentation();
    }

    private long[] getSmallRepresentation() {
        final long hashCode[] = new long[3];
        for (int i = 1; i < PLAYERS; i++) {
            hashCode[0] = hashCode[0] | (((long) playerPositions[i].pos) << ((i - 1) * 7));
        }
        int bricksPerRow[] = new int[ROWS];
        for (int i = 0; i < wallCount; i++) {
            bricksPerRow[wallPositions[i].cell.x]++;
            if (wallPositions[i].horizontal) {
                byte pos = wallPositions[i].cell.pos;
                if (pos < 64) {
                    hashCode[1] |= 1L << pos;
                } else {
                    hashCode[2] |= 1 << (pos - 64);
                }
            }
        }
        for (int i = 0; i < ROWS; i++) {
            hashCode[0] = hashCode[0] | (((long) bricksPerRow[i]) << (21 + (i << 2)));
        }
        for (int i = 1; i < PLAYERS; i++) {
            hashCode[2] = hashCode[2] | (((long) bricksRemaining[i]) << (50 + ((i - 1) << 2)));
        }
        return hashCode;
    }

    public Board play(final Move move) {
        return this;
    }

    public int isTerminated() {
        return 1;
    }

    public int heuristicValue(final int player) {
        return evaluatePositionShallow(player);
    }

    public int evaluatePositionShallow(final int player) {
        return 0;
    }

    public int evaluatePositionDeep(final int player) {
        return 0;
    }

    public Board undo(final Move move) {
        return this;
    }

    private static void setBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                final Cell cell = new Cell(i, j);
                for (byte k = 1; k < PLAYERS; k++) {
                    MOVES[i][j][k] = new Move(cell, k);
                }
                WALLS[i][j][0] = new Wall(cell, true);
                WALLS[i][j][1] = new Wall(cell, false);
            }
        }
    }

    public static class Cell {
        final int x, y;
        final byte pos;

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Board.Cell cell = (Board.Cell) o;
            return pos == cell.pos;
        }

        @Override
        public int hashCode() {
            return pos;
        }

        private Cell(final int x, final int y) {
            this.pos = (byte) (COLS * x + y);
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + " " + y;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Board board = (Board) o;
        return Arrays.equals(smallRepresentation, board.smallRepresentation);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(smallRepresentation);
    }
}

class InputReader {
    private InputStream stream;
    private byte[] buf = new byte[1024];

    private int curChar;

    private int numChars;

    public InputReader(InputStream stream) {
        this.stream = stream;
    }

    public int read() {
        if (numChars == -1)
            throw new RuntimeException();
        if (curChar >= numChars) {
            curChar = 0;
            try {
                numChars = stream.read(buf);
            } catch (IOException e) {
                throw new RuntimeException();
            }
            if (numChars <= 0)
                return -1;
        }
        return buf[curChar++];
    }

    public String readString() {
        final StringBuilder stringBuilder = new StringBuilder();
        int c = read();
        while (isSpaceChar(c))
            c = read();
        do {
            stringBuilder.append(c);
            c = read();
        } while (!isSpaceChar(c));
        return stringBuilder.toString();
    }

    public int readInt() {
        int c = read();
        while (isSpaceChar(c))
            c = read();
        int sgn = 1;
        if (c == '-') {
            sgn = -1;
            c = read();
        }
        int res = 0;
        do {
            res *= 10;
            res += c - '0';
            c = read();
        } while (!isSpaceChar(c));
        return res * sgn;
    }

    public long readLong() {
        int c = read();
        while (isSpaceChar(c))
            c = read();
        int sgn = 1;
        if (c == '-') {
            sgn = -1;
            c = read();
        }
        long res = 0;
        do {
            res *= 10;
            res += c - '0';
            c = read();
        } while (!isSpaceChar(c));
        return res * sgn;
    }

    public boolean isSpaceChar(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
    }
}