package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Connect4 {
    public static void main(String[] args) throws IOException {
        final Connect4Board board = new Connect4Board();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String input;
        do {
            final MCTS algorithm = new MCTS();
            algorithm.constructTree(board);
            final int myMove = algorithm.suggestMove();
            System.out.println("MOVE: " + myMove);
            System.out.println(board.prettyPrint());
            board.play(1, myMove);
            System.out.println(board.prettyPrint());
            input = bufferedReader.readLine();
            board.play(2, Integer.parseInt(input));
            System.out.print(board.prettyPrint());
        }
        while (!input.equalsIgnoreCase("q"));
    }
}

class MCTS {
    private final TreeNode root = new TreeNode(-1, null, 1);

    public int suggestMove() {
        return root.getChildren()
                .stream()
                .max(Comparator.comparingDouble(node -> node.wins / (double) node.plays + node.plays / 10000d))
                .map(c -> c.col)
                .orElse(0);
    }

    public void constructTree(final Connect4Board original) {
        final long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime <= 2000) {
            TreeNode current = root;
            final Connect4Board board = original.copy();
            int col = current.selectChild(board);
            int player = 1;
            while (current.getChild(col) != null) {
                current = current.getChild(col);
                board.play(player, col);
                col = current.selectChild(board);
                player = player == 1 ? 2 : 1;
            }
            if (board.canPlay(col) == -1) {
                break;
            } else {
                current.expand(board, col);
            }
        }
        System.out.println(root);
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

    public int selectChild(final Connect4Board board) {
        final Optional<TreeNode> child = children.values()
                .stream()
                .max(Comparator.comparingDouble(TreeNode::getUtility));
        double maxUtility = child.map(TreeNode::getUtility).orElse(0d);
        int bestColumn = child.map(c -> c.col).orElse(0);
        final Set<Integer> expandedSet = children.keySet();
        for (int i = 0; i < Connect4Board.COLS; i++) {
            if (board.canPlay(i) != -1 && !expandedSet.contains(i)) {
                final double utility = Math.sqrt(Math.log(plays + 1)) + (0.05 / Math.abs(Connect4Board.COLS / 2.0 - i));
                if (utility > maxUtility) {
                    maxUtility = utility;
                    bestColumn = i;
                }
            }
        }
        return bestColumn;
    }

    private double getUtility() {
        return (player == 1 ? wins : (plays - wins)) / (double) plays + Math.sqrt(Math.log(parent.plays) / plays) + (0.05 / Math.abs(Connect4Board.COLS / 2.0 - col)) / ((double) plays + 1);
    }

    private double simulate(final Connect4Board board, int player) {
        while (!board.isFull()) {
            final int possibilities[] = new int[Connect4Board.COLS];
            int movesToPlay = 0;
            for (int col = 0; col < Connect4Board.COLS; col++) {
                if (board.canPlay(col) != -1) {
                    possibilities[movesToPlay] = col;
                    movesToPlay++;
                    if (board.isWin(player, col)) {
                        return player == 1 ? 1 : 0;
                    }
                }
            }
            board.play(player, possibilities[random.nextInt(movesToPlay)]);
            player = player == 1 ? 2 : 1;
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

    public void expand(final Connect4Board board, final int col) {
        final TreeNode child = new TreeNode(col, this, player == 1 ? 2 : 1);
        board.play(player, col);
        for (int i = 0; i < SIMULATION_CONSTANT; i++) {
            child.wins = child.wins + child.simulate(board.copy(), player);
            child.plays++;
        }
        children.put(col, child);
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

class Connect4Board {
    public static byte ROWS = 6, COLS = 7;
    private int coins = 0;
    private byte[][] board = new byte[ROWS][COLS];
    private byte[] heights = new byte[COLS];

    public void play(final int player, final int col) {
        final int row = canPlay(col);
        if (row == -1) {
            throw new IllegalArgumentException();
        }
        board[row][col] = (byte) player;
        heights[col]++;
        coins++;
    }

    public int canPlay(final int col) {
        final int row = heights[col];
        if (row >= ROWS || col > COLS) {
            return -1;
        }
        return row;
    }

    public boolean isFull() {
        return coins == ROWS * COLS;
    }

    public boolean isWin(final int player, final int col) {
        final int row = canPlay(col);
        if (row == -1) {
            throw new IllegalArgumentException();
        }
        return checkColumn(player, row, col)
                || checkRow(player, board[row], col)
                || checkDiagonal(player, row, col)
                || checkReverseDiagonal(player, row, col);
    }

    private boolean checkColumn(final int player, final int row, final int col) {
        int colLength = 1;
        for (; colLength < 4 && row - colLength >= 0 && (board[row - colLength][col] == player); colLength++) ;
        return colLength >= 4;
    }

    private boolean checkRow(final int player, final byte[] row, final int col) {
        int rowLength = 1;
        for (int i = 1; i < 4 && col - i >= 0; i++) {
            if (row[col - i] == player) {
                rowLength++;
            } else {
                break;
            }
        }
        for (int i = 1; i < 4 && col + i < COLS; i++) {
            if (row[col + i] == player) {
                rowLength++;
            } else {
                break;
            }
        }
        return rowLength >= 4;
    }

    private boolean checkReverseDiagonal(final int player, final int row, final int col) {
        int reverse = 1;
        for (int i = row - 1, j = col + 1; i >= 0 && j < COLS && reverse < 4; i--, j++) {
            if (board[i][j] == player) {
                reverse++;
            } else {
                break;
            }
        }
        for (int i = row + 1, j = col - 1; i < ROWS && j >= 0 && reverse < 4; i++, j--) {
            if (board[i][j] == player) {
                reverse++;
            } else {
                break;
            }
        }
        return reverse >= 4;
    }

    private boolean checkDiagonal(final int player, final int row, final int col) {
        int diagonal = 1;
        for (int i = row - 1, j = col - 1; i >= 0 && j >= 0; i--, j--) {
            if (board[i][j] == player) {
                diagonal++;
            } else {
                break;
            }
        }
        for (int i = row + 1, j = col + 1; i < ROWS && j < COLS; i++, j++) {
            if (board[i][j] == player) {
                diagonal++;
            } else {
                break;
            }
        }
        return diagonal >= 4;
    }

    public Connect4Board copy() {
        final Connect4Board copy = new Connect4Board();
        System.arraycopy(heights, 0, copy.heights, 0, heights.length);
        for (int i = 0; i < copy.board.length; i++) {
            System.arraycopy(board[i], 0, copy.board[i], 0, board[i].length);
        }
        copy.coins = coins;
        return copy;
    }

    public String prettyPrint() {
        final StringBuilder sb = new StringBuilder();
        for (int i = Connect4Board.ROWS - 1; i >= 0; i--) {
            sb.append(Arrays.toString(board[i])).append('\n');
        }
        sb.append("HEIGHTS").append(Arrays.toString(heights)).append('\n');
        sb.append(coins).append('\n');
        return sb.toString();
    }
}