package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StringManipulations {
    public static void main(String[] args) throws IOException {
        final StringBuilder outputStringBuilder = new StringBuilder();
        final StringBuilder stringBuilder = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final int q = Integer.parseInt(bufferedReader.readLine());
        for (int i = 0; i < q; i++) {
            final String x[] = bufferedReader.readLine().split(" ");
            if (x[0].equals("+")) {
                final int start = Integer.parseInt(x[1]);
                final String substring = x[2];
                stringBuilder.insert(start, substring);
            } else {
                final int start = Integer.parseInt(x[1]) - 1;
                final int length = Integer.parseInt(x[2]) - 1;
                final String substring = stringBuilder.substring(start, start + length + 1);
                outputStringBuilder.append(substring).append('\n');
            }
        }
        System.out.println(outputStringBuilder);
    }
}