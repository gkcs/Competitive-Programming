package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

public class IntegerOperations {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final int t = Integer.parseInt(bufferedReader.readLine());
        for (int i = 0; i < t; i++) {
            System.out.println(factorial(Integer.parseInt(bufferedReader.readLine())));
        }
    }

    /**
     * @param n the number
     * @return n!
     */
    private static BigInteger factorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}
