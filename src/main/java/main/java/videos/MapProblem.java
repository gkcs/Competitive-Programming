package main.java.videos;

import java.util.HashMap;
import java.util.Map;

public class MapProblem {
    public static void main(String[] args) {
        final Map<String, MyObject> map = new HashMap<>();
        map.put("jam", new MyObject("jammy", 23));
        map.putIfAbsent("jam", new MyObject("YOLO", 25));
        map.put("games", null);
        System.out.println(map.get("jam"));
        System.out.println(map.get("games"));
        System.out.println(map.containsKey("jam"));
        map.remove("games");
        System.out.println(map.containsKey("games"));
    }
}

class MyObject {
    final String x;
    final int y;

    MyObject(final String x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MyObject myObject = (MyObject) o;
        return y == myObject.y && x.equals(myObject.x);
    }

    @Override
    public int hashCode() {
        return 31 * x.hashCode() + y;
    }

    @Override
    public String toString() {
        return "MyObject{" +
                "x='" + x + '\'' +
                ", y=" + y +
                '}';
    }
}