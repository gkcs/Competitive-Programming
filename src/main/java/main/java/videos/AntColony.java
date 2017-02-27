package main.java.videos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <a href="https://en.wikipedia.org/wiki/Ant_colony_optimization_algorithms">Ant Colony Optimisation</a> is used to
 * solve the NP problem for <a href="https://www.codechef.com/FEB17/problems/ALICGARD">CodeChef February Long
 * Challenge</a>.
 * <br/>
 * <br/>
 * Ant Colony is a meta-heuristic technique used to solve problems which have no clear + efficient algorthm to solve
 * them. Hence, we compromise on efficiency to get results in a reasonable amount of time. The ants are units of
 * computation which report the best solution when the total allotted time is exceeded.
 * <br/>
 * <br/>
 * An ant behaves like a thinking unit, with two parts of its brain: The logical and the social.
 * <br/>
 * 1) The logical brain evaluates the current position, using some heuristic, and gives more preference to high scores.
 * <br/>
 * 2) The social brain gives more weight to the amount of pheromone deposited by previous ants. This makes sense
 * because ants deposit pheromone in proportion to their reward.
 *
 * The two parts of the brain then combine to gives weights to all possible paths for the ant. A roulette function
 * is then used to decide which path will be chosen.
 */
public class AntColony {
    public static final double LOG_2 = Math.log(2);
    public double ALPHA = 0.01;
    public double BETA = 1.133;
    private int MAXIMUM_ANT_AGE;
    private final int[][] regrow, originalLimits, originalFlowers;
    private final double[][][] pheromone;
    private final int DIMENSIONS;
    private final Fence[] fences;
    private final int startX;
    private final int startY;
    private final int penalty;
    private final Movement[][] MOVEMENTS;
    private long startTime = System.currentTimeMillis();
    private final int neighbours[][][][];
    private int TIME_OUT = 1000;
    private double evaporationCoefficient = 0.184;

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
        pheromone = new double[DIMENSIONS][DIMENSIONS][];
        findAndSetNeighbours();
        MAXIMUM_ANT_AGE = 576240;
    }

    public Tour bruteForceTour() {
        originalLimits[startX][startY]--;
        originalFlowers[startX][startY] = 0;
        return bruteForceTour(startX, startY, originalFlowers, new int[DIMENSIONS][DIMENSIONS], 0);
    }

    public Tour bruteForceTour(final int x,
                               final int y,
                               final int[][] flowersNow,
                               final int[][] touchedNow,
                               final int depth) {
        Tour bestTour = new Tour(new ArrayList<>(), 0);
        if (depth == 6) {
            return new Tour(new ArrayList<>(), 0);
        }
        final int[][] flowers = new int[DIMENSIONS][DIMENSIONS],
                touched = new int[DIMENSIONS][DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            System.arraycopy(flowersNow[i], 0, flowers[i], 0, DIMENSIONS);
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            System.arraycopy(touchedNow[i], 0, touched[i], 0, DIMENSIONS);
        }
        long bestReward = 0;
        for (int cell = 0; cell < neighbours[x][y][0].length; cell++) {
            final int xToVisit = neighbours[x][y][0][cell], yToVisit = neighbours[x][y][1][cell];
            if (originalLimits[xToVisit][yToVisit] != 0) {
                if (depth - touched[xToVisit][yToVisit] >= regrow[xToVisit][yToVisit]) {
                    flowers[xToVisit][yToVisit] = originalFlowers[xToVisit][yToVisit];
                }
                flowers[xToVisit][yToVisit] = 0;
                touched[xToVisit][yToVisit] = depth;
                originalLimits[xToVisit][yToVisit]--;
                final Tour tour = bruteForceTour(xToVisit, yToVisit, flowersNow, touched, depth + 1);
                tour.reward = flowersNow[xToVisit][yToVisit] + tour.reward;
                if (tour.reward > bestReward) {
                    tour.moves.add(MOVEMENTS[x][y]);
                    bestTour = tour;
                }
                flowers[xToVisit][yToVisit] = flowersNow[xToVisit][yToVisit];
                touched[xToVisit][yToVisit] = touchedNow[xToVisit][yToVisit];
                originalLimits[xToVisit][yToVisit]++;
            }
        }
        return bestTour;
    }

    public Tour findBestTour() {
        Tour bestTour = new Tour(Collections.emptyList(), 0);
        long bestReward = 0;
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                for (int k = 0; k < pheromone[i][j].length; k++) {
                    pheromone[i][j][k] = 0;
                }
            }
        }
        while (System.currentTimeMillis() - startTime < TIME_OUT) {
            final int limit[][] = new int[DIMENSIONS][DIMENSIONS];
            final int flowers[][] = new int[DIMENSIONS][DIMENSIONS];
            int x = startX, y = startY;
            final Tour tour = new Tour(new ArrayList<>(MAXIMUM_ANT_AGE), 0);
            final Movement[] possibleMoves = new Movement[9];
            for (int i = 0; i < DIMENSIONS; i++) {
                System.arraycopy(this.originalLimits[i], 0, limit[i], 0, DIMENSIONS);
            }
            for (int i = 0; i < DIMENSIONS; i++) {
                System.arraycopy(this.originalFlowers[i], 0, flowers[i], 0, DIMENSIONS);
            }
            final int touched[][] = new int[DIMENSIONS][DIMENSIONS];
            tour.reward = flowers[startX][startY];
            flowers[startX][startY] = 0;
            touched[startX][startY] = 0;
            limit[startX][startY]--;
            int bestPoint = 0;
            long currentBestReward = 0;
            for (int age = 0; age < MAXIMUM_ANT_AGE; age++) {
                int options = 0;
                for (int cell = 0; cell < neighbours[x][y][0].length; cell++) {
                    final int xToVisit = neighbours[x][y][0][cell], yToVisit = neighbours[x][y][1][cell];
                    if (limit[xToVisit][yToVisit] != 0) {
                        if (age - touched[xToVisit][yToVisit] >= regrow[xToVisit][yToVisit]) {
                            flowers[xToVisit][yToVisit] = originalFlowers[xToVisit][yToVisit];
                        }
                        if (flowers[xToVisit][yToVisit] - findPenalty(x, y, xToVisit, yToVisit) > -tour.reward / 2) {
                            possibleMoves[options++] = MOVEMENTS[xToVisit][yToVisit];
                        }
                    }
                }
                if (options == 0) {
                    break;
                } else {
                    final Movement move = findMove(flowers, possibleMoves, options, x, y);
                    long payOff = flowers[move.x][move.y] - findPenalty(x, y, move.x, move.y);
                    tour.reward = tour.reward + payOff;
                    tour.moves.add(move);
                    if (tour.reward > currentBestReward) {
                        bestPoint = tour.moves.size();
                        currentBestReward = tour.reward;
                    }
                    x = move.x;
                    y = move.y;
                    limit[x][y]--;
                    flowers[x][y] = 0;
                    touched[x][y] = age;
                }
            }
            for (int i = 0; i < DIMENSIONS; i++) {
                for (int j = 0; j < DIMENSIONS; j++) {
                    for (int k = 0; k < pheromone[i][j].length; k++) {
                        pheromone[i][j][k] *= 1 - evaporationCoefficient;
                    }
                }
            }
            Movement previousMove = MOVEMENTS[startX][startY];
            if (bestReward < currentBestReward) {
                tour.moves = tour.moves.subList(0, bestPoint);
                tour.reward = currentBestReward;
                bestTour = tour;
            }
            for (final Movement movement : tour.moves) {
                pheromone[previousMove.x][previousMove.y][getMoveIndex(previousMove.x, previousMove.y, movement)] +=
                        Math.log((1 + tour.reward * flowers[movement.x][movement.y] / ((double) DIMENSIONS * DIMENSIONS))) / LOG_2;
                previousMove = movement;
            }
        }
        return bestTour;
    }

    private int getMoveIndex(final int x, final int y, final Movement movement) {
        for (int i = 0; i < neighbours[x][y][0].length; i++) {
            if (neighbours[x][y][0][i] == movement.x && neighbours[x][y][1][i] == movement.y) {
                return i;
            }
        }
        throw new RuntimeException("Move doesn't exist! " + movement.toString());
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
            score[i] = Math.pow(penalty + flowers[move.x][move.y] - findPenalty(x, y, move.x, move.y), ALPHA)
                    * (1 + Math.pow(pheromone[x][y][getMoveIndex(x, y, move)], BETA));
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
                pheromone[i][j] = new double[nearby];
                System.arraycopy(temp[0], 0, neighbours[i][j][0], 0, nearby);
                System.arraycopy(temp[1], 0, neighbours[i][j][1], 0, nearby);
            }
        }
    }

    public void setALPHA(final double ALPHA) {
        this.ALPHA = ALPHA;
    }

    public void setBETA(final double BETA) {
        this.BETA = BETA;
    }

    public void setTIME_OUT(final int TIME_OUT) {
        this.TIME_OUT = TIME_OUT;
    }

    public void setEvaporationCoefficient(final double evaporationCoefficient) {
        this.evaporationCoefficient = evaporationCoefficient;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public void setMAXIMUM_ANT_AGE(final int MAXIMUM_ANT_AGE) {
        this.MAXIMUM_ANT_AGE = MAXIMUM_ANT_AGE;
    }

    public static class Tour {
        List<Movement> moves;
        long reward;

        Tour(final List<Movement> moves, final long reward) {
            this.moves = moves;
            this.reward = reward;
        }

        public long getReward() {
            return reward;
        }
    }

    public static class Movement {
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

    public static class Fence implements Comparable<Fence> {
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
}
