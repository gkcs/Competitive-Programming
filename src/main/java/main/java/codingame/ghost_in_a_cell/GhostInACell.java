package main.java.codingame.ghost_in_a_cell;

import java.util.*;
import java.util.function.Function;
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
        final List<Bomb> bombs = new ArrayList<>();
        while (turn <= 400) {
            final int entityCount = in.nextInt();
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
                switch (entityType) {
                    case "FACTORY":
                        factories.add(new Factory(entityId, arg1, arg2, arg3, arg4, distances[entityId]));
                        break;
                    case "TROOP":
                        troops.add(new Troop(entityId, arg1, arg2, arg3, arg4, arg5));
                        break;
                    case "BOMB":
                        final Bomb previousVersion = bombs.stream()
                                .filter(bomb -> bomb.timeToExplode == arg4 + 1)
                                .filter(bomb -> bomb.source == arg2)
                                .findAny()
                                .orElse(null);
                        if (previousVersion == null) {
                            final Bomb bomb = new Bomb(entityId, arg1, arg2, arg3, arg4);
                            bombs.add(bomb);
                        } else {
                            previousVersion.decrementTime();
                        }
                        break;
                }
            }
            final Board board = new Board(factories, troops, bombs, turn);
            final String bestMoves = board.findBestMoves();
            System.out.println(bestMoves.equals("") ? "WAIT" : bestMoves);
            bombs.removeIf(bomb -> bomb.timeToExplode == 1);
            turn++;
        }
    }
}

class Board {
    public static final Requirement NULL_REQUIREMENT = new Requirement(null, 10000, -10000, 0, -10000);
    private final List<Troop> troops;
    private final List<Bomb> bombs;
    private final List<Factory> factories;
    private final List<Factory> myFactories;
    private final List<Factory> opponentFactories;
    private final int turn;
    public static final int MAX_TURNS = 400;
    private final Map<Integer, Factory> factoryIndexMap;
    private static int bombsUsed = 0;

    public Board(final List<Factory> factories,
                 final List<Troop> troops,
                 final List<Bomb> bombs,
                 final int turn) {
        this.factories = factories;
        this.troops = troops;
        this.bombs = bombs;
        this.turn = turn;
        myFactories = factories.stream().filter(factory -> factory.player == 1).collect(Collectors.toList());
        opponentFactories = factories.stream().filter(factory -> factory.player == -1).collect(Collectors.toList());
        factoryIndexMap = factories.stream().collect(Collectors.toMap(factory -> factory.id, Function.identity()));
        this.bombs.stream().filter(bomb -> bomb.player == -1)
                .forEach(bomb -> bomb.setDestination(myFactories, factoryIndexMap));
    }

    public String findBestMoves() {
        return play().stream().map(Move::toString).collect(Collectors.joining(";"));
    }

    private List<Move> play() {
        final Map<Factory, Factory> supplyToCity = getSupplyDestinations();
        //move the ships to attack/defense. Then look for strategic movements to the frontLine
        final Requirement utility[][] = new Requirement[factories.size()][factories.get(0).getHistogram(troops,
                                                                                                        turn).size];
        for (final Requirement[] requirements : utility) {
            for (int i = 0; i < requirements.length; i++) {
                requirements[i] = NULL_REQUIREMENT;
            }
        }
        final Map<Factory, Integer> excessTroops = new HashMap<>();
        final List<Integer> hopelessFactories = bombs.stream()
                .filter(bomb -> bomb.player == -1)
                .filter(bomb -> bomb.timeToExplode == 1)
                .filter(bomb -> bomb.destination != -1)
                .map(bomb -> bomb.destination)
                .collect(Collectors.toList());
        for (final Factory factory : factories) {
            final Histogram histogram = factory.getHistogram(troops, turn);
            final int minimumGarrison = Arrays.stream(histogram.histogram[1][0]).min().orElse(0);
            if (factory.player == 1) {
                if (hopelessFactories.contains(factory.id)) {
                    excessTroops.put(factory, factory.cyborgs);
                } else if (minimumGarrison > 0) {
                    excessTroops.put(factory, minimumGarrison);
                }
            } else {
                for (int time = 0; time < histogram.size; time++) {
                    if (histogram.owner[time] != 1) {
                        final int opponentShips = histogram.histogram[0][0][time]
                                + histogram.histogram[2][0][time]
                                + 1;
                        final int numberToGain = (Board.MAX_TURNS - time - turn)
                                * factory.production * (Math.abs(factory.player) + 1);
                        utility[factory.id][time] = new Requirement(factory,
                                                                    opponentShips,
                                                                    numberToGain,
                                                                    time,
                                                                    numberToGain / (double) opponentShips);
                    }
                }
            }
        }
        final List<Requirement> currentRequirements = new ArrayList<>();
        for (final Requirement[] requirements : utility) {
            Collections.addAll(currentRequirements, requirements);
        }
        currentRequirements.removeIf(currentRequirement -> currentRequirement.utility <= 0);
        currentRequirements.sort((o1, o2) -> Double.valueOf(o2.utility).compareTo(o1.utility));
        final List<Troop> movements = getTroopTactics(supplyToCity, excessTroops, currentRequirements);
        moveSupplies(supplyToCity, excessTroops, movements);
        final List<Move> moves = movements.stream().map(troop -> new Move(troop.source,
                                                                          troop.destination,
                                                                          troop.size,
                                                                          MoveType.MOVE)).collect(Collectors.toList());
        speedFactoryProduction(excessTroops, moves);
        bombThem(movements, moves);
        takeNullFactories(excessTroops, moves);
        dodgeBombs(excessTroops, hopelessFactories, moves);
        //TODO: stop passing the baton if not front city, take bomb impact on histogram
        return moves;
    }

    private Map<Factory, Factory> getSupplyDestinations() {
        final Map<Factory, Factory> supplyToCity = new HashMap<>();
        final List<Factory> frontLine = new ArrayList<>();
        final List<Factory> suppliers = new ArrayList<>();
        for (final Factory source : myFactories) {
            final Factory nearestEnemy = source.findNearestEnemy(opponentFactories);
            if (nearestEnemy != null) {
                final Factory target = nearestEnemy.findNearestEnemyWithConstraint(myFactories,
                                                                                   source,
                                                                                   source.distances[nearestEnemy.id]);
                if (target == null) {
                    frontLine.add(source);
                } else {
                    suppliers.add(source);
                    supplyToCity.put(source, target);
                }
            }
        }
        suppliers.removeAll(frontLine);
        final Set<Factory> touched = new HashSet<>();
        for (final Factory supplier : suppliers) {
            if (!touched.contains(supplier)) {
                touched.add(supplier);
                final Factory start = supplier;
                final List<Factory> citiesTouched = new ArrayList<>();
                Factory current = start;
                citiesTouched.add(current);
                while (current != null && supplyToCity.containsKey(current) &&
                        !citiesTouched.contains(supplyToCity.get(current))) {
                    current = supplyToCity.get(current);
                    citiesTouched.add(current);
                    touched.add(current);
                }
                if (current != null && citiesTouched.contains(current)) {
                    supplyToCity.remove(current);
                }
            }
        }
        return supplyToCity;
    }

    private void dodgeBombs(final Map<Factory, Integer> excessTroops,
                            final List<Integer> hopelessFactories,
                            final List<Move> moves) {
        if (hopelessFactories.size() > 0) {
            final List<Factory> hopelessFactory = hopelessFactories.stream()
                    .map(factoryIndexMap::get)
                    .collect(Collectors.toList());
            for (final Factory factory : hopelessFactory) {
                final Troop exodus = factory.abandon(factory.findNearest(factories));
                moves.add(new Move(exodus.source, exodus.destination, exodus.size, MoveType.MOVE));
                excessTroops.put(factory, 0);
            }
        }
    }

    private void takeNullFactories(final Map<Factory, Integer> excessTroops, final List<Move> moves) {
        final List<Factory> nullFactories = factories.stream()
                .filter(factory -> factory.player != 1)
                .filter(factory -> factory.production == 0)
                .filter(factory -> {
                    final Histogram histogram = factory.getHistogram(troops, turn);
                    return histogram.owner[histogram.size - 1] == 0 && histogram.histogram[0][0][0] == 0;
                })
                .collect(Collectors.toList());
        for (final Factory factory : myFactories) {
            while (nullFactories.size() > 0 && excessTroops.getOrDefault(factory, 0) >= 1) {
                final Factory destination = nullFactories.stream()
                        .min(Comparator.comparingInt(o -> factory.distances[o.id]))
                        .orElseThrow(() -> new RuntimeException("No such factory!"));
                moves.add(new Move(factory.id, destination.id, 1, MoveType.MOVE));
                nullFactories.remove(destination);
                excessTroops.put(factory, excessTroops.get(factory) - 1);
            }
        }
    }

    private void bombThem(final List<Troop> movements, final List<Move> moves) {
        if (bombsUsed < 2) {
            final List<Factory> askingForIt = new ArrayList<>();
            for (final Factory factory : opponentFactories) {
                if (factory.production > 1 && movements.stream()
                        .map(c -> c.destination)
                        .noneMatch(c -> c == factory.id) && troops.stream()
                        .filter(troop -> troop.player != 1)
                        .map(c -> c.destination)
                        .noneMatch(c -> c == factory.id)
                        && bombs.stream()
                        .map(c -> c.destination)
                        .noneMatch(c -> c == factory.id)) {
                    askingForIt.add(factory);
                }
            }
            askingForIt.sort(Comparator.comparingInt(o -> o.findNearestEnemy(myFactories).distances[o.id]));
            int bomb = 0;
            while (bombsUsed < 2 && bomb < askingForIt.size()) {
                final Factory target = askingForIt.get(bomb);
                final Factory nearestEnemy = target.findNearestEnemy(myFactories);
                if (nearestEnemy == null) {
                    break;
                }
                moves.add(new Move(nearestEnemy.id, target.id, -1, MoveType.BOMB));
                bomb++;
                bombsUsed++;
            }
        }
    }

    private void speedFactoryProduction(final Map<Factory, Integer> excessTroops, final List<Move> moves) {
        for (final Factory factory : excessTroops.keySet()) {
            if (excessTroops.get(factory) >= 10 && factory.production < 3) {
                moves.add(new Move(factory.id, -1, -1, MoveType.INC));
                excessTroops.put(factory, excessTroops.get(factory) - 10);
            }
        }
    }

    private void moveSupplies(final Map<Factory, Factory> suppliers,
                              final Map<Factory, Integer> excessTroops,
                              final List<Troop> movements) {
        for (final Factory source : suppliers.keySet()) {
            final Factory target = suppliers.get(source);
            if (target.getHistogram(troops, turn).owner[source.distances[target.id]] == 1) {
                if (excessTroops.getOrDefault(source, 0) > 0) {
                    movements.add(source.dispatchTroop(target, excessTroops.get(source)));
                    excessTroops.put(source, 0);
                }
            }
        }
    }

    private List<Troop> getTroopTactics(final Map<Factory, Factory> suppliers,
                                        final Map<Factory, Integer> excessTroops,
                                        final List<Requirement> currentRequirements) {
        final List<Troop> movements = new ArrayList<>();
        for (final Requirement currentRequirement : currentRequirements) {
            if (currentRequirement.utility > 0) {
                if (currentRequirement.factory.player != -1) {
                    tryToMeetRequirement(excessTroops, excessTroops, movements, currentRequirement);
                } else {
                    final Map<Factory, Integer> excessWithoutSuppliers = new HashMap<>();
                    excessTroops.forEach((key, value) -> {
                        if (!suppliers.containsKey(key)) {
                            excessWithoutSuppliers.put(key, value);
                        }
                    });
                    tryToMeetRequirement(excessWithoutSuppliers,
                                         excessTroops,
                                         movements,
                                         currentRequirement);
                }
            }
        }
        return movements;
    }

    private void tryToMeetRequirement(final Map<Factory, Integer> excessTroops,
                                      final Map<Factory, Integer> original,
                                      final List<Troop> movements,
                                      final Requirement currentRequirement) {
        final int sum = excessTroops.values().stream().mapToInt(c -> c).sum();
        if (sum >= currentRequirement.size) {
            final List<Map.Entry<Factory, Integer>> sortedExcessTroops = excessTroops.entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(o -> o.getKey().distances[currentRequirement.factory.id]))
                    .collect(Collectors.toList());
            for (final Map.Entry<Factory, Integer> entry : sortedExcessTroops) {
                if (entry.getKey().distances[currentRequirement.factory.id] <= currentRequirement.timeToArrive) {
                    if (entry.getValue() >= currentRequirement.size) {
                        if (entry.getKey().distances[currentRequirement.factory.id]
                                == currentRequirement.timeToArrive - 1) {
                            movements.add(entry.getKey().dispatchTroop(currentRequirement.factory,
                                                                       currentRequirement.size));
                        }
                        excessTroops.put(entry.getKey(), entry.getValue() - currentRequirement.size);
                        currentRequirement.size = 0;
                        break;
                    } else {
                        if (entry.getKey().distances[currentRequirement.factory.id] == currentRequirement.timeToArrive) {
                            movements.add(entry.getKey().dispatchTroop(currentRequirement.factory, entry.getValue()));
                        }
                        currentRequirement.size -= entry.getValue();
                        excessTroops.put(entry.getKey(), 0);
                    }
                    original.put(entry.getKey(), excessTroops.get(entry.getKey()));
                }
            }
        }
    }
}

class Requirement {
    final Factory factory;
    int size;
    final int shipsGained;
    final int timeToArrive;
    final double utility;

    public Requirement(final Factory factory,
                       final int size,
                       final int shipsGained,
                       final int timeToArrive,
                       final double utility) {
        this.factory = factory;
        this.size = size;
        this.shipsGained = shipsGained;
        this.timeToArrive = timeToArrive;
        this.utility = utility;
    }

    @Override
    public String toString() {
        return size + " " + utility;
    }
}

class Move {
    final int source, destination, troopSize;
    final MoveType moveType;

    public Move(final int source, final int destination, final int troopSize, final MoveType moveType) {
        this.source = source;
        this.destination = destination;
        this.troopSize = troopSize;
        this.moveType = moveType;
    }

    @Override
    public String toString() {
        if (moveType.equals(MoveType.MOVE)) {
            return moveType.name() + " " + source + " " + destination + " " + troopSize;
        } else if (moveType.equals(MoveType.BOMB)) {
            return moveType.name() + " " + source + " " + destination;
        } else if (moveType.equals(MoveType.INC)) {
            return moveType.name() + " " + source;
        } else {
            throw new RuntimeException("Unknown move type!");
        }
    }
}

enum MoveType {
    MOVE, BOMB, INC
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
        distances[entityId] = 1000;
        this.starts = starts;
        sumOfDistances = Arrays.stream(distances)
                .mapToDouble(distance -> 1.0 / distance)
                .sum();
    }

    public Troop dispatchTroop(final Factory destination, final int armySize) {
        //TODO: WHY DOES THIS THROW UP?? assert cyborgs >= armySize;
        cyborgs -= armySize;
        return new Troop(Troop.troops++, player, id, destination.id, armySize, distances[destination.id]);
    }

    private Histogram plotHistogram(final List<Troop> troops, final int turn) {
        return new Histogram(this,
                             troops.stream().filter(troop -> troop.destination == id).collect(Collectors.toList()),
                             Board.MAX_TURNS - turn);
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

    public Factory findNearest(final List<Factory> factories) {
        return factories.stream()
                .min(Comparator.comparingInt(factory -> distances[factory.id]))
                .orElse(null);
    }

    public Troop abandon(final Factory destination) {
        final Troop troop = new Troop(0, id, destination.id, cyborgs, distances[destination.id], player);
        cyborgs = 0;
        return troop;
    }

    public Histogram getHistogram(final List<Troop> troops, final int turn) {
        if (histogram == null) {
            histogram = plotHistogram(troops, turn);
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
        histogram[getPlayerIndex(factory.player)][0][0] = factory.cyborgs;
        int currentPlayer = factory.player;
        int starts = factory.starts;
        owner[0] = currentPlayer;
        for (int i = 1; i < remainingTurns; i++) {
            histogram[0][0][i] = histogram[0][0][i - 1] + histogram[0][1][i] - histogram[0][2][i];
            histogram[1][0][i] = histogram[1][0][i - 1] + histogram[1][1][i] - histogram[1][2][i];
            histogram[2][0][i] = histogram[2][0][i - 1] + histogram[2][1][i] - histogram[2][2][i];
            if (starts <= 0) {
                if (currentPlayer == 1) {
                    histogram[1][0][i] += factory.production;
                } else if (currentPlayer == -1) {
                    histogram[2][0][i] += factory.production;
                }
            } else {
                starts--;
            }
            if (histogram[1][0][i] > histogram[2][0][i]) {
                histogram[1][0][i] -= histogram[2][0][i];
                histogram[2][0][i] = 0;
            } else if (histogram[1][0][i] < histogram[2][0][i]) {
                histogram[2][0][i] -= histogram[1][0][i];
                histogram[1][0][i] = 0;
            } else {
                histogram[2][0][i] = histogram[1][0][i] = 0;
            }
            if (histogram[2][0][i] > histogram[0][0][i]) {
                histogram[2][0][i] -= histogram[0][0][i];
                histogram[0][0][i] = 0;
                currentPlayer = -1;
            } else if (histogram[1][0][i] > histogram[0][0][i]) {
                histogram[1][0][i] -= histogram[0][0][i];
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

    public void setDestination(final List<Factory> myFactories, final Map<Integer, Factory> opponentFactories) {
        if (destination == -1) {
            final Factory opponentSource = opponentFactories.get(source);
            final List<Factory> possibleDestinations = new ArrayList<>();
            for (final Factory myFactory : myFactories) {
                if (opponentSource.distances[myFactory.id] == totalTimeToBlow) {
                    possibleDestinations.add(myFactory);
                }
            }
            if (possibleDestinations.size() == 1) {
                destination = possibleDestinations.get(0).id;
            }
            //Else They are attacking neutrals
        }
    }

    public void decrementTime() {
        --timeToExplode;
    }
}

class Troop extends Entity {
    public static int troops = 0;
    int timeToDestination, destination;
    final int size, player, source;

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
    }
}