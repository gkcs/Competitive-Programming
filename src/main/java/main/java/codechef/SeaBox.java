package main.java.codechef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class SeaBox {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final String[] params = bufferedReader.readLine().split(" ");
        final int n = Integer.parseInt(params[0]);
        final int diff = Integer.parseInt(params[1]);
        final int a[][][] = new int[n][n][n];
        final int maximumCube[][][] = new int[n][n][n];
        final int minimumCube[][][] = new int[n][n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                final String[] bits = bufferedReader.readLine().split(" ");
                for (int k = 0; k < n; k++) {
                    maximumCube[i][j][k] = minimumCube[i][j][k] = a[i][j][k] = Integer.parseInt(bits[k]);
                }
            }
        }
        final SeaBox seaBox = new SeaBox();
        final List<Unit> units = Arrays.stream(seaBox.findUnits(a, 0, 0, 0, n))
                .filter(Objects::nonNull)
                .filter(unit -> unit.size > 1)
                .collect(Collectors.toList());
        units.sort(Comparator.comparingInt(o -> o.size));
        int maxDiff = diff;
        for (final Unit unit : units) {
            if (maxDiff > 0 && unit.size == 2 && (unit.black == unit.size * unit.size * unit.size || unit.black == 0)) {
                maximumCube[unit.length][unit.width][unit.height]
                        = maximumCube[unit.length][unit.width][unit.height] == 1 ? 1 : 0;
                maxDiff--;
            }
        }
        Collections.reverse(units);

        int minDiff = diff;
        //Change of plan, if I can recursively change the cube as we process it, that should be enough

        System.out.println(seaBox.findSums(minimumCube) + " " + seaBox.findSums(maximumCube));
    }


    private int findSums(final int[][][] a) {
        return findSums(a, 0, 0, 0, a.length);
    }

    private void makeHomogeneous(final int[][][] a,
                                 final int length,
                                 final int width,
                                 final int height,
                                 final int size,
                                 int diff) {
        if (size != 1 && !allAreSame(a, length, width, height, size)) {
            final int halfSize = size >> 1;
            final int volume = halfSize * halfSize * halfSize;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        int x = findSums(a,
                                         length + i * halfSize,
                                         width + j * halfSize,
                                         height + k * halfSize,
                                         halfSize);
                        x = x < volume ? x : volume - x;
                        if (x != 0) {
                            if (x < diff) {
                                makeAllSame(a,
                                            length + i * halfSize,
                                            width + j * halfSize,
                                            height + k * halfSize,
                                            halfSize);
                                diff -= x;
                            } else {

                            }
                        }
                    }
                }
            }
        }
    }

    private int findSums(final int[][][] a, final int length, final int width, final int height, final int size) {
        if (size == 1 || allAreSame(a, length, width, height, size)) {
            return 1;
        } else {
            int count = 0;
            final int halfSize = size >> 1;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        count += findSums(a,
                                          length + i * halfSize,
                                          width + j * halfSize,
                                          height + k * halfSize,
                                          halfSize);
                    }
                }
            }
            return count;
        }
    }

    private Unit[] findUnits(final int[][][] a, final int length, final int width, final int height, final int size) {
        if (size == 1) {
            return new Unit[]{new Unit(size, a[length][width][height] == 0 ? 1 : 0, length, width, height)};
        } else {
            final Unit[] units = new Unit[size * size * size * (int) (Math.ceil(Math.log(size) / Math.log(2)))];
            int count = 0;
            final int halfSize = size >> 1;
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        final Unit[] sums = findUnits(a,
                                                      length + i * halfSize,
                                                      width + j * halfSize,
                                                      height + k * halfSize,
                                                      halfSize);
                        System.arraycopy(sums, 0, units, count, sums.length);
                        count += sums.length;
                        units[count++] = new Unit(size,
                                                  getCount(sums),
                                                  length,
                                                  width,
                                                  height);
                    }
                }
            }
            return units;
        }
    }

    private int getCount(final Unit[] sums) {
        return Arrays.stream(sums).filter(Objects::nonNull).mapToInt(c -> c.white).sum();
    }

    private boolean allAreSame(final int[][][] a, int length, int width, int height, int size) {
        int count = getCount(a, length, width, height, size);
        return count == 0 || count == size * size * size;
    }

    private int getCount(int[][][] a, int length, int width, int height, int size) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = width; j < size; j++) {
                for (int k = height; k < size; k++) {
                    count += a[i + length][j + width][k + height];
                }
            }
        }
        return count;
    }

    private void makeAllSame(int[][][] a, int length, int width, int height, int size) {
        for (int i = 0; i < size; i++) {
            for (int j = width; j < size; j++) {
                for (int k = height; k < size; k++) {
                    a[i + length][j + width][k + height] = 1;
                }
            }
        }
    }

}

class Unit {
    public int getWhite() {
        return white;
    }

    public int getBlack() {
        return black;
    }

    int white, black;
    final int size;
    final int length;
    final int width;
    final int height;

    public Unit(int size, int white, int length, final int width, final int height) {
        this.white = white;
        this.size = size;
        this.length = length;
        this.width = width;
        this.height = height;
        this.black = size * size * size - white;
    }
}