package main.java.codingame.code4life;

import java.util.*;
import java.util.stream.Collectors;

public class Medicine {
    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        final int projectCount = in.nextInt();
        final List<Project> projects = new ArrayList<>(projectCount);
        for (int i = 0; i < projectCount; i++) {
            final int experience[] = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
            projects.add(new Project(experience));
        }
        int rounds = 0;
        while (true) {
            final Robot myBot = getRobot(in), opponentBot = getRobot(in);
            final int available[] = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
            final int sampleCount = in.nextInt();
            final Map<Integer, Sample> samples = new HashMap<>(sampleCount);
            for (int i = 0; i < sampleCount; i++) {
                final Sample sample = getSample(in);
                if (sample.material.cost[1] >= 0) {
                    sample.makeVisible(null);
                }
                if (sample.material.carriedBy == 0) {
                    myBot.samples[opponentBot.samplesLength++] = sample;
                } else if (sample.material.carriedBy == 1) {
                    opponentBot.samples[opponentBot.samplesLength++] = sample;
                } else {
                    samples.put(sample.material.sampleId, sample);
                }
            }
            rounds++;
            if (myBot.eta > 0) {
                System.out.println(Action.GOTO.name() + " " + myBot.target);
            } else if (!myBot.target.equals(Position.START_POS)) {
                System.out.println(new MCTS().findBestMove(new GameState(0, new Robot[]{myBot, opponentBot}, available, projects, samples, rounds)));
            } else {
                System.out.println(Action.GOTO.name() + " " + Position.SAMPLES.name());
            }
        }
    }

    private static Sample getSample(Scanner in) {
        final int sampleId = in.nextInt();
        final int carriedBy = in.nextInt();
        final int rank = in.nextInt() - 1;
        final String expertiseGain = in.next();
        final int health = in.nextInt();
        final int cost[] = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
        return new Sample(sampleId, carriedBy, rank, health, cost, expertiseGain);
    }

    private static Robot getRobot(Scanner in) {
        final String target = in.next();
        final int eta = in.nextInt();
        final int score = in.nextInt();
        final int storage[] = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()},
                expertise[] = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
        return new Robot(Position.valueOf(target), eta, score, storage, expertise);
    }
}

class MCTS {
    public static long MAX_COMPUTATIONS = 2;

    public String findBestMove(final GameState gameState) {
        final Node root = new Node(gameState.rounds, null, null, gameState);
        for (int i = 0; i < MAX_COMPUTATIONS; i++) {
            Node current = root;
            Node next = current.getNextChild();
            while (next != null) {
                current = next;
                next = current.getNextChild();
            }
            current.expand();
            for (final Node child : current.children) {
                for (int simulate = 0; simulate < 2; simulate++) {
                    current.propagate(child.simulation());
                }
                System.err.println("ITERATION: PROPAGATED CHILD " + child + " TO PARENT: " + current);
            }
        }
        return root.getRobustChild().getMove();
    }

    public static double evaluate(final GameState gameState) {
        final Robot first = gameState.robots[gameState.player];
        return first.score + (1 - gameState.rounds / 400.0) * (first.totalExpertise + 0.1 * first.totalMolecules);
    }

    public static int flip(final int player) {
        return player ^ 1;
    }
}

class Node {
    final int roundsTillNow;
    double totalScore;
    int plays;
    final String moveToGetHere;
    final Node parent;
    final GameState gameState;
    List<Node> children;
    private static final Random random = new Random();

    public Node(final int roundsTillNow, final Node parent, final String moveToGetHere, final GameState gameState) {
        this.roundsTillNow = roundsTillNow;
        this.parent = parent;
        this.moveToGetHere = moveToGetHere;
        this.gameState = gameState;
    }

    public Node getRobustChild() {
        return children.stream().max(Comparator.comparingInt(node -> node.plays)).orElseThrow(() -> new RuntimeException("No children here"));
    }

    public Node getNextChild() {
        if (children == null) {
            return null;
        } else {
            return children.stream()
                    .max(Comparator.comparingDouble(node -> node.totalScore / node.plays - Math.sqrt(2 * Math.log(plays) / node.plays)))
                    .orElseThrow(() -> new RuntimeException("No children here"));
        }
    }

    public void propagate(final double score) {
        totalScore += score;
        plays++;
        if (parent != null) {
            parent.propagate(score);
        }
    }

    public void expand() {
        children = getPossibleMoves(gameState)
                .entrySet()
                .stream()
                .map(entry -> new Node(roundsTillNow + 2, this, entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    public double simulation() {
        if (roundsTillNow >= 400) {
            return gameState.robots[0].score;
        } else {
            double score = 0;
            GameState currentState = gameState;
            int rounds = 0;
            while (rounds < 8 && roundsTillNow + rounds < 400) {
                final Map<GameState, String> possibleMoves = getPossibleMoves(currentState);
                int count = 0;
                int index = possibleMoves.size() == 0 ? 0 : random.nextInt(possibleMoves.size());
                for (final GameState possible : possibleMoves.keySet()) {
                    if (count == index) {
                        currentState = possible;
                        break;
                    }
                    count++;
                }
                rounds += 2;
            }
            totalScore += MCTS.evaluate(currentState);
            plays++;
            return score;
        }
    }

    public String getMove() {
        return moveToGetHere;
    }


    private Map<GameState, String> getPossibleMoves(final GameState gameState) {
        if (gameState.rounds == 400) {
            return Collections.singletonMap(gameState, Action.GOTO.name() + " " + Position.SAMPLES.name());
        }
        final Robot currentBot = gameState.robots[gameState.player];
        final Position currentLocation = gameState.robots[gameState.player].target;
        final Map<GameState, String> gameStates = new HashMap<>();
        boolean diagnosisLeft = false;
        for (int i = 0; i < currentBot.samplesLength; i++) {
            if (!currentBot.samples[i].isVisible()) {
                diagnosisLeft = true;
                break;
            }
        }
        switch (currentLocation) {
            case SAMPLES: {
                if (diagnosisLeft
                        || !gameState.samples.isEmpty()
                        || currentBot.samplesLength == 3) {
                    gameStates.put(gameState.play(Position.DIAGNOSIS), Action.GOTO.name() + " " + Position.DIAGNOSIS.name());
                }
                if (currentBot.samplesLength < 3) {
                    for (int rank = 0; rank < 3; rank++) {
                        gameStates.put(gameState.play(rank), Action.CONNECT.name() + " " + (rank + 1));
                    }
                }
                break;
            }
            case DIAGNOSIS: {
                if (currentBot.canMakeMedicine()) {
                    gameStates.put(gameState.play(Position.LABORATORY), Action.GOTO.name() + " " + Position.LABORATORY.name());
                }
                if (currentBot.totalMolecules < 10) {
                    gameStates.put(gameState.play(Position.MOLECULES), Action.GOTO.name() + " " + Position.MOLECULES.name());
                }
                if (currentBot.samplesLength < 3) {
                    gameStates.put(gameState.play(Position.SAMPLES), Action.GOTO.name() + " " + Position.SAMPLES.name());
                }
                for (int i = 0; i < currentBot.samplesLength; i++) {
                    if (!currentBot.samples[i].isVisible() || !currentBot.isPossible(currentBot.samples[i], gameState.availableMolecules)) {
                        gameStates.put(gameState.play(currentBot.samples[i].material.sampleId), Action.CONNECT.name()
                                + " " + currentBot.samples[i].material.sampleId);
                    }
                }
                if (currentBot.samplesLength < 3) {
                    for (final Integer id : gameState.samples.keySet()) {
                        if (currentBot.isPossible(gameState.samples.get(id), gameState.availableMolecules)) {
                            gameStates.put(gameState.play(id), Action.CONNECT.name() + " " + id);
                        }
                    }
                }
                break;
            }
            case MOLECULES: {
                if (currentBot.canMakeMedicine()) {
                    gameStates.put(gameState.play(Position.LABORATORY), Action.GOTO.name() + " " + Position.LABORATORY.name());
                }
                if (diagnosisLeft || !gameState.samples.isEmpty() || currentBot.samplesLength == 3) {
                    gameStates.put(gameState.play(Position.DIAGNOSIS), Action.GOTO.name() + " " + Position.DIAGNOSIS.name());
                }
                if (currentBot.samplesLength < 3) {
                    gameStates.put(gameState.play(Position.SAMPLES), Action.GOTO.name() + " " + Position.SAMPLES.name());
                }
                if (currentBot.totalMolecules < 10) {
                    for (int i = 0; i < gameState.availableMolecules.length; i++) {
                        if (gameState.availableMolecules[i] > 0) {
                            gameStates.put(gameState.play(i), Action.CONNECT.name() + " " + (char) ('A' + i));
                        }
                    }
                }
                break;
            }
            case LABORATORY: {
                if (diagnosisLeft || !gameState.samples.isEmpty() || currentBot.samplesLength == 3) {
                    gameStates.put(gameState.play(Position.DIAGNOSIS), Action.GOTO.name() + " " + Position.DIAGNOSIS.name());
                }
                if (currentBot.totalMolecules < 10) {
                    gameStates.put(gameState.play(Position.MOLECULES), Action.GOTO.name() + " " + Position.MOLECULES.name());
                }
                if (currentBot.samplesLength < 3) {
                    gameStates.put(gameState.play(Position.SAMPLES), Action.GOTO.name() + " " + Position.SAMPLES.name());
                }
                for (int i = 0; i < currentBot.samplesLength; i++) {
                    Sample sample = currentBot.samples[i];
                    if (sample.isVisible()) {
                        if (currentBot.isAdequate(sample)) {
                            gameStates.put(gameState.play(sample.material.sampleId), Action.CONNECT.name() + " " + sample.material.sampleId);
                        }
                    }
                }
                break;
            }
            default: {
                throw new RuntimeException("Where are ya?");
            }
        }
        return gameStates;
    }

    @Override
    public String toString() {
        return "Node{" +
                "roundsTillNow=" + roundsTillNow +
                ", totalScore=" + totalScore +
                ", plays=" + plays +
                ", moveToGetHere='" + moveToGetHere + '\'' +
                ", gameState=" + gameState +
                '}';
    }
}

class SampleMaterial {
    final int[] cost;
    final int health;
    final String expertise;

    public SampleMaterial(final int[] cost, final int health, final MoleculeType e) {
        this.cost = cost;
        this.health = health;
        this.expertise = String.valueOf((char) (e.index + 'A'));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SampleMaterial that = (SampleMaterial) o;
        return health == that.health && Arrays.equals(cost, that.cost) && expertise.equals(that.expertise);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Arrays.hashCode(cost) + health) + expertise.hashCode();
    }
}

enum MoleculeType {
    A(0), B(1), C(2), D(3), E(4);

    final int index;

    MoleculeType(int index) {
        this.index = index;
    }
}

class GameState {
    int player;
    final Robot[] robots;
    final int availableMolecules[];
    final List<Project> projects;
    final Map<Integer, Sample> samples;
    int totalMoleculesLeft;
    int rounds;
    private static final List<LinkedList<SampleMaterial>> remainingSampleMaterial = initSamplePool();
    private static final Random random = new Random();

    private static List<LinkedList<SampleMaterial>> initSamplePool() {
        final List<LinkedList<SampleMaterial>> l = Arrays.asList(new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        l.get(0).add(new SampleMaterial(new int[]{0, 3, 0, 0, 0}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 2, 1}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{0, 1, 1, 1, 1}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{0, 2, 0, 0, 2}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 4, 0, 0}, 10, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{0, 1, 2, 1, 1}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{0, 2, 2, 0, 1}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{3, 1, 0, 0, 1}, 1, MoleculeType.A));
        l.get(0).add(new SampleMaterial(new int[]{1, 0, 0, 0, 2}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 0, 3}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{1, 0, 1, 1, 1}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 2, 0, 2}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 4, 0}, 10, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{1, 0, 1, 2, 1}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{1, 0, 2, 2, 0}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{0, 1, 3, 1, 0}, 1, MoleculeType.B));
        l.get(0).add(new SampleMaterial(new int[]{2, 1, 0, 0, 0}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 3, 0}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{1, 1, 0, 1, 1}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{0, 2, 0, 2, 0}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 0, 0, 4}, 10, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{1, 1, 0, 1, 2}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{0, 1, 0, 2, 2}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{1, 3, 1, 0, 0}, 1, MoleculeType.C));
        l.get(0).add(new SampleMaterial(new int[]{0, 2, 1, 0, 0}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{3, 0, 0, 0, 0}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{1, 1, 1, 0, 1}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{2, 0, 0, 2, 0}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{4, 0, 0, 0, 0}, 10, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{2, 1, 1, 0, 1}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{2, 0, 1, 0, 2}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{1, 0, 0, 1, 3}, 1, MoleculeType.D));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 2, 1, 0}, 1, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 3, 0, 0}, 1, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{1, 1, 1, 1, 0}, 1, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{2, 0, 2, 0, 0}, 1, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{0, 4, 0, 0, 0}, 10, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{1, 2, 1, 1, 0}, 1, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{2, 2, 0, 1, 0}, 1, MoleculeType.E));
        l.get(0).add(new SampleMaterial(new int[]{0, 0, 1, 3, 1}, 1, MoleculeType.E));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 5, 0}, 20, MoleculeType.A));
        l.get(1).add(new SampleMaterial(new int[]{6, 0, 0, 0, 0}, 30, MoleculeType.A));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 3, 2, 2}, 10, MoleculeType.A));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 1, 4, 2}, 20, MoleculeType.A));
        l.get(1).add(new SampleMaterial(new int[]{2, 3, 0, 3, 0}, 10, MoleculeType.A));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 5, 3}, 20, MoleculeType.A));
        l.get(1).add(new SampleMaterial(new int[]{0, 5, 0, 0, 0}, 20, MoleculeType.B));
        l.get(1).add(new SampleMaterial(new int[]{0, 6, 0, 0, 0}, 30, MoleculeType.B));
        l.get(1).add(new SampleMaterial(new int[]{0, 2, 2, 3, 0}, 10, MoleculeType.B));
        l.get(1).add(new SampleMaterial(new int[]{2, 0, 0, 1, 4}, 20, MoleculeType.B));
        l.get(1).add(new SampleMaterial(new int[]{0, 2, 3, 0, 3}, 20, MoleculeType.B));
        l.get(1).add(new SampleMaterial(new int[]{5, 3, 0, 0, 0}, 20, MoleculeType.B));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 5, 0, 0}, 20, MoleculeType.C));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 6, 0, 0}, 30, MoleculeType.C));
        l.get(1).add(new SampleMaterial(new int[]{2, 3, 0, 0, 2}, 10, MoleculeType.C));
        l.get(1).add(new SampleMaterial(new int[]{3, 0, 2, 3, 0}, 10, MoleculeType.C));
        l.get(1).add(new SampleMaterial(new int[]{4, 2, 0, 0, 1}, 20, MoleculeType.C));
        l.get(1).add(new SampleMaterial(new int[]{0, 5, 3, 0, 0}, 20, MoleculeType.C));
        l.get(1).add(new SampleMaterial(new int[]{5, 0, 0, 0, 0}, 20, MoleculeType.D));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 6, 0}, 30, MoleculeType.D));
        l.get(1).add(new SampleMaterial(new int[]{2, 0, 0, 2, 3}, 10, MoleculeType.D));
        l.get(1).add(new SampleMaterial(new int[]{1, 4, 2, 0, 0}, 20, MoleculeType.D));
        l.get(1).add(new SampleMaterial(new int[]{0, 3, 0, 2, 3}, 10, MoleculeType.D));
        l.get(1).add(new SampleMaterial(new int[]{3, 0, 0, 0, 5}, 20, MoleculeType.D));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 0, 5}, 20, MoleculeType.E));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 0, 0, 6}, 30, MoleculeType.E));
        l.get(1).add(new SampleMaterial(new int[]{3, 2, 2, 0, 0}, 10, MoleculeType.E));
        l.get(1).add(new SampleMaterial(new int[]{0, 1, 4, 2, 0}, 20, MoleculeType.E));
        l.get(1).add(new SampleMaterial(new int[]{3, 0, 3, 0, 2}, 10, MoleculeType.E));
        l.get(1).add(new SampleMaterial(new int[]{0, 0, 5, 3, 0}, 20, MoleculeType.E));
        l.get(2).add(new SampleMaterial(new int[]{0, 0, 0, 0, 7}, 40, MoleculeType.A));
        l.get(2).add(new SampleMaterial(new int[]{3, 0, 0, 0, 7}, 50, MoleculeType.A));
        l.get(2).add(new SampleMaterial(new int[]{3, 0, 0, 3, 6}, 40, MoleculeType.A));
        l.get(2).add(new SampleMaterial(new int[]{0, 3, 3, 5, 3}, 30, MoleculeType.A));
        l.get(2).add(new SampleMaterial(new int[]{7, 0, 0, 0, 0}, 40, MoleculeType.B));
        l.get(2).add(new SampleMaterial(new int[]{7, 3, 0, 0, 0}, 50, MoleculeType.B));
        l.get(2).add(new SampleMaterial(new int[]{6, 3, 0, 0, 3}, 40, MoleculeType.B));
        l.get(2).add(new SampleMaterial(new int[]{3, 0, 3, 3, 5}, 30, MoleculeType.B));
        l.get(2).add(new SampleMaterial(new int[]{0, 7, 0, 0, 0}, 40, MoleculeType.C));
        l.get(2).add(new SampleMaterial(new int[]{0, 7, 3, 0, 0}, 50, MoleculeType.C));
        l.get(2).add(new SampleMaterial(new int[]{3, 6, 3, 0, 0}, 40, MoleculeType.C));
        l.get(2).add(new SampleMaterial(new int[]{5, 3, 0, 3, 3}, 30, MoleculeType.C));
        l.get(2).add(new SampleMaterial(new int[]{0, 0, 7, 0, 0}, 40, MoleculeType.D));
        l.get(2).add(new SampleMaterial(new int[]{0, 0, 7, 3, 0}, 50, MoleculeType.D));
        l.get(2).add(new SampleMaterial(new int[]{0, 3, 6, 3, 0}, 40, MoleculeType.D));
        l.get(2).add(new SampleMaterial(new int[]{3, 5, 3, 0, 3}, 30, MoleculeType.D));
        l.get(2).add(new SampleMaterial(new int[]{0, 0, 0, 7, 0}, 40, MoleculeType.E));
        l.get(2).add(new SampleMaterial(new int[]{0, 0, 0, 7, 3}, 50, MoleculeType.E));
        l.get(2).add(new SampleMaterial(new int[]{0, 0, 3, 6, 3}, 40, MoleculeType.E));
        l.get(2).add(new SampleMaterial(new int[]{3, 3, 5, 3, 0}, 30, MoleculeType.E));
        Collections.shuffle(l);
        return l;
    }

    public static final int distances[][] = {
            {0, 3, 3, 3},
            {3, 0, 3, 4},
            {3, 3, 0, 3},
            {3, 4, 3, 0}
    };

    public GameState(final int player,
                     final Robot[] robots,
                     final int[] availableMolecules,
                     final List<Project> projects,
                     final Map<Integer, Sample> samples,
                     final int rounds) {
        this.player = player;
        this.robots = robots;
        this.availableMolecules = availableMolecules;
        this.projects = projects;
        this.samples = samples;
        this.totalMoleculesLeft = Arrays.stream(availableMolecules).sum();
        this.rounds = rounds;
    }

    public GameState play(final Position position) {
        GameState clone = clone();
        clone.updateBoard(position);
        return clone;
    }

    public GameState play(final int id) {
        GameState clone = clone();
        clone.updateBoard(id);
        return clone;
    }

    private void updateBoard(final int id) {
        final Robot currentBot = robots[player];
        switch (robots[player].target) {
            case SAMPLES: {
                final LinkedList<SampleMaterial> sampleMaterials = remainingSampleMaterial.get(id);
                final Sample sample = new Sample(sampleMaterials.get(random.nextInt(sampleMaterials.size())), id, player);
                currentBot.samples[currentBot.samplesLength++] = sample;
                break;
            }
            case DIAGNOSIS: {
                final int sampleIndex = currentBot.getSample(id);
                if (sampleIndex < 0) {
                    if (samples.containsKey(id)) {
                        currentBot.samples[currentBot.samplesLength++] = samples.remove(id);
                    } else {
                        System.err.println("No such sample" + id);
                    }
                } else if (currentBot.samples[sampleIndex].isVisible()) {
                    samples.put(id, currentBot.samples[sampleIndex]);
                    currentBot.samplesLength--;
                    currentBot.samples[sampleIndex] = currentBot.samples[currentBot.samplesLength];
                } else {
                    currentBot.samples[sampleIndex].makeVisible(remainingSampleMaterial.get(currentBot.samples[sampleIndex].material.rank));
                }
                break;
            }
            case MOLECULES: {
                if (currentBot.totalMolecules >= 10) {
                    throw new RuntimeException("Kitna uthayega be?");
                } else {
                    availableMolecules[id]--;
                    currentBot.storage[id]++;
                    currentBot.totalMolecules++;
                }
                break;
            }
            case LABORATORY: {
                final int sampleIndex = currentBot.getSample(id);
                final Sample sample = currentBot.samples[sampleIndex];
                currentBot.samplesLength--;
                currentBot.samples[sampleIndex] = currentBot.samples[currentBot.samplesLength];
                if (sampleIndex < 0) {
                    throw new RuntimeException("Make what?");
                } else {
                    for (int i = 0; i < sample.material.cost[i]; i++) {
                        final int payment = Math.max(0, sample.material.cost[i] - currentBot.expertise[i]);
                        if (currentBot.storage[i] < payment) {
                            throw new RuntimeException("Insufficient Molecules");
                        }
                        currentBot.storage[i] -= payment;
                        currentBot.totalMolecules -= payment;
                        availableMolecules[i] += payment;
                    }
                    currentBot.score += sample.material.health;
                    currentBot.expertise[sample.material.expertiseGain.charAt(0) - 'A']++;
                    for (int i = 0; i < projects.size(); i++) {
                        boolean projectCompleted = true;
                        for (int j = 0; j < projects.get(i).experience.length; j++) {
                            if (currentBot.expertise[j] < projects.get(i).experience[j]) {
                                projectCompleted = false;
                                break;
                            }
                        }
                        if (projectCompleted) {
                            projects.remove(i);
                            currentBot.score += 30;
                        }
                    }
                }
                break;
            }
            default: {
                throw new RuntimeException("Where is this place?");
            }
        }
        rounds++;
//        player = MCTS.flip(player);
    }

    private void updateBoard(final Position position) {
        if (!robots[player].target.equals(position)) {
            rounds += distances[robots[player].target.index][position.index];
            robots[player].target = position;
        }
//        player = MCTS.flip(player);
    }

    @Override
    public GameState clone() {
        final Robot[] cloneBots = new Robot[robots.length];
        cloneBots[0] = robots[0].clone();
        final Map<Integer, Sample> cloneSamples = new HashMap<>();
        for (Integer id : samples.keySet()) {
            cloneSamples.put(id, samples.get(id).clone());
        }
        return new GameState(player, cloneBots,
                Arrays.copyOf(availableMolecules, availableMolecules.length),
                new ArrayList<>(projects), cloneSamples, rounds);
    }

    @Override
    public String toString() {
        return "GameState{" +
                "player=" + player +
                ", robots=" + Arrays.toString(robots) +
                ", availableMolecules=" + Arrays.toString(availableMolecules) +
                ", projects=" + projects +
                ", samples=" + samples +
                ", totalMoleculesLeft=" + totalMoleculesLeft +
                '}';
    }
}

class Project {
    final int experience[];

    public Project(final int[] experience) {
        this.experience = experience;
    }
}

class Sample {
    final Material material;
    private static final Random random = new Random();
    private boolean visible = false;

    public Sample(final int sampleId,
                  final int carriedBy,
                  final int rank,
                  final int health,
                  final int[] cost,
                  final String expertiseGain) {
        this.material = new Material(sampleId, carriedBy, rank, health, cost, expertiseGain);
    }

    public Sample(final SampleMaterial material, final int rank, final int player) {
        this.material = new Material(random.nextInt(100) + 100, player, rank, material.health, material.cost, material.expertise);
    }

    public Sample(final Material material) {
        this.material = material;
    }

    public void makeVisible(final LinkedList<SampleMaterial> remainingSampleMaterial) {
        if (material.cost[1] < 0) {
            SampleMaterial pop = remainingSampleMaterial.get(random.nextInt(remainingSampleMaterial.size()));
            material.health = pop.health;
            System.arraycopy(pop.cost, 0, material.cost, 0, pop.cost.length);
            material.expertiseGain = pop.expertise;
        }
        this.visible = true;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    protected Sample clone() {
        final Sample sample = new Sample(material);
        sample.visible = this.visible;
        return sample;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Sample sample = (Sample) o;
        return visible == sample.visible && material.equals(sample.material);
    }

    @Override
    public String toString() {
        return "Sample{" +
                "material=" + material +
                ", visible=" + visible +
                '}';
    }

    @Override
    public int hashCode() {
        return 31 * material.hashCode() + (visible ? 1 : 0);
    }
}

class Material {
    final int sampleId, carriedBy, rank;
    int health;
    final int cost[];
    String expertiseGain;

    public Material(final int sampleId,
                    final int carriedBy,
                    final int rank,
                    final int health,
                    final int[] cost,
                    final String expertiseGain) {
        this.sampleId = sampleId;
        this.carriedBy = carriedBy;
        this.rank = rank;
        this.health = health;
        this.cost = cost;
        this.expertiseGain = expertiseGain;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "sampleId=" + sampleId +
                ", carriedBy=" + carriedBy +
                ", rank=" + rank +
                ", health=" + health +
                ", cost=" + Arrays.toString(cost) +
                ", expertiseGain='" + expertiseGain + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && sampleId == ((Material) o).sampleId;
    }

    @Override
    public int hashCode() {
        return sampleId;
    }
}

enum Action {
    GOTO, CONNECT
}

enum Position {
    START_POS(-1),
    SAMPLES(0),
    DIAGNOSIS(1),
    MOLECULES(2),
    LABORATORY(3);

    final int index;

    Position(final int index) {

        this.index = index;
    }
}

class Robot {
    Position target;
    int eta, score;
    final int[] storage, expertise;
    int totalMolecules;
    final Sample[] samples = new Sample[3];
    int totalExpertise;
    int samplesLength;

    public Robot(final Position target,
                 final int eta,
                 final int score,
                 final int[] storage,
                 final int[] expertise) {
        this.target = target;
        this.eta = eta;
        this.score = score;
        this.storage = storage;
        this.expertise = expertise;
        this.totalMolecules = Arrays.stream(storage).sum();
        this.totalExpertise = Arrays.stream(expertise).sum();
    }

    @Override
    public String toString() {
        return "Robot{" +
                "target='" + target + '\'' +
                ", eta=" + eta +
                ", score=" + score +
                ", storage=" + Arrays.toString(storage) +
                ", expertise=" + Arrays.toString(expertise) +
                '}';
    }

    public boolean canMakeMedicine() {
        for (int i = 0; i < samplesLength; i++) {
            if (samples[i].isVisible()) {
                if (isAdequate(samples[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAdequate(final Sample sample) {
        for (int i = 0; i < storage.length; i++) {
            if (storage[i] + expertise[i] < sample.material.cost[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isPossible(final Sample sample, final int available[]) {
        for (int i = 0; i < storage.length; i++) {
            if (storage[i] + expertise[i] + available[i] < sample.material.cost[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Robot clone() {
        final Robot robot = new Robot(target, eta, score, Arrays.copyOf(storage, storage.length), Arrays.copyOf(expertise, expertise.length));
        for (int i = 0; i < samplesLength; i++) {
            robot.samples[i] = samples[i].clone();
        }
        robot.samplesLength = samplesLength;
        return robot;
    }

    public int getSample(final int id) {
        for (int i = 0; i < samplesLength; i++) {
            if (samples[i].material.sampleId == id) {
                return i;
            }
        }
        return -1;
    }
}