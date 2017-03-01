package main.java.codingame.ghost_in_a_cell;

import java.util.*;
import java.util.stream.Collectors;

public class GhostInACell {

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        final int factoryCount = in.nextInt();
        final int linkCount = in.nextInt();
        final int distances[][] = new int[factoryCount][factoryCount];
        for (int i = 0; i < factoryCount; i++) {
            for (int j = 0; j < factoryCount; j++) {
                distances[i][j] = distances[i][j] = Board.MAX_TURNS + 1;
            }
        }
        int turn = 0;
        for (int i = 0; i < linkCount; i++) {
            final int factory1 = in.nextInt();
            final int factory2 = in.nextInt();
            final int distance = in.nextInt();
            distances[factory1][factory2] = distances[factory2][factory1] = distance;
        }
        while (turn <= 400) {
            final int entityCount = in.nextInt();
            final List<Troop> troops = new ArrayList<>();
            final List<Factory> factories = new ArrayList<>();
            final List<Bomb> bombs = new ArrayList<>();
            for (int i = 0; i < entityCount; i++) {
                final int entityId = in.nextInt();
                final String entityType = in.next();
                final int arg1 = in.nextInt();
                final int arg2 = in.nextInt();
                final int arg3 = in.nextInt();
                final int arg4 = in.nextInt();
                final int arg5 = in.nextInt();
                switch (entityType) {
                    case "FACTORY":
                        factories.add(new Factory(entityId, arg1, arg2, arg3, distances[entityId]));
                        break;
                    case "TROOP":
                        troops.add(new Troop(entityId, arg1, arg5, arg2, arg3, arg4));
                        break;
                    case "BOMB":
                        bombs.add(new Bomb(entityId, arg1, arg2, arg3, arg4));
                        break;
                }
            }
            final Board board = new Board(factories, troops, bombs, distances);
            System.out.println(board.makeMove());
            turn++;
        }
    }
}

class Board {
    private final List<Troop> troops;
    private final List<Bomb> bombs;
    private final List<Factory> factories;
    private final int distances[][];
    public static final int MAX_TURNS = 400;

    public Board(final List<Factory> factories,
                 final List<Troop> troops,
                 final List<Bomb> bombs,
                 final int[][] distances) {
        this.factories = factories;
        this.troops = troops;
        this.bombs = bombs;
        this.distances = distances;
    }

    public String makeMove() {
        final List<Factory> opponents = factories.stream().filter(c -> c.player != 1).collect(Collectors.toList()),
                sources = factories.stream().filter(factory -> factory.player == 1).collect(Collectors.toList());
        final StringBuilder sb = new StringBuilder();
        for (final Factory source : sources) {
            final List<Factory> poachableFactories = findPoachableFactories(source, opponents, distances[source.id]);
            poachableFactories.sort(Comparator.comparingInt(o -> distances[source.id][o.id]));
            if (!poachableFactories.isEmpty()) {
                final Factory destination = poachableFactories.get(0);
                sb.append("MOVE ")
                        .append(source.id)
                        .append(' ')
                        .append(destination.id)
                        .append(' ')
                        .append(destination.cyborgs + destination.production * distances[source.id][destination.id] + 1)
                        .append(';');
            }
        }
        return sb.length() == 0 ? "WAIT" : sb.substring(0, sb.length() - 1);
    }

    private List<Factory> findPoachableFactories(final Factory source,
                                                 final List<Factory> opponentFactories,
                                                 final int[] distances) {
        final List<Factory> factoryList = new ArrayList<>();
        for (final Factory factory : opponentFactories) {
            if (factory.cyborgs +
                    (factory.player == 0 ? 0 : 1) * factory.production * distances[factory.id] < source.cyborgs / 2) {
                factoryList.add(factory);
            }
        }
        return factoryList;
    }
}

class Entity {
    final int id;

    Entity(final int id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && id == ((Entity) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}

class Factory extends Entity {
    int player, cyborgs;
    final int production;
    final int[] distances;
    final int sumOfDistances;

    public Factory(final int entityId,
                   final int player,
                   final int cyborgs,
                   final int production,
                   final int[] distances) {
        super(entityId);
        this.player = player;
        this.cyborgs = cyborgs;
        this.production = production;
        this.distances = distances;
        sumOfDistances = Arrays.stream(distances).map(distance -> Board.MAX_TURNS / distance).sum();
    }

    public int utility() {
        return sumOfDistances * production;
    }

    public int[][] plotHistogram(final List<Troop> troops, int remainingTurns) {
        final int[][] histogram = new int[3][remainingTurns];
        for (final Troop troop : troops) {
            if (troop.destination == id && troop.timeToDestination < remainingTurns) {
                if (troop.player == 1) {
                    histogram[1][troop.timeToDestination] += troop.size;
                } else {
                    histogram[2][troop.timeToDestination] += troop.size;
                }
            }
        }
        histogram[0][player] = cyborgs;
        int currentPlayer = player;
        for (int i = 1; i < remainingTurns; i++) {
            histogram[0][i] = histogram[0][i - 1];
            histogram[1][i] = histogram[1][i - 1];
            histogram[2][i] = histogram[2][i - 1];
            if (currentPlayer == 1) {
                histogram[1][i] += production;
            } else if (currentPlayer == -1) {
                histogram[2][i] += production;
            }
            if (histogram[1][i] > histogram[2][i]) {
                histogram[1][i] = histogram[1][i] - histogram[2][i];
                histogram[2][i] = 0;
            } else if (histogram[1][i] < histogram[2][i]) {
                histogram[2][i] = histogram[2][i] - histogram[1][i];
                histogram[1][i] = 0;
            } else {
                histogram[2][i] = histogram[1][i] = 0;
            }
            if (histogram[2][i] > histogram[0][i]) {
                histogram[2][i] = histogram[2][i] - histogram[0][i];
                histogram[0][i] = 0;
                currentPlayer = -1;
            } else if (histogram[1][i] > histogram[0][i]) {
                histogram[1][i] = histogram[1][i] - histogram[0][i];
                histogram[0][i] = 0;
                currentPlayer = 1;
            }
        }
        return histogram;
    }

    public Factory findNearestFriendly(final List<Factory> factories) {
        return factories.stream()
                .filter(factory -> factory.player == player)
                .min(Comparator.comparingInt(factory -> distances[factory.id]))
                .orElse(null);
    }

    public Factory findNearestEnemy(final List<Factory> factories) {
        return factories.stream()
                .filter(factory -> factory.player == -player)
                .min(Comparator.comparingInt(factory -> distances[factory.id]))
                .orElse(null);
    }

    public Factory findNearestNeutral(final List<Factory> factories) {
        return factories.stream()
                .filter(factory -> factory.player == 0)
                .min(Comparator.comparingInt(factory -> distances[factory.id]))
                .orElse(null);
    }

    public Troop abandon() {
        final int destination = 0;
        final Troop troop = new Troop(Troop.troops, player, distances[destination], id, destination, cyborgs);
        cyborgs = 0;
        return troop;
    }
}

class Bomb extends Entity {
    final int player, source, destination;
    int timeToExplode;

    public Bomb(final int entityId,
                final int player,
                final int source,
                final int destination,
                final int timeToExplode) {
        super(entityId);
        this.player = player;
        this.source = source;
        this.destination = destination;
        this.timeToExplode = timeToExplode;
    }

    public Factory explode(final Factory factory) {
        if (destination != -1 && factory.id != destination) {
            throw new RuntimeException("Wrong place to blow!");
        } else if (timeToExplode == 0) {
            throw new RuntimeException("It shouldn't blow now...");
        } else if (factory.cyborgs > 20) {
            factory.cyborgs /= 2;
        } else {
            factory.cyborgs = factory.cyborgs > 10 ? factory.cyborgs - 10 : 0;
        }
        return factory;
    }

    public void decrementTime() {
        --timeToExplode;
    }
}

class Troop extends Entity {
    static int troops = 0;
    int timeToDestination;
    final int size, player, source, destination;

    public Troop(final int entityId,
                 final int player,
                 final int timeToDestination,
                 final int source,
                 final int destination,
                 final int size) {
        super(entityId);
        this.size = size;
        this.player = player;
        this.timeToDestination = timeToDestination;
        this.source = source;
        this.destination = destination;
    }

    public Factory crash(final Factory factory) throws Throwable {
        if (factory.id != destination) {
            throw new RuntimeException("Wrong place to fight!");
        } else if (timeToDestination == 0) {
            if (size <= factory.cyborgs) {
                factory.cyborgs -= size;
            } else {
                factory.cyborgs = size - factory.cyborgs;
                factory.player = player;
            }
        } else {
            throw new RuntimeException("You need to fight later...");
        }
        return factory;
    }

    public void decrementTime() {
        --timeToDestination;
    }
}