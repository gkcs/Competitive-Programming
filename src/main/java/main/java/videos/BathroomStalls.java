package main.java.videos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class BathroomStalls {
    public static void main(String[] args) throws IOException {
        final Queue<Stall> stalls = new PriorityQueue<>((o1, o2) -> {
            final int size1 = o1.nearestRight - o1.nearestLeft, size2 = o2.nearestRight - o2.nearestLeft;
            if (size1 != size2) {
                return size2 - size1;
            } else {
                return o1.nearestLeft - o2.nearestLeft;
            }
        });
        final StringBuilder sb = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final int T = Integer.parseInt(bufferedReader.readLine());
        for (int t = 0; t < T; t++) {
            final String split[] = bufferedReader.readLine().split(" ");
            final int n = Integer.parseInt(split[0]);
            final int k = Integer.parseInt(split[1]);
            stalls.clear();
            stalls.add(new Stall(0, n + 1));
            int max = 0, min = 0;
            for (int i = 0; i < k; i++) {
                final Stall largest = stalls.poll();
                final int middle = (largest.nearestRight + largest.nearestLeft) >> 1;
                stalls.add(new Stall(middle, largest.nearestRight));
                stalls.add(new Stall(largest.nearestLeft, middle));
                max = Math.max(largest.nearestRight - middle, middle - largest.nearestLeft) - 1;
                min = Math.min(largest.nearestRight - middle, middle - largest.nearestLeft) - 1;
            }
            sb.append("Case #").append(t + 1).append(": ").append(max).append(" ").append(min).append('\n');
        }
        System.out.println(sb);
    }
}

class Stall {
    final int nearestLeft, nearestRight;

    Stall(final int nearestLeft, final int nearestRight) {
        this.nearestLeft = nearestLeft;
        this.nearestRight = nearestRight;
    }

    @Override
    public String toString() {
        return "{L=" + nearestLeft + ", R=" + nearestRight + "}";
    }
}
