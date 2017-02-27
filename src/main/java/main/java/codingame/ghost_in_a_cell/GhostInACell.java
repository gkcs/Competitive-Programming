package main.java.codingame.ghost_in_a_cell;

import java.util.*;
import java.util.stream.Collectors;

public class GhostInACell {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        final int factoryCount = in.nextInt(); // the number of factories
        final int linkCount = in.nextInt(); // the number of links between factories
        Factory.distances = new int[factoryCount][factoryCount];
        for (int i = 0; i < factoryCount; i++) {
            for (int j = 0; j < factoryCount; j++) {
                Factory.distances[i][j] = Factory.distances[i][j] = Integer.MAX_VALUE;
            }
        }
        for (int i = 0; i < linkCount; i++) {
            final int factory1 = in.nextInt();
            final int factory2 = in.nextInt();
            final int distance = in.nextInt();
            Factory.distances[factory1][factory2] = Factory.distances[factory2][factory1] = distance;
        }
        // game loop
        while (true) {
            final int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            final List<Troop> troops = new ArrayList<>();
            final List<Factory> factories = new ArrayList<>();
            for (int i = 0; i < entityCount; i++) {
                final int entityId = in.nextInt();
                final String entityType = in.next();
                final int arg1 = in.nextInt();
                final int arg2 = in.nextInt();
                final int arg3 = in.nextInt();
                final int arg4 = in.nextInt();
                final int arg5 = in.nextInt();
                if (entityType.equals("FACTORY")) {
                    factories.add(new Factory(entityId, arg1, arg2, arg3));
                } else {
                    troops.add(new Troop(entityId, arg1, arg5, arg2, arg3, arg4));
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            final Board board = new Board(factories, troops);
            // Any valid action, such as "WAIT" or "MOVE source destination cyborgs"
            System.out.println(board.makeMove());
        }
    }
}

class Board {
    private final List<Troop> troops;
    private final List<Factory> factories;

    public Board(final List<Factory> factories, final List<Troop> troops) {
        this.factories = factories;
        this.troops = troops;
    }

    public String makeMove() {
        final List<Factory> opponents = factories.stream().filter(c -> c.player != 1).collect(Collectors.toList()),
                sources = factories.stream().filter(factory -> factory.player == 1).collect(Collectors.toList());
        for (final Factory source : sources) {
            final List<Factory> poachableFactories = source.findPoachableFactories(opponents);
            poachableFactories.sort(Comparator.comparingInt(o -> Factory.distances[source.id][o.id]));
            if (!poachableFactories.isEmpty()) {
                final Factory destination = poachableFactories.get(0);
                return "MOVE " + source.id + " " + destination.id + " "
                        + (destination.cyborgs + destination.production * Factory.distances[source.id][destination.id] + 1);
            }
        }
        return "WAIT";
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
    final int player, cyborgs, production;
    static int distances[][];

    public Factory(final int entityId, final int player, final int cyborgs, final int production) {
        super(entityId);
        this.player = player;
        this.cyborgs = cyborgs;
        this.production = production;
    }

    public List<Factory> findPoachableFactories(final List<Factory> opponentFactories) {
        final List<Factory> factoryList = new ArrayList<>();
        for (final Factory factory : opponentFactories) {
            if (factory.cyborgs + factory.production * Factory.distances[this.id][factory.id] < this.cyborgs) {
                factoryList.add(factory);
            }
        }
        return factoryList;
    }
}

class Troop extends Entity {
    final int size, player, timeToDestination;
    final Factory source, destination;

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
        this.source = null;
        this.destination = null;
    }
}