package main.java;

import java.util.Scanner;

class Player {

    private static final String THROW = "THROW";
    private static final String MOVE = "MOVE";
    private static final String WIZARD = "WIZARD";
    private static final String OPPONENT_WIZARD = "OPPONENT_WIZARD";
    private static final String SNAFFLE = "SNAFFLE";
    private static final String BLUDGER = "BLUDGER";

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        final int myTeamId = in.nextInt();
        final int goalCenter[] = new int[]{myTeamId == 1 ? 0 : 16000, 3750};
        int magic = 100;
        final int bludgers[][] = new int[2][];
        while (true) {
            final int ballPositions[][] = new int[7][2];
            final int playerPositions[][] = new int[7][3];
            final int entity[][] = new int[in.nextInt()][7];
            int bludgerCount = 0;
            int myPlayers = 0;
            int snaffles = 0;
            for (int i = 0; i < entity.length; i++) {
                entity[i][0] = in.nextInt();
                entity[i][1] = getEntityType(in.next());
                for (int j = 0; j < 5; j++) {
                    entity[i][j + 2] = in.nextInt();
                }
                if (entity[i][1] == 2) {
                    ballPositions[snaffles][0] = entity[i][2];
                    ballPositions[snaffles][1] = entity[i][3];
                    snaffles++;
                } else if (entity[i][1] == 3) {
                    bludgers[bludgerCount][0] = entity[i][2];
                    bludgers[bludgerCount][1] = entity[i][3];
                    bludgerCount++;
                } else if (entity[i][1] == 0) {
                    playerPositions[myPlayers][0] = entity[i][2];
                    playerPositions[myPlayers][1] = entity[i][3];
                    playerPositions[myPlayers][2] = entity[i][6];
                    myPlayers++;
                }
            }
            int playerActions[][] = new int[myPlayers][5];
            for (int i = 0; i < myPlayers; i++) {
                if (playerPositions[i][2] == 1) {
                    playerActions[i] = new int[]{1, goalCenter[0], goalCenter[1], 500};
                } else {
                    final int nearestEntity = getNearestEntity(ballPositions, playerPositions[i], snaffles);
                    playerActions[i] = new int[]{0, ballPositions[nearestEntity][0], ballPositions[nearestEntity][1], 150, nearestEntity};
                }
            }
            if (playerActions[0][0] == playerActions[1][0] && playerActions[0][0] == 0 && playerActions[1][4] == playerActions[0][4]) {
                final int nearestEntity = getNearestEntity(playerPositions,
                                                           ballPositions[playerActions[0][4]],
                                                           myPlayers);
                if (playerPositions[0][0] == playerPositions[nearestEntity][0]
                        && playerPositions[0][1] == playerPositions[nearestEntity][1]) {
                    final int nearest = getNearestEntity(update(ballPositions, playerActions[1][4], snaffles - 1),
                                                         playerPositions[1],
                                                         snaffles - 1);
                    playerActions[1] = new int[]{0, ballPositions[nearest][0], ballPositions[nearest][1], 150, nearest};
                } else {
                    final int nearest = getNearestEntity(update(ballPositions, playerActions[1][4], snaffles - 1),
                                                         playerPositions[0],
                                                         snaffles - 1);
                    playerActions[0] = new int[]{0, ballPositions[nearest][0], ballPositions[nearest][1], 150, nearest};
                }
            }
            final StringBuilder stringBuilder = new StringBuilder();
            for (final int[] playerAction : playerActions) {
                stringBuilder.append(playerAction[0] == 0 ? MOVE : THROW)
                        .append(' ')
                        .append(playerAction[1])
                        .append(' ')
                        .append(playerAction[2])
                        .append(' ')
                        .append(playerAction[3])
                        .append('\n');
            }
            System.out.println(stringBuilder.deleteCharAt(stringBuilder.length() - 1));
            magic++;
        }
    }

    private static int[][] update(int[][] ballPositions, int tabooIndex, int size) {
        final int ballPos[][] = new int[size][2];
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (i != tabooIndex) {
                System.arraycopy(ballPositions[i], 0, ballPos[count], 0, ballPositions[i].length);
                count++;
            }
        }
        return ballPos;
    }

    private static int getNearestEntity(int[][] entity, int[] seeker, final int entities) {
        double min = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < entities; i++) {
            final double distance = Math.sqrt(square(entity[i][0] - seeker[0])
                                                      + square(entity[i][1] - seeker[1]));
            if (min > distance) {
                min = distance;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static long square(long number) {
        return number * number;
    }

    private static int getEntityType(String s) {
        switch (s) {
            case WIZARD:
                return 0;
            case OPPONENT_WIZARD:
                return 1;
            case SNAFFLE:
                return 2;
            case BLUDGER:
                return 3;
        }
        return 4;
    }
}