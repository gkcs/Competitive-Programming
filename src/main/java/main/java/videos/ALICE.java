package main.java.videos;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ALICE {
    public static void main(String[] args) {
        final InputReader inputReader = new InputReader(System.in);
        final int DIMENSIONS = inputReader.readInt(),
                fenceCount = inputReader.readInt(),
                startX = inputReader.readInt() - 1,
                startY = inputReader.readInt() - 1,
                penalty = inputReader.readInt();
        final int[][] flowers = new int[DIMENSIONS][DIMENSIONS], regrow = new int[DIMENSIONS][DIMENSIONS], limit = new int[DIMENSIONS][DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                flowers[i][j] = inputReader.readInt();
            }
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                limit[i][j] = inputReader.readInt();
            }
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                regrow[i][j] = inputReader.readInt();
            }
        }
        final int[][][] fences = new int[fenceCount][2][2];
        for (int i = 0; i < fenceCount; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    fences[i][j][k] = inputReader.readInt() - 1;
                }
            }
        }
        final AntColony antColony = new AntColony(flowers, regrow, limit, DIMENSIONS, fences, startX, startY, penalty);
        System.out.println(Arrays.stream(antColony.findBestTour()).map(Movement::toString).collect(Collectors.joining(
                "\n")) + "\nEXIT");
    }
}

class AntColony {
    public static final int MAXIMUM_ANT_AGE = 100;
    private final int[][] flowers, regrow, limit, originalLimits, originalFlowers;
    private final double[][] pheromone;
    private final int DIMENSIONS;
    private final int[][][] fences;
    private final int startX;
    private final int startY;
    private final int penalty;
    private final Movement[][] MOVEMENTS;
    private final long startTime = System.currentTimeMillis();
    private final int neighbours[][][][];
    private static final int ANT_COLONY_SIZE = 1000;
    private static final int TIME_OUT = 1000;
    private static final double evaporationCoefficient = 0.1;
    private final long[][][] penalties;

    public AntColony(final int[][] originalFlowers,
                     final int[][] regrow,
                     final int[][] originalLimits,
                     final int DIMENSIONS,
                     final int[][][] fences,
                     final int startX,
                     final int startY,
                     final int penalty) {
        this.originalFlowers = originalFlowers;
        this.regrow = regrow;
        this.originalLimits = originalLimits;
        this.DIMENSIONS = DIMENSIONS;
        this.fences = fences;
        this.startX = startX;
        this.startY = startY;
        this.penalty = penalty;
        this.limit = new int[DIMENSIONS][DIMENSIONS];
        this.flowers = new int[DIMENSIONS][DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            System.arraycopy(this.originalLimits[i], 0, this.limit[i], 0, DIMENSIONS);
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            System.arraycopy(this.originalFlowers[i], 0, this.flowers[i], 0, DIMENSIONS);
        }
        this.MOVEMENTS = new Movement[DIMENSIONS][DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                MOVEMENTS[i][j] = new Movement(i, j);
            }
        }
        neighbours = new int[DIMENSIONS][DIMENSIONS][][];
        penalties = new long[DIMENSIONS][DIMENSIONS][];
        findAndSetNeighboursWithCost();
        System.out.println(Arrays.deepToString(neighbours));
        System.out.println(Arrays.deepToString(penalties));
        pheromone = new double[DIMENSIONS][DIMENSIONS];
    }

    public Movement[] findBestTour() {
        List<Movement> bestTour = Collections.emptyList();
        long bestReward = Integer.MIN_VALUE;
        for (int ant = 0; ant < ANT_COLONY_SIZE && System.currentTimeMillis() - startTime < TIME_OUT; ant++) {
            int x = startX, y = startY;
            final List<Movement> tour = new ArrayList<>(MAXIMUM_ANT_AGE);
            long reward = 0;
            final Movement[] possibleMoves = new Movement[9];
            for (int i = 0; i < DIMENSIONS; i++) {
                System.arraycopy(this.originalLimits[i], 0, this.limit[i], 0, DIMENSIONS);
            }
            for (int i = 0; i < DIMENSIONS; i++) {
                System.arraycopy(this.originalFlowers[i], 0, this.flowers[i], 0, DIMENSIONS);
            }
            final int touched[][] = new int[DIMENSIONS][DIMENSIONS];
            for (int age = 0; age < MAXIMUM_ANT_AGE; age++) {
                int options = 0;
                for (int cell = 0; cell < neighbours[x][y].length; cell++) {
                    if (limit[neighbours[x][y][0][cell]][neighbours[x][y][1][cell]] != 0) {
                        if (age - touched[x][y] >= regrow[x][y]) {
                            flowers[x][y] = originalFlowers[x][y];
                        }
                        possibleMoves[options++] = MOVEMENTS[neighbours[x][y][0][cell]][neighbours[x][y][1][cell]];
                    }
                }
                if (options == 0) {
                    break;
                } else {
                    final int moveIndex = findMove(possibleMoves, options);
                    Movement move = possibleMoves[moveIndex];
                    tour.add(move);
                    reward = reward + flowers[move.x][move.y] - penalties[x][y][moveIndex];
                    x = move.x;
                    y = move.y;
                    limit[x][y]--;
                    flowers[x][y] = 0;
                    touched[x][y] = age;
                }
            }
            if (bestReward < reward) {
                bestTour = tour;
                bestReward = reward;
            }
        }
        return bestTour.toArray(new Movement[bestTour.size()]);
    }

    private final Random random = new Random();

    private int findMove(final Movement[] possibleMoves, final int options) {
        return random.nextInt(options);
    }

    private void findAndSetNeighboursWithCost() {
        final int temp[][] = new int[2][9];
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                int nearby = 0;
                if (i != 0) {
                    if (j != 0) {
                        temp[0][nearby] = i - 1;
                        temp[1][nearby] = j - 1;
                        nearby++;
                    }
                    temp[0][nearby] = i - 1;
                    temp[1][nearby] = j;
                    nearby++;
                    if (j != DIMENSIONS - 1) {
                        temp[0][nearby] = i - 1;
                        temp[1][nearby] = j + 1;
                        nearby++;
                    }
                }
                if (j != 0) {
                    temp[0][nearby] = i;
                    temp[1][nearby] = j - 1;
                    nearby++;
                }
                temp[0][nearby] = i;
                temp[1][nearby] = j;
                nearby++;
                if (j != DIMENSIONS - 1) {
                    temp[0][nearby] = i;
                    temp[1][nearby] = j + 1;
                    nearby++;
                }
                if (i != DIMENSIONS - 1) {
                    if (j != 0) {
                        temp[0][nearby] = i + 1;
                        temp[1][nearby] = j - 1;
                        nearby++;
                    }
                    temp[0][nearby] = i + 1;
                    temp[1][nearby] = j;
                    nearby++;
                    if (j != DIMENSIONS - 1) {
                        temp[0][nearby] = i + 1;
                        temp[1][nearby] = j + 1;
                        nearby++;
                    }
                }
                neighbours[i][j] = new int[2][nearby];
                penalties[i][j] = new long[nearby];
                System.arraycopy(temp[0], 0, neighbours[i][j][0], 0, nearby);
                System.arraycopy(temp[1], 0, neighbours[i][j][1], 0, nearby);
            }
        }
        for (final int[][] fence : fences) {
            if (fence[0][0] > 0) {
                final int x1 = fence[0][0] - 1;
                final int start = fence[0][1] == 0 ? 0 : fence[0][1] - 1;
                final int end = fence[1][1] == DIMENSIONS - 1 ? fence[1][1] : fence[1][1] + 1;
                for (int y = start; y <= end; y++) {
                    for (int j = 0; j < neighbours[x1][y][0].length; j++) {
                        if (neighbours[x1][y][0][j] == fence[0][0]) {
                            penalties[x1][y][j] = penalty;
                        }
                    }
                }
            }
            if (fence[1][0] + 1 < DIMENSIONS) {
                final int x2 = fence[1][0] + 1;
                final int start = fence[0][1] == 0 ? 0 : fence[0][1] - 1;
                final int end = fence[1][1] == DIMENSIONS - 1 ? fence[1][1] : fence[1][1] + 1;
                for (int y = start; y <= end; y++) {
                    for (int j = 0; j < neighbours[x2][y][0].length; j++) {
                        if (neighbours[x2][y][0][j] == fence[1][0]) {
                            penalties[x2][y][j] = penalty;
                        }
                    }
                }
            }
            if (fence[0][1] > 0) {
                final int y1 = fence[0][1] - 1;
                final int start = fence[0][0];
                final int end = fence[1][0];
                for (int x = start; x <= end; x++) {
                    for (int j = 0; j < 3; j++) {
                        if (neighbours[x][y1][1][j] == fence[0][1]) {
                            penalties[x][y1][j] = penalty;
                        }
                    }
                }
            }
            if (fence[1][1] + 1 < DIMENSIONS) {
                final int y2 = fence[1][1] + 1;
                final int start = fence[0][0];
                final int end = fence[1][0];
                for (int x = start; x <= end; x++) {
                    for (int j = neighbours[x][y2][0].length - 3; j < neighbours[x][y2][0].length; j++) {
                        if (neighbours[x][y2][1][j] == fence[1][1]) {
                            penalties[x][y2][j] = penalty;
                        }
                    }
                }
            }
        }
    }
}

class Movement {
    final int x, y;

    public Movement(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "MOVE " + (x + 1) + " " + (y + 1);
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