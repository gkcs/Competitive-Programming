package main.java.codechef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The algorithm is an adaptation of the hill climbing technique. Some of the key features are:
 * 1) Probabilistically choosing which 'block' to move into. Initially, the cube is divided into H^3 blocks, each
 * with side = N/H. Ensure that H is a factor of N.
 * 2) Every time a segment undergoes change, its 'stiffness' changes. Stiffness is the chance of the segment
 * changing again. Stiffness is directly proportional to how beneficial the current change was.
 * 3) A ribbon consists of K segments. There are multiple iterations to choose a suitable ribbon configuration.
 * 4) One possibility is to treat some tiny segments(size Q) as nuggets. Paths to the Plane X=1 can then be searched
 * from here.
 * 5) A genetic algorithm comes to mind. Crossover would be each parent's merging of one branch from each parent
 * protruding on opposite sides. Mutation would be a random segment shifting. Fitness calculation is simple.
 * Population generation is based on getting ribbons from Plane X=1 to nugget and then from nugget to anywhere else.
 * 6) Nuggets should then be interconnected. Ribbons passing through nuggets can be combined or broken apart.
 */

class Weaver {

    public String weaveWithMaximum(final int[][][] a) {
        return "";
    }
}

public class Main {

    private static final int SIZE = 50;

    public static void main(String[] args) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final int a[][][] = new int[SIZE][SIZE][SIZE];
        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    a[x][y][z] = Integer.parseInt(br.readLine());
                }
            }
        }
        System.out.println(new Weaver().weaveWithMaximum(a));
    }
}
