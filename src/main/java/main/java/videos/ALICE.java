package main.java.videos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    public static final int MAXIMUM_ANT_AGE = 7;
    private final int[][] regrow, originalLimits, originalFlowers;
    private final double[][] pheromone;
    private final int DIMENSIONS;
    private final Fence[] fences;
    private final int startX;
    private final int startY;
    private final int penalty;
    private final Movement[][] MOVEMENTS;
    private final long startTime = System.currentTimeMillis();
    private final int neighbours[][][][];
    private static final int ANT_COLONY_SIZE = 5;
    private static final int TIME_OUT = 1000;
    private static final double evaporationCoefficient = 0.1;

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
        this.fences = new Fence[fences.length];
        for (int i = 0; i < fences.length; i++) {
            this.fences[i] = new Fence(fences[i][0][0], fences[i][0][1], fences[i][1][0], fences[i][1][1]);
        }
        Arrays.sort(this.fences);
        this.startX = startX;
        this.startY = startY;
        this.penalty = penalty;
        this.MOVEMENTS = new Movement[DIMENSIONS][DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                MOVEMENTS[i][j] = new Movement(i, j);
            }
        }
        neighbours = new int[DIMENSIONS][DIMENSIONS][][];
        findAndSetNeighbours();
        pheromone = new double[DIMENSIONS][DIMENSIONS];
    }

    public Movement[] findBestTour() {
        List<Movement> bestTour = Collections.emptyList();
        long bestReward = 0;
        for (int ant = 0; ant < ANT_COLONY_SIZE && System.currentTimeMillis() - startTime < TIME_OUT; ant++) {
            final int limit[][] = new int[DIMENSIONS][DIMENSIONS];
            final int flowers[][] = new int[DIMENSIONS][DIMENSIONS];
            int x = startX, y = startY;
            final List<Movement> tour = new ArrayList<>(MAXIMUM_ANT_AGE);
            final Movement[] possibleMoves = new Movement[9];
            for (int i = 0; i < DIMENSIONS; i++) {
                System.arraycopy(this.originalLimits[i], 0, limit[i], 0, DIMENSIONS);
            }
            for (int i = 0; i < DIMENSIONS; i++) {
                System.arraycopy(this.originalFlowers[i], 0, flowers[i], 0, DIMENSIONS);
            }
            final int touched[][] = new int[DIMENSIONS][DIMENSIONS];
            long reward = flowers[startX][startY];
            flowers[startX][startY] = 0;
            touched[startX][startY] = 0;
            limit[startX][startY]--;
            for (int age = 0; age < MAXIMUM_ANT_AGE; age++) {
                int options = 0;
                for (int cell = 0; cell < neighbours[x][y][0].length; cell++) {
                    final int xToVisit = neighbours[x][y][0][cell], yToVisit = neighbours[x][y][1][cell];
                    if (limit[xToVisit][yToVisit] != 0) {
                        if (age - touched[xToVisit][yToVisit] >= regrow[xToVisit][yToVisit]) {
                            flowers[xToVisit][yToVisit] = originalFlowers[xToVisit][yToVisit];
                        }
                        possibleMoves[options++] = MOVEMENTS[xToVisit][yToVisit];
                    }
                }
                if (options == 0) {
                    break;
                } else {
                    final Movement move = findMove(flowers, possibleMoves, options, x, y);
                    tour.add(move);
                    reward = reward + flowers[move.x][move.y] - findPenalty(x, y, move.x, move.y);
                    x = move.x;
                    y = move.y;
                    limit[x][y]--;
                    flowers[x][y] = 0;
                    touched[x][y] = age;
                }
            }
            for (final Movement movement : tour) {
                pheromone[movement.x][movement.y] +=
                        (double) reward * flowers[movement.x][movement.y] / (DIMENSIONS * DIMENSIONS);
            }
            for (int i = 0; i < DIMENSIONS; i++) {
                for (int j = 0; j < DIMENSIONS; j++) {
                    pheromone[i][j] *= (1 - evaporationCoefficient);
                }
            }
            if (bestReward < reward) {
                bestTour = tour;
                bestReward = reward;
            }
        }
        return bestTour.toArray(new Movement[bestTour.size()]);
    }

    private long findPenalty(final int startX, final int startY, final int endX, final int endY) {
        int index = Arrays.binarySearch(fences, new Fence(endX, endY, endX, endY));
        if (index < 0) {
            index = -(index + 1);
        }
        for (int i = index; i < fences.length && startX >= fences[i].x1; i++) {
            final Fence fence = fences[i];
            if (isInside(endX, endY, fence)) {
                if (!isInside(startX, startY, fence)) {
                    return penalty;
                }
            }
        }
        return 0;
    }

    private boolean isInside(final int x, final int y, final Fence fence) {
        return fence.x1 <= x && x <= fence.x2 && fence.y1 <= y && y <= fence.y2;
    }

    private Movement findMove(final int[][] flowers,
                              final Movement[] possibleMoves,
                              final int options,
                              final int x, final int y) {
        double totalScore = 0;
        final double[] score = new double[options];
        for (int i = 0; i < options; i++) {
            final Movement move = possibleMoves[i];
            score[i] = Math.pow(flowers[move.x][move.y] - findPenalty(x, y, move.x, move.y), 0.8);
            //* (1 + Math.pow(pheromone[move.x][move.y], 1.2));
            totalScore = totalScore + score[i];
        }
        double roulette = Math.random();
        for (int i = 0; i < options; i++) {
            score[i] /= totalScore;
            if (score[i] > roulette) {
                return possibleMoves[i];
            } else {
                roulette -= score[i];
            }
        }
        return possibleMoves[0];
    }

    private void findAndSetNeighbours() {
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
                System.arraycopy(temp[0], 0, neighbours[i][j][0], 0, nearby);
                System.arraycopy(temp[1], 0, neighbours[i][j][1], 0, nearby);
            }
        }
    }
}

class Fence implements Comparable<Fence> {
    final int x1, y1, x2, y2;

    public Fence(final int x1, final int y1, final int x2, final int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public int compareTo(final Fence other) {
        final int upperDiff = x1 - other.x1;
        if (upperDiff != 0) {
            return upperDiff;
        }
        final int lowerDiff = x2 - other.x2;
        if (lowerDiff != 0) {
            return lowerDiff;
        }
        return y1 != other.y1 ? y1 - other.y2 : y2 - other.y2;
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

    public boolean isSpaceChar(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
    }
}