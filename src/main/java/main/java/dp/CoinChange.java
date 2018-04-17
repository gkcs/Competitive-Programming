package main.java.dp;

public class CoinChange {
    private static long countWays(int coins[], int amount) {
        final long[] table = new long[amount + 1];
        table[0] = 1;
        for (final int value : coins) {
            for (int j = value; j <= amount; j++) {
                table[j] += table[j - value];
            }
        }
        return table[amount];
    }

    public static void main(String args[]) {
        System.out.println(countWays(new int[]{1, 2, 3}, 4));
    }
}