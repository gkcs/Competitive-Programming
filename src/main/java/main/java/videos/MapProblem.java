package main.java.videos;

import java.util.HashMap;
import java.util.Map;

public class MapProblem {
    public static void main(String[] args) {
        final Map<MyObject, String> map = new HashMap<>();
        map.put(new MyObject(2, 3), "Jammy");
        map.put(new MyObject(3, 3), "YOLO");
        System.out.println(map.get(new MyObject(3, 3)));
    }
}

class MyObject {
    int x, y;

    public MyObject(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MyObject myObject = (MyObject) o;
        return y == myObject.y && x == myObject.x;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "MyObject{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}