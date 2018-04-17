package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GCD {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        int a = Integer.parseInt(bufferedReader.readLine());
        int b = Integer.parseInt(bufferedReader.readLine());
        System.out.println(recursiveGcd(a, b));
        System.out.println(iterativeGcd(a, b));
        System.out.println(binaryGcd(a, b));
    }

    private static int recursiveGcd(final int a, final int b) {
        return b == 0 ? a : recursiveGcd(b, a % b);
    }

    private static int iterativeGcd(int a, int b) {
        while (b != 0) {
            final int temp = a % b;
            a = b;
            b = temp;
        }
        return a;
    }

    private static int binaryGcd(int a, int b) {
		if (a < b) {
			int tempSwap = a;
			a = b;
			b = tempSwap;
		}
		
        if (b == 0) {
            return a;
        }
        int commonMultipleOf2 = 1;
        while ((a & 1) == 0 && (b & 1) == 0) {
            commonMultipleOf2 = commonMultipleOf2 << 1;
            a = a >> 1;
            b = b >> 1;
        }
        while ((a & 1) == 0) {
            a = a >> 1;
        }
        while ((b & 1) == 0) {
            b = b >> 1;
        }
        while (b != 0) {
            int temp = a - b;
            a = b;
            b = temp;
            while ((b & 1) == 0 && b > 0) {
                b = b >> 1;
            }
            if (a < b) {
                int tempSwap = a;
                a = b;
                b = tempSwap;
            }
        }
        return a * commonMultipleOf2;
        //Knuth 20% saving in processing power
    }
}
