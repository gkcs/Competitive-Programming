package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TShirts {
    public static void main(String[] args) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final StringBuilder stringBuilder = new StringBuilder();
        for (int t = Integer.parseInt(br.readLine()); t > 0; t--) {
            final int n = Integer.parseInt(br.readLine());
            final int wardrobe[][] = new int[n][];
            for (int i = 0; i < n; i++) {
                wardrobe[i] = Arrays.stream(br.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            }
            final Solver solver = new Solver(wardrobe);
            stringBuilder.append(solver.solve()).append('\n');
        }
        System.out.println(stringBuilder);
    }
}

class Solver {

    public static final int MOD = 1000000007;
    private final int length;
    private int[][] people;
    private final int[] count;
    private final long dp[][];

    public Solver(final int[][] wardrobe) {
        final int people[][] = new int[101][wardrobe.length];
        count = new int[101];
        for (int i = 0; i < wardrobe.length; i++) {
            for (int index = 0; index < wardrobe[i].length; index++) {
                int shirt = wardrobe[i][index];
                people[shirt][count[shirt]++] = i;
            }
        }
        this.length = wardrobe.length;
        this.people = people;
        dp = new long[1 << length][101];
        for (final long[] aDp : dp) {
            Arrays.fill(aDp, -1);
        }
    }

    public long solve() {
        return possiblities(0, 0);
    }

    private long possiblities(final int people, final int assigned) {
        if (assigned == this.people.length) {
            return people == (1 << length) - 1 ? 1 : 0;
        }
        if (dp[people][assigned] != -1) {
            return dp[people][assigned];
        }
        long answer = 0;
        for (int i = 0; i < count[assigned]; i++) {
            int person = this.people[assigned][i];
            if ((people & (1 << person)) == 0) {
                int changedPeople = people;
                changedPeople = changedPeople | (1 << person);
                answer = (answer + possiblities(changedPeople, assigned + 1)) % MOD;
            }
        }
        answer = (answer + possiblities(people, assigned + 1)) % MOD;
        dp[people][assigned] = answer;
        return answer;
    }
}
