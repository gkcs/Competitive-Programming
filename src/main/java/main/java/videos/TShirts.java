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
            final int sets[] = Arrays.stream(br.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            stringBuilder.append(new Solver(sets).solve()).append('\n');
        }
        System.out.println(stringBuilder);
    }
}

class Solver {

    public static final int MOD = 1000000007;
    private int[][] shirtSets;

    public Solver(final int[] people) {
        final int shirtSets[][] = new int[100][people.length];
        final int count[] = new int[people.length];
        for (int i = 0; i < people.length; i++) {
            shirtSets[people[i]][count[people[i]]++] = i;
        }
        this.shirtSets = shirtSets;
    }

    public long solve() {
        return possiblities(0, 0);
    }

    private long possiblities(final int people, final int assigned) {
        long answer = 0;
        for (int i = 0; i < shirtSets[assigned].length; i++) {
            int person = shirtSets[assigned][i];
            if ((people & (1 << person)) == 0) {
                int changedPeople = people;
                changedPeople = changedPeople | (1 << person);
                answer = (answer + possiblities(changedPeople, assigned + 1)) % MOD;
            }
        }
        return answer;
    }
}
