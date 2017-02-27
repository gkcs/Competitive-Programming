package main.java.videos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class ALICE {
    public static void main(String[] args) {
        final InputReader inputReader = new InputReader(System.in);
        final int DIMENSIONS = inputReader.readInt(),
                fenceCount = inputReader.readInt(),
                startX = inputReader.readInt() - 1,
                startY = inputReader.readInt() - 1,
                penalty = inputReader.readInt();
        final int[][] flowers = new int[DIMENSIONS][DIMENSIONS], regrow = new int[DIMENSIONS][DIMENSIONS], limit = new int[DIMENSIONS][DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                flowers[i][j] = inputReader.readInt();
            }
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                limit[i][j] = inputReader.readInt();
            }
        }
        for (int i = 0; i < DIMENSIONS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                regrow[i][j] = inputReader.readInt();
            }
        }
        new ArrayList().remove(1);
        final int[][][] fences = new int[fenceCount][2][2];
        for (int i = 0; i < fenceCount; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    fences[i][j][k] = inputReader.readInt() - 1;
                }
            }
        }
        AntColony.Tour bestTour = new AntColony(flowers,
                                                regrow,
                                                limit,
                                                DIMENSIONS,
                                                fences,
                                                startX,
                                                startY,
                                                penalty).findBestTour();
        System.out.println(bestTour.getReward());
        System.out.println(bestTour.moves
                                   .stream()
                                   .map(AntColony.Movement::toString)
                                   .collect(Collectors.joining("\n")) + "\nEXIT");
    }

    public static class InputReader {
        private InputStream stream;
        private byte[] buf = new byte[1024];

        private int curChar;

        private int numChars;

        public InputReader(InputStream stream) {
            this.stream = stream;
        }

        public int read() {
            if (numChars == -1)
                throw new RuntimeException();
            if (curChar >= numChars) {
                curChar = 0;
                try {
                    numChars = stream.read(buf);
                } catch (IOException e) {
                    throw new RuntimeException();
                }
                if (numChars <= 0)
                    return -1;
            }
            return buf[curChar++];
        }

        public int readInt() {
            int c = read();
            while (isSpaceChar(c))
                c = read();
            int sgn = 1;
            if (c == '-') {
                sgn = -1;
                c = read();
            }
            int res = 0;
            do {
                res *= 10;
                res += c - '0';
                c = read();
            } while (!isSpaceChar(c));
            return res * sgn;
        }

        public boolean isSpaceChar(int c) {
            return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == -1;
        }
    }
}


/*
2 1 1 1 1
1 2
1 4
2 2
2 2
1 2
2 3
2 2 2 2
 */