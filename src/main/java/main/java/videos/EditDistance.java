package main.java.videos;

import java.util.stream.Stream;

public class EditDistance {
    public static int findEditDistance(final String a,
                                       final String b,
                                       final int cD,
                                       final int cA,
                                       final int cT) {
        final int f[][] = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            f[i][0] = cD * i;
        }
        for (int j = 1; j <= b.length(); j++) {
            f[0][j] = cA * j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                final int transitionCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : cT;
                f[i][j] = Stream.of(f[i - 1][j] + cD, f[i][j - 1] + cA, f[i - 1][j - 1] + transitionCost)
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            }
        }
        return f[a.length()][b.length()];
    }
}

class EditDistanceMain {
    public static void main(String[] args) {
        System.out.println("Edit Distance: " + EditDistance.findEditDistance("pqqrst", "qqttps", 1, 1, 1));
    }
}