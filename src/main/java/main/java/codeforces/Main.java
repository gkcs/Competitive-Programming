package main.java.codeforces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;

public class Main {

    public static void main(String[] args) throws java.lang.Exception {
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        for (int testCases = parseInt(br.readLine()); testCases > 0; testCases--) {
            final String numbers[] = br.readLine().split(" ");
            final List<Integer> distances = Stream.of(parseInt(numbers[0]), parseInt(numbers[1]), parseInt(numbers[2])).sorted().collect(Collectors.toList());
            final int X = distances.get(0), Y = distances.get(1), Z = distances.get(2);
            final int A = parseInt(numbers[3]), B = parseInt(numbers[4]), C = parseInt(numbers[5]);
            final int moveUsingOnlyAs = (X + Y + Z) * A;
            final int moveUsingOnlyCandAs = X * C + (Y + Z - 2 * X) * A;
            final int moveUsingA_BandC = X * C + (Y - X) * B + (Z - Y) * A;
            long res = Stream.of(moveUsingOnlyAs, moveUsingOnlyCandAs, moveUsingA_BandC).min(Integer::compareTo).orElseThrow(RuntimeException::new);
            int k = Math.min(Z - Y, X);
            final int moveUsingOnlyAandBs = X * B + (Y - X) * B + (Z - (Y - X)) * A;
            res = Math.min(res, Math.min(Y * B + k * B + (Z - Y - k) * A + (X - k) * A, moveUsingOnlyAandBs));
            final int p1 = Y - X;
            final int p2 = Z - Y;
            //2 * Z - Y - X
            if (X - p2 >= 0) {
                res = Math.min(res, (p1 + 2 * p2) * B + (X - p2) * C);
                res = Math.min(res, (p1 + 2 * p2 + (X - p2) / 2 * 3) * B + ((X - p2) & 1) * Math.min(C, Math.min(3 * A, A + B)));
            }
            System.out.println(res);
        }
    }

}