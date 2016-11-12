package main.java;

import java.util.Arrays;

public class Experiments {

    public static void main(String[] args) {
        System.out.println(compactRepresentation(new int[][][]{
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {2, 1}, {1, 3}, {2, 1}},
                {{0, 0}, {0, 0}, {0, 0}, {2, 1}, {0, 0}},
                {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}}}));
    }

    private static String compactRepresentation(final int[][][] board) {
        final byte[] representation = new byte[13];
        int offset = 0;
        for (final int[][] row : board) {
            for (final int[] col : row) {
                for (final int content : col) {
                    System.out.println(offset + " " + (content << (offset & 7)));
                    representation[offset >> 3] |= content << (offset & 7);
                    offset = offset + 2;
                }
            }
        }
        return Arrays.toString(representation);
    }
}