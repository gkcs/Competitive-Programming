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
        final MCTS algorithm = new MCTS();
        algorithm.construct(largeBoard, 1);
        while (true) {
            String line[] = in.readLine().split(" ");
            final int opponentRow = Integer.parseInt(line[0]), opponentCol = Integer.parseInt(line[1]);
            if (opponentCol >= 0) {
                final int opponentMove = opponentRow * 9 + opponentCol;
                if (algorithm.root.getChild(opponentMove) == null) {
                    algorithm.root.expand(largeBoard, opponentMove);
                }
                //todo: Fails sometimes? Occupied is full then
                largeBoard.play(2, opponentMove);
                algorithm.root = algorithm.root.getChild(opponentMove);
                algorithm.root.parent = null;
                algorithm.construct(largeBoard, 1);
                System.err.println(largeBoard);
            }
            final int validActionCount = Integer.parseInt(in.readLine());
            for (int i = 0; i < validActionCount; i++) {
                in.readLine();
            }
            final int bestMove = algorithm.suggestMove();
            final int row = bestMove / 9, col = bestMove % 9;
            System.out.println(row + " " + col);
            largeBoard.play(1, bestMove);
            algorithm.root = algorithm.root.getChild(bestMove);
            algorithm.root.parent = null;
            algorithm.construct(largeBoard, 2);
            System.err.println(largeBoard);
        }
    }
}

//todo: write unit tests for each function
class MCTS {
    public static final int TIME_OUT = 50;
    public static final double CONSTANT = 10000d;
    TreeNode root = new TreeNode(-1, null, 1);

    public int suggestMove() {
        return root.getChildren()
                .stream()
                .max(Comparator.comparingDouble(node -> node.wins / (double) node.plays + node.plays / CONSTANT))
                .map(c -> c.move)
                .orElseThrow(() -> new RuntimeException("No moves to play!"));
    }

    public void construct(final LargeBoard board, int player) {
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime <= TIME_OUT) {
            TreeNode current = root;
            int position = current.selectChild(board);
            //todo: Why do you check for canPlay?
            while (board.canPlay(position) && current.getChild(position) != null) {
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
    public final int move;
    public int plays;
    public double wins;
    public TreeNode parent;
    private final int player;
    private Map<Integer, TreeNode> children = new HashMap<>();

    public TreeNode(final int move, final TreeNode parent, final int player) {
        this.move = move;
        this.parent = parent;
        this.player = player;
    }

    public int selectChild(final LargeBoard board) {
        final Optional<TreeNode> child = children.values()
                .stream()
                .max(Comparator.comparingDouble(TreeNode::getUtility));
        double maxUtility = child.map(TreeNode::getUtility).orElse(0d);
        int bestColumn = child.map(c -> c.move).orElse(0);
        final Set<Integer> expandedSet = children.keySet();
        final int currentBoardIndex = board.currentBoard();
        if (currentBoardIndex != -1) {
            int start = (currentBoardIndex / 3) * 27 + (currentBoardIndex % 3) * 3;
            for (int x = 0; x < 3; x++, start++) {
                for (int y = 0; y < 3; y++) {
                    int i = start + y * 9;
                    if (board.canPlay(i) && !expandedSet.contains(i)) {
                        final double utility = Math.sqrt(Math.log(plays + 1)) + (0.05 / Math.abs(40 - i));
                        if (utility > maxUtility) {
                            maxUtility = utility;
                            bestColumn = i;
                        }
                    }
                }
            }
        } else {
            for (int boardIndex = 0; boardIndex < 9; boardIndex++) {
                if ((board.largeOccupied & (1 << boardIndex)) == 0) {
                    int start = (boardIndex / 3) * 27 + (boardIndex % 3) * 3;
                    for (int x = 0; x < 3; x++, start++) {
                        for (int y = 0; y < 3; y++) {
                            int i = start + y * 9;
                            if (board.canPlay(i) && !expandedSet.contains(i)) {
                                final double utility = Math.sqrt(Math.log(plays + 1)) + (0.05 / Math.abs(40 - i));
                                if (utility > maxUtility) {
                                    maxUtility = utility;
                                    bestColumn = i;
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestColumn;
    }

    private double getUtility() {
        return (player == 1 ? wins : (plays - wins)) / (double) plays + Math.sqrt(Math.log(parent.plays) / plays);
    }

    private double simulate(final LargeBoard board, int player) {
        final int numberOfMovesPlayed = board.movesPlayed;
        final int originalPlayer = player;
        while (board.result() == -1) {
            int movesToPlay = 0;
            final int currentBoardIndex = board.currentBoard();
            final int possibilities[] = new int[currentBoardIndex != -1 ? 9 : 81];
            if (currentBoardIndex != -1) {
                int start = (currentBoardIndex / 3) * 27 + (currentBoardIndex % 3) * 3;
                for (int x = 0; x < 3; x++, start++) {
                    for (int y = 0; y < 3; y++) {
                        int position = start + y * 9;
                        if (board.canPlay(position)) {
                            possibilities[movesToPlay] = position;
                            movesToPlay++;
                        }
                    }
                }
            } else {
                for (int boardIndex = 0; boardIndex < 9; boardIndex++) {
                    if ((board.largeOccupied & (1 << boardIndex)) == 0) {
                        int start = (boardIndex / 3) * 27 + (boardIndex % 3) * 3;
                        for (int x = 0; x < 3; x++, start++) {
                            for (int y = 0; y < 3; y++) {
                                int position = start + y * 9;
                                if (board.canPlay(position)) {
                                    possibilities[movesToPlay] = position;
                                    movesToPlay++;
                                }
                            }
                        }
                    }
                }
            }
            if (movesToPlay == 0) {
                break;
            }
            board.play(player, possibilities[random.nextInt(movesToPlay)]);
            player = player == 1 ? 2 : 1;
        }
        final int r = board.result();
        final double result = r == originalPlayer ? 1 : (r == 0 ? 0.5 : 0);
        while (board.movesPlayed > numberOfMovesPlayed) {
            board.undo();
        }
        return result;
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
                "\nmove=" + move +
                ", \nplays=" + plays +
                ", \nwins=" + wins +
                ", \nparent=" + (parent == null ? -1 : parent.move) +
                ", \nplayer=" + player +
                ", \nchildren=" + children.values()
                .stream()
                .map(node -> "MOVE: " + node.move + " WINS: " + node.wins + " PLAYS: " + node.plays + "\n")
                .collect(Collectors.joining("\n")) +
                '}';
    }
}

class LargeBoard {
    public static final int FULL = (1 << 9) - 1;
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
            final int pMoveRow = (previousMove / 9) % 3, pMoveCol = previousMove % 3;
            if (!((largeOccupied & (1 << (pMoveRow * 3 + pMoveCol))) != 0 || (bRow == pMoveRow && bCol == pMoveCol))) {
                throw new RuntimeException();
            }
        }
        final int position = bRow * 3 + bCol;
        int bit = 1 << position;
        if ((largeOccupied & bit) != 0) {
            throw new RuntimeException();
        }
        boards[position].play(player, row * 3 + col);
        if (boards[position].result(player) == player) {
            if (player == 1) {
                largeBoard = largeBoard | bit;
            }
            largeCaptures = largeCaptures | bit;
            largeOccupied = largeOccupied | bit;
        } else if (boards[position].occupied == FULL) {
            largeOccupied = largeOccupied | bit;
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
        for (int position = 0; position < 9; position++) {
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
        if (p < 0) {
            return false;
        }
        final int bRow = p / 27, bCol = (p % 9) / 3;
        final int row = (p / 9) % 3, col = p % 3;
        if (movesPlayed > 0) {
            final int previousMove = moves[movesPlayed - 1];
            final int pMoveRow = (previousMove / 9) % 3, pMoveCol = previousMove % 3;
            if (!((largeOccupied & (1 << (pMoveRow * 3 + pMoveCol))) != 0 || (bRow == pMoveRow && bCol == pMoveCol))) {
                return false;
            }
        }
        return ((largeOccupied & (1 << (bRow * 3 + bCol))) == 0)
                && (boards[bRow * 3 + bCol].occupied & (1 << (row * 3 + col))) == 0;
    }

    public int currentBoard() {
        if (movesPlayed > 0) {
            final int previousMove = moves[movesPlayed - 1];
            final int pMoveRow = (previousMove / 9) % 3, pMoveCol = previousMove % 3;
            if ((largeOccupied & (1 << (pMoveRow * 3 + pMoveCol))) == 0) {
                return pMoveRow * 3 + pMoveCol;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "LargeBoard{" +
                "\nlargeBoard=" + Integer.toBinaryString(largeBoard) +
                ", \nlargeCaptures=" + Integer.toBinaryString(largeCaptures) +
                ", \nlargeOccupied=" + Integer.toBinaryString(largeOccupied) +
                ", \nmovesPlayed=" + movesPlayed +
                ", \nmoves=" + Arrays.toString(moves) +
                ", \nboards=" + Arrays.deepToString(boards) +
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
        if ((occupied & bit) != 0) {
            throw new RuntimeException();
        }
        if (player == 1) {
            board = board | bit;
        }
        occupied = occupied | bit;
    }

    public void undo(final int p) {
        final int bit = 1 << p;
        if ((occupied & bit) == 0) {
            throw new RuntimeException();
        }
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
        int i = 0;
        for (; i < winningStates.length; i++) {
            if (effectiveBoard >= winningStates[i]) {
                break;
            }
        }
        for (; i < winningStates.length; i++) {
            if (effectiveBoard >= winningStates[i]) {
                if (winningStates[i] == (effectiveBoard & winningStates[i])) {
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
        return "Occupied:" + Integer.toBinaryString(occupied) + " Board:" + Integer.toBinaryString(board) + "\n";
    }
}