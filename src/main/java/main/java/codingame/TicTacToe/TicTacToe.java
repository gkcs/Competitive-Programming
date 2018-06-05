package main.java.codingame.TicTacToe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class TicTacToe {
    public static void main(String args[]) throws IOException {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        final LargeBoard largeBoard = new LargeBoard();
        final MCTS algorithm = new MCTS(largeBoard);
        while (true) {
            String line[] = in.readLine().split(" ");
            final int opponentRow = Integer.parseInt(line[0]), opponentCol = Integer.parseInt(line[1]);
            if (opponentCol >= 0) {
                largeBoard.play(2, opponentRow * 9 + opponentCol);
                System.err.println(largeBoard);
            }
            final int validActionCount = Integer.parseInt(in.readLine());
            int bRow = 0, bCol = 0;
            for (int i = 0; i < validActionCount; i++) {
                line = in.readLine().split(" ");
                bRow = Integer.parseInt(line[0]);
                bCol = Integer.parseInt(line[1]);
            }
            final int bestMove = algorithm.suggestMove();
            final int row = bestMove / 3, col = bestMove % 3;
            largeBoard.play(1, bestMove);
            System.out.println(((bRow / 3) * 3 + row) + " " + ((bCol / 3) * 3 + col));
            System.err.println(largeBoard);
        }
    }
}

class MCTS {
    public static final int TIME_OUT = 200;
    public static final double CONSTANT = 10000d;
    private final TreeNode root = new TreeNode(-1, null, 1);

    public int suggestMove() {
        return root.getChildren()
                .stream()
                .max(Comparator.comparingDouble(node -> node.wins / (double) node.plays + node.plays / CONSTANT))
                .map(c -> c.col)
                .orElse(0);
    }

    public MCTS(final LargeBoard board) {
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime <= TIME_OUT) {
            TreeNode current = root;
            int position = current.selectChild(board);
            int player = 1;
            while (current.getChild(position) != null) {
                current = current.getChild(position);
                board.play(player, position);
                position = current.selectChild(board);
                player = player == 1 ? 2 : 1;
                board.undo();
            }
            if (board.canPlay(position)) {
                current.expand(board, position);
            } else {
                break;
            }
        }
        System.err.println(root);
    }
}

class TreeNode {
    private static final Random random = new Random();
    public static final int SIMULATION_CONSTANT = 50;
    public final int col;
    public int plays;
    public double wins;
    private TreeNode parent;
    private final int player;
    private Map<Integer, TreeNode> children = new HashMap<>();

    public TreeNode(final int col, final TreeNode parent, final int player) {
        this.col = col;
        this.parent = parent;
        this.player = player;
    }

    public int selectChild(final LargeBoard board) {
        final Optional<TreeNode> child = children.values()
                .stream()
                .max(Comparator.comparingDouble(TreeNode::getUtility));
        double maxUtility = child.map(TreeNode::getUtility).orElse(0d);
        int bestColumn = child.map(c -> c.col).orElse(0);
        final Set<Integer> expandedSet = children.keySet();
        for (int i = 0; i < 81; i++) {
            if (board.canPlay(i) && !expandedSet.contains(i)) {
                final double utility = Math.sqrt(Math.log(plays + 1)) + (0.05 / Math.abs(9 / 2.0 - i));
                if (utility > maxUtility) {
                    maxUtility = utility;
                    bestColumn = i;
                }
            }
        }
        return bestColumn;
    }

    private double getUtility() {
        return (player == 1 ? wins : (plays - wins)) / (double) plays + Math.sqrt(Math.log(parent.plays) / plays);
    }

    private double simulate(final LargeBoard board, int player) {
        int numberOfMovesPlayed = board.movesPlayed;
        while (board.result() == -1) {
            final int possibilities[] = new int[81];
            int movesToPlay = 0;
            for (int position = 0; position < possibilities.length; position++) {
                if (board.canPlay(position)) {
                    possibilities[movesToPlay] = position;
                    movesToPlay++;
                    final int result = board.result();
                    if (result != -1) {
                        return result == player ? 1 : (result == 0 ? 0.5 : 0);
                    }
                }
            }
            board.play(player, possibilities[random.nextInt(movesToPlay)]);
            player = player == 1 ? 2 : 1;
        }
        while (board.movesPlayed > numberOfMovesPlayed) {
            board.undo();
        }
        return 0.5;
    }

    public void backPropagate(final TreeNode node) {
        TreeNode current = this;
        while (current != null) {
            current.plays += node.plays;
            current.wins += node.wins;
            current = current.parent;
        }
    }

    public void expand(final LargeBoard board, final int position) {
        final TreeNode child = new TreeNode(position, this, player == 1 ? 2 : 1);
        board.play(player, position);
        for (int i = 0; i < SIMULATION_CONSTANT; i++) {
            child.wins = child.wins + child.simulate(board, player);
            child.plays++;
        }
        board.undo();
        children.put(position, child);
        backPropagate(child);
    }

    public Collection<TreeNode> getChildren() {
        return children.values();
    }

    public TreeNode getChild(final int col) {
        return children.get(col);
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "col=" + col +
                ", plays=" + plays +
                ", wins=" + wins +
                ", parent=" + (parent == null ? -1 : parent.col) +
                ", player=" + player +
                ", children=" + children.values()
                .stream()
                .map(c -> "COL: " + c.col + " WINS: " + c.wins + " PLAYS: " + c.plays + "\n")
                .collect(Collectors.joining(",")) +
                '}';
    }
}

class LargeBoard {
    public static final int FULL = (1 << 10) - 1;
    int movesPlayed;
    int largeBoard, largeCaptures, largeOccupied;
    final int moves[] = new int[81];
    final Board boards[] = new Board[9];

    public LargeBoard() {
        for (int i = 0; i < 9; i++) {
            boards[i] = new Board();
        }
    }

    public void play(final int player, final int p) {
        moves[movesPlayed] = p;
        final int bRow = p / 27, bCol = (p % 9) / 3;
        final int row = (p / 9) % 3, col = p % 3;
        if (movesPlayed > 0) {
            final int previousMove = moves[movesPlayed - 1];
            final int pRow = previousMove / 27, pCol = (previousMove % 9) / 3;
            assert (largeOccupied & (1 << (pRow * 3 + pCol))) != 0 || (bRow == pRow && bCol == pCol);
        }
        final int position = bRow * 3 + bCol;
        assert (largeOccupied & (1 << position)) == 0;
        boards[position].play(player, row * 3 + col);
        if (boards[position].occupied == FULL) {
            largeOccupied = largeOccupied | (1 << position);
        }
        movesPlayed++;
    }

    public void undo() {
        movesPlayed--;
        final int p = moves[movesPlayed];
        final int row = (p / 9) % 3, col = p % 3;
        final int bRow = p / 27, bCol = (p % 9) / 3;
        boards[bRow * 3 + bCol].undo(row * 3 + col);
        final int bitFlipped = ~(1 << (bRow * 3 + bCol));
        largeBoard = largeBoard & bitFlipped;
        largeCaptures = largeCaptures & bitFlipped;
        largeOccupied = largeOccupied & bitFlipped;
    }

    public int result() {
        int firstScore = 0, secondScore = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int position = i * 3 + j;
                final int bit = 1 << position;
                if (boards[position].result(1) == 1) {
                    largeBoard = largeBoard | bit;
                    firstScore++;
                    largeCaptures = largeCaptures | bit;
                    largeOccupied = largeOccupied | bit;
                } else if (boards[position].result(2) == 2) {
                    secondScore++;
                    largeCaptures = largeCaptures | bit;
                    largeOccupied = largeOccupied | bit;
                } else if (boards[position].occupied == FULL) {
                    largeOccupied = largeOccupied | bit;
                }
            }
        }
        if (firstScore > 4) {
            return 1;
        } else if (secondScore > 4) {
            return 2;
        } else if (Board.evaluateBoard(1, largeBoard, largeCaptures) == 1) {
            return 1;
        } else if (Board.evaluateBoard(2, largeBoard, largeCaptures) == 2) {
            return 2;
        } else if (largeOccupied == FULL) {
            return firstScore > secondScore ? 1 : (secondScore > firstScore ? 2 : 0);
        } else {
            return -1;
        }
    }

    public boolean canPlay(final int p) {
        final int bRow = p / 27, bCol = (p % 9) / 3;
        final int row = (p / 9) % 3, col = p % 3;
        if (movesPlayed > 0) {
            final int previousMove = moves[movesPlayed - 1];
            final int pRow = previousMove / 27, pCol = (previousMove % 9) / 3;
            if (!((largeOccupied & (1 << (pRow * 3 + pCol))) != 0 || (bRow == pRow && bCol == pCol))) {
                return false;
            }
        }
        return ((largeOccupied & (1 << (bRow * 3 + bCol))) == 0)
                && (boards[bRow * 3 + bCol].occupied & (1 << (row * 3 + col))) == 0;
    }

    @Override
    public String toString() {
        return "LargeBoard{" +
                "largeBoard=" + largeBoard +
                ", largeCaptures=" + largeCaptures +
                ", boards=" + Arrays.deepToString(boards) +
                '}';
    }
}

class Board {
    int occupied;
    int board;
    int decided;
    static final int winningStates[] = new int[]{
            0b111_000_000,
            0b100_100_100,
            0b100_010_001,
            0b010_010_010,
            0b001_010_100,
            0b001_001_001,
            0b000_111_000,
            0b000_000_111
    };

    public void play(final int player, final int p) {
        final int bit = 1 << p;
        assert (occupied & bit) == 0;
        if (player == 1) {
            board = board | bit;
        }
        occupied = occupied | bit;
    }

    public void undo(final int p) {
        final int bit = 1 << p;
        assert (occupied & bit) != 0;
        board = board & (~bit);
        occupied = occupied & (~bit);
        decided = 0;
    }

    public int result(final int player) {
        return decided = (decided != 0 ? decided : evaluateBoard(player, board, occupied));
    }

    public static int evaluateBoard(final int player, int board, int occupied) {
        final int boardForPlayer = player == 1 ? board : ~board;
        final int effectiveBoard = boardForPlayer & occupied;
        for (final int winningState : winningStates) {
            if (effectiveBoard >= winningState) {
                if (winningState == (effectiveBoard & winningState)) {
                    return player;
                }
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Occupied:" + Integer.toBinaryString(occupied) + "\nBoard:" + Integer.toBinaryString(board);
    }
}