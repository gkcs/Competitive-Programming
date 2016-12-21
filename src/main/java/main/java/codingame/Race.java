package main.java.codingame;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;

class Race {

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        boolean boost = true;
        // game loop
        int touch=0;
        boolean lap=false;
        final List<Point> checkpoints = new ArrayList<>();
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            if(checkpoints.isEmpty()){
                checkpoints.add(new Point(x,y));
            }
            if(x==checkpoints.get(0).x&&y==checkpoints.get(0).y){
                touch++;
            }
            int cX = in.nextInt();
            int cY = in.nextInt();
            int nextCheckpointDist = in.nextInt();
            int nextCheckpointAngle = in.nextInt();
            final Point point = new Point(cX, cY);
            int indexOf = checkpoints.indexOf(point);
            if(indexOf==0){
                lap=true;
            }
            System.err.println(checkpoints);
            System.err.println(indexOf+" "+lap);
            if (indexOf >= 0&&lap) {
                if (nextCheckpointDist < 2500*Math.cos(Math.toRadians(nextCheckpointAngle)) && Math.abs(nextCheckpointAngle) < 20) {
                    if (indexOf == checkpoints.size()-1) {
                        cX = checkpoints.get(0).x;
                        cY = checkpoints.get(0).y;
                    } else {
                        cX = checkpoints.get(indexOf + 1).x;
                        cY = checkpoints.get(indexOf + 1).y;
                        if(indexOf==0){
                            if(touch==3){
                                cX = checkpoints.get(indexOf ).x;
                                cY = checkpoints.get(indexOf ).y;
                            }
                        }
                    }
                }
            } else {
                checkpoints.add(point);
            }
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();
            int thrust;
            if (nextCheckpointAngle > 90 || nextCheckpointAngle < -90) {
                thrust = 10;
            } else if (boost && Math.abs(nextCheckpointAngle) < 5 && nextCheckpointDist > 800) {
                thrust = -1;
                boost = false;
            } else {
                thrust = 100;
            }
            System.out.println(cX + " " + cY + " " + (thrust == -1 ? "BOOST" : String.valueOf(thrust)));
        }
    }
}

class Point {
    final int x, y;

    Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Math.abs(x - point.x)<1200 && Math.abs(y - point.y)<1200;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}