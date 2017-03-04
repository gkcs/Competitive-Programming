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
        final Map<Integer, Integer> factoryIndexes = new HashMap<>();
        final Map<Integer, Integer> troopIndexes = new HashMap<>();
        final Map<Integer, Integer> bombIndexes = new HashMap<>();
        for (int i = 0; i < linkCount; i++) {
            final int factory1 = in.nextInt();
            final int factory2 = in.nextInt();
            if (!factoryIndexes.containsKey(factory1)) {
                factoryIndexes.put(factory1, factoryIndexes.size());
            }
            if (!factoryIndexes.containsKey(factory2)) {
                factoryIndexes.put(factory2, factoryIndexes.size());
            }
            final int distance = in.nextInt();
            distances[factory1][factory2] = distances[factory2][factory1] = distance;
        }
        final List<Bomb> bombs = new ArrayList<>();
        while (turn <= 400) {
            final int entityCount = in.nextInt();
            final List<Troop> troops = new ArrayList<>();
            final List<Factory> factories = new ArrayList<>();
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                final String entityType = in.next();
                final int arg1 = in.nextInt();
                final int arg2 = in.nextInt();
                final int arg3 = in.nextInt();
                final int arg4 = in.nextInt();
                final int arg5 = in.nextInt();
                switch (entityType) {
                    case "FACTORY":
                        entityId = factoryIndexes.get(entityId);
                        factories.add(new Factory(entityId, arg1, arg2, arg3, arg4, distances[entityId]));
                        break;
                    case "TROOP":
                        troopIndexes.putIfAbsent(entityId, troopIndexes.size());
                        entityId = troopIndexes.get(entityId);
                        troops.add(new Troop(entityId, arg1, arg2, arg3, arg4, arg5));
                        break;
                    case "BOMB":
                        bombIndexes.putIfAbsent(entityId, bombIndexes.size());
                        entityId = bombIndexes.get(entityId);
                        final Bomb previousVersion = bombs.stream()
                                .filter(bomb -> bomb.timeToExplode == arg4 + 1)
                                .filter(bomb -> bomb.source == arg2)
                                .findAny()
                                .orElse(null);
                        if (previousVersion == null) {
                            bombs.add(new Bomb(entityId, arg1, arg2, arg3, arg4));
                        } else {
                            previousVersion.decrementTime();
                        }
                        break;
                }
            }
            final Board board = new Board(factories, troops, bombs, distances, turn);
            final String bestMoves = board.findBestMoves(factoryIndexes);
            System.out.println(bestMoves.equals("") ? "WAIT" : bestMoves);
            turn++;
        }
    }
}

class Board {
    private final List<Troop> troops;
    private final List<Bomb> bombs;
    private final List<Factory> factories;
    private final List<Factory> myFactories;
    private final List<Factory> opponentFactories;
    private final List<Factory> neutrals;
    private final int[][] distances;
    private final int turn;
    private final int myProduction, opponentProduction, myArmy, opponentArmy;
    public static final int MAX_TURNS = 400;

    public Board(final List<Factory> factories,
                 final List<Troop> troops,
                 final List<Bomb> bombs,
                 final int[][] distances,
                 final int turn) {
        this.factories = factories;
        this.troops = troops;
        troops.addAll(bombs.stream().map(bomb -> bomb.convertToTroop(factories)).collect(Collectors.toList()));
        this.bombs = bombs;
        this.distances = distances;
        this.turn = turn;
        myFactories = factories.stream().filter(factory -> factory.player == 1).collect(Collectors.toList());
        opponentFactories = factories.stream().filter(factory -> factory.player == -1).collect(Collectors.toList());
        neutrals = factories.stream().filter(factory -> factory.player == 0).collect(Collectors.toList());
        opponentArmy = opponentFactories.stream().mapToInt(factory -> factory.cyborgs).sum()
                + troops.stream().filter(troop -> troop.player == -1).mapToInt(troop -> troop.size).sum();
        myArmy = myFactories.stream().mapToInt(factory -> factory.cyborgs).sum()
                + troops.stream().filter(troop -> troop.player == 1).mapToInt(troop -> troop.size).sum();
        myProduction = factories.stream().filter(factory -> factory.player == 1).mapToInt(factory -> factory.production).sum();
        opponentProduction = factories.stream().filter(factory -> factory.player == -1).mapToInt(factory -> factory.production).sum();
    }

    public String findBestMoves(final Map<Integer, Integer> factoryIndexes) {
        return play().stream()
                .map(move -> new Move(factoryIndexes.get(move.source),
                                      factoryIndexes.get(move.destination),
                                      move.troopSize))
                .map(Move::toString)
                .collect(Collectors.joining(";"));
    }

    private List<Move> play() {
        final List<Troop> movements = new ArrayList<>();
        final List<Factory> frontLine = new ArrayList<>();
        final List<Factory> suppliers = new ArrayList<>();
        for (final Factory source : myFactories) {
            final Factory nearestEnemy = source.findNearestEnemy(opponentFactories);
            final Factory target = nearestEnemy.findNearestEnemyWithConstraint(myFactories,
                                                                               source,
                                                                               source.distances[nearestEnemy.id]);
            if (target == null) {
                frontLine.add(source);
            } else {
                suppliers.add(source);
            }
        }
        suppliers.removeAll(frontLine);
        //move the ships to attack/defense. Then look for strategic movements to the frontLine

        for (final Factory source : suppliers) {
            final Factory nearestEnemy = source.findNearestEnemy(opponentFactories);
            final Factory target = nearestEnemy.findNearestEnemyWithConstraint(myFactories,
                                                                               source,
                                                                               source.distances[nearestEnemy.id]);
            if (target.getHistogram(troops, Board.MAX_TURNS - turn).owner[source.distances[target.id]] == 1) {
                movements.add(source.dispatchTroop(target, source.getSpareShips()));
            }
        }
        return movements.stream().map(troop -> new Move(troop.source,
                                                        troop.destination,
                                                        troop.size)).collect(Collectors.toList());
    }
}

class Move {
    final int source, destination, troopSize;

    public Move(final int source, final int destination, final int troopSize) {
        this.source = source;
        this.destination = destination;
        this.troopSize = troopSize;
    }

    @Override
    public String toString() {
        return "MOVE" + " " + source + " " + destination + " " + troopSize;
    }
}

abstract class Entity {
    final int id;
    final String type;

    public Entity(final int id, final String type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Entity entity = (Entity) o;
        return id == entity.id && type.equals(entity.type);
    }

    @Override
    public int hashCode() {
        return 31 * id + type.hashCode();
    }
}

class Factory extends Entity {
    int player, cyborgs;
    final int production;
    final int[] distances;
    final double sumOfDistances;
    final int starts;
    private Histogram histogram;

    public Factory(final int entityId,
                   final int player,
                   final int cyborgs,
                   final int production,
                   final int starts,
                   final int[] distances) {
        super(entityId, "FACTORY");
        this.player = player;
        this.cyborgs = cyborgs;
        this.production = production;
        this.distances = distances;
        this.starts = starts;
        sumOfDistances = Arrays.stream(distances)
                .mapToDouble(distance -> 1.0 / distance)
                .sum();
    }

    public double utility(final int turn) {
        return sumOfDistances * production * (Board.MAX_TURNS - (turn + starts));
    }

    public double attackPotential(final List<Factory> factories) {
        return factories.stream()
                .filter(c -> c.player != 0)
                .filter(c -> c.player == -player)
                .map(c -> c.distances)
                .mapToDouble(c -> 1.0 / c[id])
                .sum() * production;
    }

    public double helpPotential(final List<Factory> factories) {
        return factories.stream()
                .filter(c -> c.player != 0)
                .filter(c -> c.player == player)
                .map(c -> c.distances)
                .mapToDouble(c -> 1.0 / c[id])
                .sum() * production;
    }

    public boolean isRearFactory(final List<Factory> factories) {
        return helpPotential(factories) > 3 * attackPotential(factories);
    }

    public boolean isBehindEnemyLines(final List<Factory> factories) {
        return attackPotential(factories) > 5 * helpPotential(factories);
    }

    public Troop dispatchTroop(final Factory destination, final int armySize) {
        assert cyborgs >= armySize;
        cyborgs -= armySize;
        return new Troop(Troop.troops, id, destination.id, armySize, distances[destination.id], player);
    }

    public Histogram plotHistogram(final List<Troop> troops, int remainingTurns) {
        return new Histogram(this,
                             troops.stream().filter(troop -> troop.destination == id).collect(Collectors.toList()),
                             remainingTurns);
    }

    public Factory findNearestFriendly(final List<Factory> factories) {
        return factories.stream()
                .filter(factory -> factory.player == player)
                .min(Comparator.comparingInt(factory -> distances[factory.id]))
                .orElse(null);
    }

    public Factory findNearestEnemyWithConstraint(final List<Factory> factories,
                                                  final Factory source,
                                                  final int closestEnemyDistanceWithSource) {
        return factories.stream()
                .filter(factory -> factory.player == -player)
                .filter(factory -> factory.distances[source.id] < closestEnemyDistanceWithSource)
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

    public Troop abandon(final Factory destination) {
        final Troop troop = new Troop(Troop.troops, id, destination.id, cyborgs, distances[destination.id], player);
        cyborgs = 0;
        return troop;
    }

    public int getSpareShips() {
        return cyborgs;
    }

    public Histogram getHistogram(final List<Troop> troops, final int remainingTurns) {
        if (histogram == null) {
            histogram = plotHistogram(troops, remainingTurns);
        }
        return histogram;
    }
}

class Histogram {
    public static final int PLAYERS = 3;
    public static final int LOOK_AHEAD = 25;
    final int[][][] histogram;
    final int[] owner;
    final int size;
    // PLAYER
    //(CURRENT_COUNT, ARRIVING_TROOPS, DEPARTING_TROOPS)
    // TIME

    public Histogram(final Factory factory, final List<Troop> incomingTroops, int remainingTurns) {
        remainingTurns = remainingTurns < LOOK_AHEAD ? remainingTurns : LOOK_AHEAD;
        histogram = new int[PLAYERS][3][remainingTurns];
        owner = new int[remainingTurns];
        size = remainingTurns;
        for (final Troop troop : incomingTroops) {
            if (troop.timeToDestination < remainingTurns) {
                histogram[getPlayerIndex(troop.player)][1][troop.timeToDestination] += troop.size;
            }
        }
        histogram[factory.player][0][getPlayerIndex(factory.player)] = factory.cyborgs;
        int currentPlayer = factory.player;
        owner[0] = currentPlayer;
        for (int i = 1; i < remainingTurns; i++) {
            histogram[0][0][i] = histogram[0][0][i - 1] + histogram[0][1][i] - histogram[0][2][i];
            histogram[1][0][i] = histogram[1][0][i - 1] + histogram[1][1][i] - histogram[1][2][i];
            histogram[2][0][i] = histogram[2][0][i - 1] + histogram[2][1][i] - histogram[2][2][i];
            if (currentPlayer == 1) {
                histogram[1][0][i] += factory.production;
            } else if (currentPlayer == -1) {
                histogram[2][0][i] += factory.production;
            }
            if (histogram[1][0][i] > histogram[2][0][i]) {
                histogram[1][0][i] = histogram[1][0][i] - histogram[2][0][i];
                histogram[2][0][i] = 0;
            } else if (histogram[1][0][i] < histogram[2][0][i]) {
                histogram[2][0][i] = histogram[2][0][i] - histogram[1][0][i];
                histogram[1][0][i] = 0;
            } else {
                histogram[2][0][i] = histogram[1][0][i] = 0;
            }
            if (histogram[2][0][i] > histogram[0][0][i]) {
                histogram[2][0][i] = histogram[2][0][i] - histogram[0][0][i];
                histogram[0][0][i] = 0;
                currentPlayer = -1;
            } else if (histogram[1][0][i] > histogram[0][0][i]) {
                histogram[1][0][i] = histogram[1][0][i] - histogram[0][0][i];
                histogram[0][0][i] = 0;
                currentPlayer = 1;
            } else {
                histogram[0][0][i] = histogram[0][0][i] - histogram[1][0][i] - histogram[2][0][i];
                histogram[2][0][i] = histogram[1][0][i] = 0;
            }
            owner[i] = currentPlayer;
        }
    }

    private int getPlayerIndex(final int player) {
        return player == -1 ? 2 : player;
    }
}

class Bomb extends Entity {
    final int player, source;
    int destination;
    final int totalTimeToBlow;
    int timeToExplode;

    public Bomb(final int entityId,
                final int player,
                final int source,
                final int destination,
                final int timeToExplode) {
        super(entityId, "BOMB");
        this.player = player;
        this.source = source;
        this.destination = destination;
        this.totalTimeToBlow = timeToExplode;
        this.timeToExplode = timeToExplode;
    }

    public Factory explode(final Factory factory) {
        if (destination != -1 && factory.id != destination) {
            throw new RuntimeException("Wrong place to blow!");
        } else if (timeToExplode != 0) {
            throw new RuntimeException("It shouldn't blow now...");
        } else if (factory.cyborgs > 20) {
            factory.cyborgs /= 2;
        } else {
            factory.cyborgs = factory.cyborgs > 10 ? factory.cyborgs - 10 : 0;
        }
        return factory;
    }

    public Troop convertToTroop(final List<Factory> factories) {
        if (destination == -1) {
            final Factory sourceFactory = factories.stream()
                    .filter(f -> f.id == this.source)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such source!"));
            for (int i = 0; i < factories.size(); i++) {
                if (sourceFactory.distances[i] == totalTimeToBlow) {
                    destination = factories.get(i).id;
                }
            }
        }
        final Factory destination = factories.stream()
                .filter(f -> f.id == this.destination)
                .findAny()
                .orElseThrow(() -> new RuntimeException("No such destination!"));
        return new Troop(Troop.troops, player,
                         source,
                         this.destination,
                         destination.cyborgs > 20 ?
                                 destination.cyborgs / 2 :
                                 destination.cyborgs > 10
                                         ? 10
                                         : destination.cyborgs,
                         timeToExplode);
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
                 final int source,
                 final int destination,
                 final int size,
                 final int timeToDestination) {
        super(entityId, "TROOP");
        this.size = size;
        this.player = player;
        this.timeToDestination = timeToDestination;
        this.source = source;
        this.destination = destination;
        troops++;
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
            return factory;
        }
        throw new RuntimeException("You need to fight later...");
    }

    public void decrementTime() {
        --timeToDestination;
    }
}