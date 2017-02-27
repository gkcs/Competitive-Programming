package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Predicate;

public class DEVUCLASS {
    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final StringBuilder stringBuilder = new StringBuilder();
        final DEVUCLASS devuclass = new DEVUCLASS();
        for (int t = Integer.parseInt(bufferedReader.readLine()); t > 0; t--) {
            final int type = Integer.parseInt(bufferedReader.readLine());
            stringBuilder.append(devuclass.findCostOfRearranging(bufferedReader.readLine(), type)).append('\n');
        }
        System.out.println(stringBuilder);
    }

    private long findCostOfRearranging(final String arrangement, final int type) {
        int b = 0, g = 0;
        for (char c : arrangement.toCharArray()) {
            if (c == 'B') {
                b++;
                g++;
            }
        }
        if (Math.abs(b - g) > 1) {
            return -1;
        }
        long cost;
        final Predicate<Integer> predicate = j -> j % 2 != 0;
        if (b > g) {
            cost = findArrangementCost(arrangement, type, predicate);
        } else if (g > b) {
            cost = findArrangementCost(arrangement, type, predicate.negate());
        } else {
            cost = findArrangementCost(arrangement, type, predicate);
            final long otherCost =
                    findArrangementCost(arrangement, type, predicate.negate());
            if (otherCost < cost) {
                cost = otherCost;
            }
        }
        return cost;
    }

    private long findArrangementCost(final String arrangement,
                                     final int type,
                                     final Predicate<Integer> predicate) {
        long cost = 0;
        final Queue<Integer> boysOffPosition = new ArrayDeque<>(), girlsOffPosition = new ArrayDeque<>();
        findOffPositions(arrangement, boysOffPosition, girlsOffPosition, predicate);
        final int weirdos = boysOffPosition.size();
        for (int i = 0; i < weirdos; i++) {
            cost += getCost(boysOffPosition.poll(), girlsOffPosition.poll(), type);
        }
        return cost;
    }

    private void findOffPositions(final String arrangement,
                                  final Queue<Integer> boysOffPosition,
                                  final Queue<Integer> girlsOffPosition,
                                  final Predicate<Integer> predicate) {
        for (int i = 0; i < arrangement.length(); i++) {
            if (arrangement.charAt(i) == 'B') {
                if (predicate.test(i)) {
                    boysOffPosition.add(i);
                }
            } else {
                if (predicate.negate().test(i)) {
                    girlsOffPosition.add(i);
                }
            }
        }
    }

    private long getCost(final int index1, final int index2, final int type) {
        return type == 0 ? 1 : Math.abs(index1 - index2);
    }
}