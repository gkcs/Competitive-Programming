package main.java.codingame.CodersOfTheCarribean;

import java.util.*;

public class CodersOfTheCaribbean {

    public static final String SHIP = "SHIP";
    public static final String BARREL = "BARREL";
    public static final String FIRE = "FIRE ";
    public static final String MOVE = "MOVE ";
    public static final String SLOWER = "SLOWER";
    public static final String WAIT = "WAIT";
    public static final String FASTER = "FASTER";
    public static final String MINE = "MINE";
    public static final String CANNONBALL = "CANNONBALL";
    public static final String STARBOARD = "STARBOARD";
    public static final String PORT = "PORT";
    private static final Random random = new Random();

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        final boolean canFire[] = new boolean[100];
        for (int i = 0; i < canFire.length; i++) {
            canFire[i] = true;
        }
        while (true) {
            final int myShipCount = in.nextInt();
            final int entityCount = in.nextInt();
            final List<Ship> myShips = new ArrayList<>();
            final List<Ship> enemyShips = new ArrayList<>();
            final List<Barrel> barrels = new ArrayList<>();
            final List<Mine> mines = new ArrayList<>();
            final List<CannonBall> cannonBalls = new ArrayList<>();
            for (int i = 0; i < entityCount; i++) {
                final int entityId = in.nextInt();
                final String entityType = in.next();
                final int x = in.nextInt();
                final int y = in.nextInt();
                final int arg1 = in.nextInt();
                final int arg2 = in.nextInt();
                final int arg3 = in.nextInt();
                final int arg4 = in.nextInt();
                if (entityType.equals(SHIP)) {
                    final Ship ship = new Ship(arg1, arg2, arg3, arg4, x, y, entityId);
                    if (arg4 == 1) {
                        myShips.add(ship);
                    } else {
                        enemyShips.add(ship);
                    }
                } else if (entityType.equals(BARREL)) {
                    barrels.add(new Barrel(arg1, x, y, entityId));
                } else if (entityType.equals(MINE)) {
                    mines.add(new Mine(x, y, entityId));
                } else if (entityType.equals(CANNONBALL)) {
                    cannonBalls.add(new CannonBall(x, y, arg1, arg2, entityId));
                } else {
                    throw new RuntimeException();
                }
            }
            final Ship myBestShip = myShips.stream().max(Comparator.comparingInt(o -> o.rum)).orElseThrow(RuntimeException::new);
            final Ship opponentBestShip = enemyShips.stream().max(Comparator.comparingInt(o -> o.rum)).orElseThrow(RuntimeException::new);

        }
    }
}

class Move {
    final List<Action> actions;
    final int player;

    public Move(final List<Action> actions, final int player) {
        this.actions = actions;
        this.player = player;
    }
}

class Action {
    final Ship actor;

    public Action(Ship actor) {
        this.actor = actor;
    }
}

enum ACT {
    EVADE, ATTACK, DEFEND, GATHER, WAIT

    //Face the center if doing nothing
    //If you are not getting the barrel, they aren't either
    //Don’t fire if he’s gonna blow in some time
    //Check for friendly fire
    //Check if a bomb is heading towards you
    //If a person is down, with barrels remaining, hunt them down. Don’t let them come back

    //Stay away from enemy ships. Gather barrels if possible/necessary. Try not to get cornered. Mine maybe...
    //Find the wealthiest ship. Attack/Hunt it.
    //Stay (Not too) close to the wealthiest ship. Flank it. On being attacked, attack the attackers.
    //Take barrels if possible. Choose the one nearest, and furthest from enemies.
    //If a ship is attacked, back them up. If you are under attack try to move towards a barrel/friend while firing back.
}

class Board {
    public static final int WIDTH = 23;
    public static final int HEIGHT = 21;
    final Coordinate board[][] = new Coordinate[HEIGHT][WIDTH];

    public Board() {

    }

    public Mine findTheNearestMine(final List<Mine> mines, final Coordinate coordinate) {
        return mines.stream().max(Comparator.comparingInt(o -> o.calculateDistance(coordinate))).orElse(null);
    }

    public Ship findTheNearestEnemy(final List<Ship> opponentShips, final Coordinate coordinate) {
        return opponentShips.stream().max(Comparator.comparingInt(o -> o.calculateDistance(coordinate))).orElse(null);
    }

    public Coordinate evade(final Ship myShip, final Coordinate coordinate) {
        final int deltaX = coordinate.x > myShip.x ? -5 : 5;
        final int deltaY = coordinate.y > myShip.y ? -5 : 5;
        return new Coordinate(myShip.x + deltaX, myShip.y + deltaY);
    }

    public Barrel findTheNearestBarrel(final List<Barrel> barrels, final Ship ship) {
        Barrel nearest = barrels.get(0);
        for (final Barrel barrel : barrels) {
            if (ship.calculateDistance(barrel) <= ship.calculateDistance(nearest)) {
                if (ship.calculateDistance(barrel) == ship.calculateDistance(nearest)) {
                    final int orientation = ship.closestOrientation(barrel);
                    final int currentOrientation = ship.closestOrientation(nearest);
                    if (Math.abs(Math.abs(ship.orientation - orientation) - 3) < Math.abs(Math.abs(ship.orientation - currentOrientation) - 3)) {
                        nearest = barrel;
                    }
                } else {
                    nearest = barrel;
                }
            }
        }
        return nearest;
    }
}

class Coordinate {
    int x, y;

    public Coordinate(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public int calculateDistance(final Coordinate coordinate) {
        final int x1 = y - (x + (x & 1)) / 2, z1 = x, y1 = -x1 - z1;
        final int x2 = coordinate.y - (coordinate.x + (coordinate.x & 1)) / 2, z2 = coordinate.x, y2 = -x2 - z2;
        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) >> 1;
    }

    public boolean isOutSideMap() {
        return x >= Board.HEIGHT || x < 0 || y >= Board.WIDTH || y < 0;
    }
}

class Entity extends Coordinate {
    final int id;

    public Entity(final int id, final int x, final int y) {
        super(x, y);
        this.id = id;
    }

    public boolean equalsLogically(Object other) {
        return this == other || !(other == null || getClass() != other.getClass()) && id == ((Entity) other).id;
    }
}

class CannonBall {
    final Coordinate target;
    final int firedBy;
    final int id;
    int turnsToHitTarget;

    public CannonBall(final int x, final int y, final int firedBy, final int turnsToHitTarget, int id) {
        //todo: set the x and y correctly, if required
        this.id = id;
        this.target = new Coordinate(x, y);
        this.firedBy = firedBy;
        this.turnsToHitTarget = turnsToHitTarget;
    }

    public CannonBall decrementTimeToHit() {
        turnsToHitTarget--;
        return this;
    }

    public static int findTimeToReach(final Coordinate currentPosition, final Coordinate target) {
        return (int) (Math.round(1 + (currentPosition.calculateDistance(target)) / 3.0));
    }

    @Override
    public String toString() {
        return "CannonBall{" +
                "targetX=" + target +
                ", firedBy=" + firedBy +
                ", turnsToHitTarget=" + turnsToHitTarget +
                '}';
    }
}

class Barrel extends Entity {
    final int rum;

    public Barrel(final int rum, final int x, final int y, int id) {
        super(id, x, y);
        this.rum = rum;
    }
}

class Mine extends Entity {

    public Mine(final int x, final int y, final int id) {
        super(id, x, y);
    }
}

class Ship extends Entity {
    int orientation;
    int speed;
    int rum;
    final int player;
    boolean canShoot = true;
    private static final int oddMovement[][] = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
    private static final int evenMovement[][] = new int[][]{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final int movement[][] = new int[][]{{2, 0}, {1, -2}, {-1, -2}, {-2, 0}, {-1, 2}, {1, 2}};

    public Ship(final int orientation, final int speed, final int rum, final int player, final int x, final int y, int id) {
        super(id, x, y);
        this.orientation = orientation;
        this.speed = speed;
        this.rum = rum;
        this.player = player;
    }

    public Coordinate shoot(final Ship ship) {
        if (ship.player == player) {
            throw new RuntimeException("Don't fire at friendlies, retard!");
        } else {
            final int myMovement[] = x % 2 == 0 ? evenMovement[orientation] : oddMovement[orientation];
            final Coordinate enemyLocation = new Coordinate(ship.x, ship.y);
            for (int time = 0; time < 5; time++) {
                final int[] movement = enemyLocation.x % 2 == 0
                        ? evenMovement[ship.orientation]
                        : oddMovement[ship.orientation];
                final int predictedX = enemyLocation.x + time * ship.speed * movement[0];
                final int predictedY = enemyLocation.y + time * ship.speed * movement[1];
                final Coordinate prediction = new Coordinate(predictedX, predictedY);
                if (calculateDistance(prediction) <= 10 && CannonBall.findTimeToReach(new Coordinate(x + myMovement[0], y + myMovement[1]), prediction) == time) {
                    return prediction;
                }
            }
            return null;
        }
    }

    public Ship slowDown() {
        if (speed > 0) {
            speed--;
        }
        return this;
    }

    public Ship increaseSpeed() {
        if (speed < 2) {
            speed++;
        }
        return this;
    }

    public Ship damage(final int damage) {
        rum -= damage;
        return this;
    }

    private boolean isAHit(final Coordinate current, final Coordinate bomb, final int orientation) {
        final int movement[][];
        if (current.x % 2 == 0) {
            movement = evenMovement;
        } else {
            movement = oddMovement;
        }
        final int[] front = movement[orientation];
        final int[] back = movement[(orientation + 3) % 6];
        return bomb.equals(current) || bomb.equals(new Coordinate(current.x + front[0], current.y + front[1])) || bomb.equals(new Coordinate(current.x + back[0], current.y + back[1]));
    }

    public Mine willCollide(final List<Mine> mines, final Coordinate destination) {
        if (mines.isEmpty()) {
            return null;
        }
        final List<Projection> path = findPath(destination);
        final boolean coordinates[][] = new boolean[Board.HEIGHT][Board.WIDTH];
        for (final Coordinate coordinate : path) {
            coordinates[coordinate.x][coordinate.y] = true;
        }
        for (final Mine mine : mines) {
            if (coordinates[mine.x][mine.y]) {
                return mine;
            }
        }
        return null;
    }

    public CannonBall cannonsWillHit(final List<CannonBall> cannonBalls, final Coordinate destination) {
        if (cannonBalls.isEmpty()) {
            return null;
        }
        final List<Projection> path = findPath(destination);
        for (final Projection projection : path) {
            final int movement[][] = projection.x % 2 == 0 ? evenMovement : oddMovement;
            final int front[] = movement[orientation];
            final int back[] = movement[(orientation + 3) % 6];
            final List<Coordinate> coordinates = Arrays.asList(projection,
                    new Coordinate(projection.x + front[0], projection.y + front[1]),
                    new Coordinate(projection.x + back[0], projection.y + back[1]));
            for (final CannonBall cannonBall : cannonBalls) {
                if (coordinates.contains(cannonBall.target)) {
                    if (cannonBall.turnsToHitTarget == projection.time) {
                        return cannonBall;
                    }
                }
            }
        }
        return null;
    }

    public List<Projection> findPath(final Coordinate destination) {
        final List<Projection> path = new ArrayList<>();
        final Ship temp = new Ship(orientation, speed, rum, player, x, y, id);
        int time = 0;
        while (!temp.equals(destination)) {
            path.add(new Projection(temp.x, temp.y, temp.orientation, time));
            final int closestOrientation = temp.closestOrientation(destination);
            if (temp.orientation == closestOrientation) {
                if (temp.speed != 2) {
                    temp.increaseSpeed();
                }
            } else {
                if (Math.abs(closestOrientation - temp.orientation) % 6 == 1) {
                    temp.orientation = closestOrientation;
                } else {
                    if (Math.abs(closestOrientation - temp.orientation) % 6 == 2) {
                        temp.orientation = closestOrientation;
                    } else {
                        temp.orientation = toTheLeft(destination) ? (temp.orientation + 2) % 6 : (temp.orientation + 4) % 6;
                    }
                }
            }
            final int movement[] = temp.x % 2 == 0 ? evenMovement[temp.orientation] : oddMovement[temp.orientation];
            temp.x += movement[0];
            temp.y += movement[1];
            if (speed == 2) {
                final Projection location = new Projection(temp.x, temp.y, temp.orientation, time);
                if (location.equals(destination)) {
                    break;
                }
                path.add(location);
                temp.x += movement[0];
                temp.y += movement[1];
            }
            time++;
        }
        path.add(new Projection(temp.x, temp.y, temp.orientation, time));
        return path;
    }

    private boolean toTheLeft(final Coordinate coordinate) {
        return (x - coordinate.x) * movement[orientation][1] / (double) movement[orientation][0] + coordinate.y - y < 0;
    }

    public int closestOrientation(final Coordinate coordinate) {
        return (int) (Math.round((360 + Math.toDegrees(Math.atan((coordinate.y - y) / (double) (coordinate.x - x)))) / 60.0) % 6);
    }

    public boolean isAligned(final Coordinate coordinate, final int orientation) {
        return orientation == closestOrientation(coordinate);
    }
}

class Projection extends Coordinate {
    final int orientation;
    final int time;

    Projection(final int x, final int y, final int orientation, final int time) {
        super(x, y);
        this.orientation = orientation;
        this.time = time;
    }
}
