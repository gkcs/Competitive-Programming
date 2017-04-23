package main.java.codingame;

import java.util.*;

public class Pirates {

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
                switch (entityType) {
                    case SHIP:
                        if (arg4 == 1) {
                            myShips.add(new Ship(arg1, arg2, arg3, arg4, x, y, entityId));
                        } else {
                            enemyShips.add(new Ship(arg1, arg2, arg3, arg4, x, y, entityId));
                        }
                        break;
                    case BARREL:
                        barrels.add(new Barrel(arg1, x, y, entityId));
                        break;
                    case MINE:
                        mines.add(new Mine(x, y, entityId));
                        break;
                    case CANNONBALL:
                        cannonBalls.add(new CannonBall(x, y, arg1, arg2, entityId));
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
            final Ship myBestShip = myShips.stream().max(Comparator.comparingInt(o -> o.rum)).orElseThrow(RuntimeException::new);
            final Ship opponentBestShip = enemyShips.stream().max(Comparator.comparingInt(o -> o.rum)).orElseThrow(RuntimeException::new);
            for (final Ship myShip : myShips) {
                final boolean canFireNow = canFire[myShip.id];
                Ship nearestEnemy = enemyShips.get(0);
                for (final Ship enemyShip : enemyShips) {
                    if (myShip.calculateDistance(enemyShip) < myShip.calculateDistance(nearestEnemy)) {
                        nearestEnemy = enemyShip;
                    }
                }
                Ship nearestAlly = myShips.get(0);
                for (final Ship ship : myShips) {
                    if (myShip.calculateDistance(ship) < myShip.calculateDistance(nearestAlly)) {
                        nearestAlly = ship;
                    }
                }
                final CannonBall incomingCannonBall = myShip.willHitOnTime(cannonBalls);
                if (myShip.speed == 0 && nearestAlly.calculateDistance(myShip) < 3 && nearestAlly.speed == 0) {
                    System.out.println(MOVE + random.nextInt(Ship.HEIGHT) + " " + random.nextInt(Ship.WIDTH));
                } else if (incomingCannonBall != null) {
                    System.err.println("Gonna be hit with cannons!" + incomingCannonBall.targetX + " " + incomingCannonBall.targetY);
                    if (myShip.speed > 0) {
                        System.out.println(Math.random() > 0.5 ? STARBOARD : PORT);
                    } else if (myShip.speed < 2) {
                        System.out.println(FASTER);
                    } else {
                        System.out.println(SLOWER);
                    }
                } else {
                    final Mine willCollide = myShip.willCollide(mines);
                    if (willCollide != null && myShip.calculateDistance(willCollide) < Ship.MAX_MINE_DISTANCE) {
                        System.err.println("Gonna hit a mine! " + willCollide);
                        if (myShip.speed > 0) {
                            System.out.println(Math.random() > 0.5 ? STARBOARD : PORT);
                        } else if (myShip.speed < 2) {
                            System.out.println(FASTER);
                        } else {
                            System.out.println(SLOWER);
                        }
                    } else if (myShip.calculateDistance(nearestEnemy) <= 9) {
                        final Coordinate shot = myShip.shoot(nearestEnemy);
                        if (shot != null && canFireNow && isNotFriendlyFire(shot, myShips, myShip)) {
                            System.out.println(FIRE + shot.x + " " + shot.y);
                            canFire[myShip.id] = false;
                        } else {
                            if (barrels.isEmpty()) {
                                System.out.println(MOVE + opponentBestShip.x + " " + opponentBestShip.y);
                            } else {
                                final Barrel nearest = findTheNearestBarrel(barrels, myShip);
                                if (nearestEnemy.rum <= myShip.rum - 25) {
                                    System.out.println(MOVE + nearestEnemy.x + " " + nearestEnemy.y);
                                } else {
                                    System.out.println(MOVE + nearest.x + " " + nearest.y);
                                }
                            }
                        }
                    } else if (!barrels.isEmpty()) {
                        final Barrel nearest = findTheNearestBarrel(barrels, myShip);
                        if (myShip.rum < 87 || myShip.calculateDistance(nearest) > 5) {
                            System.out.println(MOVE + nearest.x + " " + nearest.y);
                        } else {
                            if (myShip.speed == 0 && myShip.calculateDistance(nearest) > 3) {
                                System.out.println(MOVE + nearest.x + " " + nearest.y);
                            } else {
                                System.out.println(SLOWER);
                            }
                        }
                    } else if (barrels.isEmpty() && myShip.equals(myBestShip) && myShip.rum > opponentBestShip.rum) {
                        if (myShip.calculateDistance(nearestEnemy) > 11) {
                            final Mine nearestMine = findTheNearestMine(mines, myShip);
                            if (canFireNow && nearestMine != null && myShip.calculateDistance(nearestMine) <= 10) {
                                System.out.println(FIRE + nearestMine.x + " " + nearestMine.y);
                                canFire[myShip.id] = false;
                            } else if (myShip.speed != 0) {
                                System.out.println(SLOWER);
                            } else {
                                System.out.println(WAIT);
                            }
                        } else {
                            final Coordinate run = evade(myShips, myBestShip, opponentBestShip, myShip, nearestEnemy);
                            if (run.isOutSideMap()) {
                                System.out.println(MOVE + random.nextInt(Ship.HEIGHT) + " " + random.nextInt(Ship.WIDTH));
                            } else {
                                System.out.println(MOVE + run.x + " " + run.y);
                            }
                        }
                    } else if (myShip.calculateDistance(nearestEnemy) <= 10) {
                        final Coordinate shot = myShip.shoot(nearestEnemy);
                        if (shot != null && canFireNow) {
                            System.out.println(FIRE + shot.x + " " + shot.y);
                            canFire[myShip.id] = false;
                        } else {
                            System.out.println(MOVE + opponentBestShip.x + " " + opponentBestShip.y);
                        }
                    } else {
                        System.out.println(MOVE + opponentBestShip.x + " " + opponentBestShip.y);
                    }
                }
                if (!canFireNow) {
                    canFire[myShip.id] = true;
                }
//                System.err.println("Keep firing!");
            }
        }
    }

    private static boolean isNotFriendlyFire(final Coordinate shot, final List<Ship> myShips, final Ship attackingShip) {
        final List<CannonBall> cannonBall = Collections.singletonList(new CannonBall(shot.x, shot.y, attackingShip.id, CannonBall.findTimeToReach(attackingShip, shot), random.nextInt(1000)));
        for (final Ship myShip : myShips) {
            if (myShip.willHitOnTime(cannonBall) != null) {
                return false;
            }
        }
        return true;
    }

    private static Mine findTheNearestMine(final List<Mine> mines, final Ship myShip) {
        return mines.stream().max(Comparator.comparingInt(o -> o.calculateDistance(myShip))).orElse(null);
    }

    private static Coordinate evade(List<Ship> myShips, Ship myBestShip, Ship opponentBestShip, Ship myShip, Ship nearestEnemy) {
        int deltaX = nearestEnemy.x > myShip.x ? -5 : 5;
        int deltaY = nearestEnemy.y > myShip.y ? -5 : 5;
        if (myShips.size() == 1 && myBestShip.rum < opponentBestShip.rum) {
            deltaX = -deltaX;
            deltaY = -deltaY;
        }
        return new Coordinate(myShip.x + deltaX, myShip.y + deltaY);
    }

    private static Barrel findTheNearestBarrel(List<Barrel> barrels, Ship myShip) {
        Barrel nearest = barrels.get(0);
        for (final Barrel barrel : barrels) {
            if (myShip.calculateDistance(barrel) * Math.sqrt(Math.sqrt(barrel.rum)) <= myShip.calculateDistance(nearest) * Math.sqrt(Math.sqrt(nearest.rum))) {
                if (myShip.calculateDistance(barrel) * Math.sqrt(Math.sqrt(barrel.rum)) == myShip.calculateDistance(nearest) * Math.sqrt(Math.sqrt(nearest.rum))) {
                    if (barrel.id < nearest.id) {
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
    public static final int WIDTH = 23;
    public static final int HEIGHT = 21;
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
        int result = x;
        result = 31 * result + y;
        return result;
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
        return x >= HEIGHT || x < 0 || y >= WIDTH || y < 0;
    }
}

class Entity extends Coordinate {

    final int id;

    public Entity(final int id, final int x, final int y) {
        super(x, y);
        this.id = id;
    }

}

class CannonBall extends Entity {
    final int targetX;
    final int targetY;
    final int firedBy;
    int turnsToHitTarget;

    public CannonBall(final int x, final int y, final int firedBy, final int turnsToHitTarget, int id) {
        //todo: set the x and y correctly, if required
        super(id, -1, -1);
        this.targetX = x;
        this.targetY = y;
        this.firedBy = firedBy;
        this.turnsToHitTarget = turnsToHitTarget;
    }

    public static int findTimeToReach(final Coordinate currentPosition, final Coordinate target) {
        return (int) (Math.round(1 + (currentPosition.calculateDistance(target)) / 3.0));
    }
}

class Barrel extends Entity {
    final int rum;

    public Barrel(final int rum, final int x, final int y, int id) {
        super(id, x, y);
        this.rum = rum;
    }
}

class Ship extends Entity {
    public static final int MAX_MINE_DISTANCE = 5;
    public static final int MAX_CANNON_TIME = 4;
    int rotation;
    int speed;
    int rum;
    final int player;
    private static final int oddMovement[][] = new int[][]{{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}};
    private static final int evenMovement[][] = new int[][]{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    private static final int movement[][] = new int[][]{{2, 0}, {1, -2}, {-1, -2}, {-2, 0}, {-1, 2}, {1, 2}};

    public Ship(final int rotation, final int speed, final int rum, final int player, final int x, final int y, int id) {
        super(id, x, y);
        this.rotation = rotation;
        this.speed = speed;
        this.rum = rum;
        this.player = player;
    }

    public Coordinate shoot(final Ship ship) {
        if (ship.player == player) {
            throw new RuntimeException("Don't fire at friendlies, retard!");
        } else {
            for (int time = 0; time < 4; time++) {
                final int predictedX = ship.x + (time * ship.speed * movement[ship.rotation][0]) / 2;
                final int predictedY = ship.y + (time * ship.speed * movement[ship.rotation][1]) / 2;
                final Coordinate prediction = new Coordinate(predictedX, predictedY);
                final int movement[] = x % 2 == 0 ? evenMovement[rotation] : oddMovement[rotation];
                if (calculateDistance(prediction) <= 10 && CannonBall.findTimeToReach(new Coordinate(x + movement[0], y + movement[1]), prediction) == time) {
                    return prediction;
                }
            }
            return null;
        }
    }

    public Mine willCollide(final List<Mine> mines) {
        if (mines.isEmpty()) {
            return null;
        }
        Coordinate current = this;
        for (int i = 0; i <= MAX_MINE_DISTANCE; i++) {
            for (final Mine mine : mines) {
                if (isAHit(current, mine)) {
                    return mine;
                }
            }
            final int[] movement = current.x % 2 == 0 ? evenMovement[rotation] : oddMovement[rotation];
            final Coordinate newPosition = new Coordinate(current.x + speed * movement[0], current.y + speed * movement[1]);
            if (!newPosition.isOutSideMap()) {
                current = newPosition;
            }
        }
        return null;
    }

    private boolean isAHit(final Coordinate current, final Coordinate bomb) {
        final int[] front = current.x % 2 == 0 ? evenMovement[rotation] : oddMovement[rotation];
        final int halfway = (rotation + 3) % 6;
        final int[] back = current.x % 2 == 0 ? evenMovement[halfway] : oddMovement[halfway];
        return current.equals(bomb) || bomb.equals(new Coordinate(current.x + front[0], current.y + front[1])) || bomb.equals(new Coordinate(current.x + back[0], current.y + back[1]));
    }

    public CannonBall willHitOnTime(final List<CannonBall> cannonBalls) {
        if (cannonBalls.isEmpty()) {
            return null;
        }
        Coordinate current = this;
        for (int i = 0; i <= MAX_CANNON_TIME; i++) {
            for (final CannonBall cannonBall : cannonBalls) {
                if (cannonBall.turnsToHitTarget == i + 1 && isAHit(current, new Coordinate(cannonBall.targetX, cannonBall.targetY))) {
                    return cannonBall;
                }
            }
            final int[] movement = current.x % 2 == 0 ? evenMovement[rotation] : oddMovement[rotation];
            final Coordinate newPosition = new Coordinate(current.x + speed * movement[0], current.y + speed * movement[1]);
            if (!newPosition.isOutSideMap()) {
                current = newPosition;
            }
        }
        return null;
    }
}

class Mine extends Entity {

    public Mine(final int x, final int y, final int id) {
        super(id, x, y);
    }
}