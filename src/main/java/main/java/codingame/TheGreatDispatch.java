package main.java.codingame;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Collectors;

class Player {

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        final int boxCount = in.nextInt();
        final Truck[] trucks = new Truck[100];
        final Box[] boxes = new Box[boxCount];
        for (int i = 0; i < boxCount; i++) {
            trucks[i] = new Truck(i);
        }
        for (int i = 0; i < boxCount; i++) {
            boxes[i] = new Box(i, in.nextDouble(), in.nextDouble());
        }
        final int[] assignedTrucks = new int[boxCount];
        if (boxCount < 100) {
            for (int i = 0; i < boxCount; i++) {
                assignedTrucks[i] = i;
            }
        } else {
            Arrays.sort(boxes, (o1, o2) -> (int) Math.signum(o1.weight / o1.volume - o2.weight / o2.volume));
        }
        System.out.println(Arrays.stream(assignedTrucks).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
    }
}

class Truck {
    static final int MAX_VOLUME = 100;
    double weight;
    double volume;
    final int number;

    public Truck(final int number) {
        this.number = number;
    }
}

class Box {
    final int number;
    final double weight;
    final double volume;

    public Box(final int number, final double weight, final double volume) {
        this.number = number;
        this.weight = weight;
        this.volume = volume;
    }
}