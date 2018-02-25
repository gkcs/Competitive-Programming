package main.java.course;

import java.util.Arrays;
import java.util.HashMap;

public class Map {
    private int keys[];
    private String values[];
    private int size;

    public Map(final int size) {
        values = new String[size];
        keys = new int[size];
        this.size = 0;
    }

    public void add(int key, String value) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == key) {
                values[i] = value;
                return;
            }
        }
        keys[size] = key;
        values[size] = value;
        size++;
    }

    public String delete(int key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == key) {
                keys[i] = keys[size - 1];
                values[i] = values[size - 1];
                size--;
                return values[i];
            }
        }
        throw new RuntimeException("Value does not exist!");
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public String search(int key) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == key) {
                return values[i];
            }
        }
        return null;
    }

    public boolean contains(int key) {
        for (final int mapKey : keys) {
            if (mapKey == key) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Map{" +
                "keys=" + Arrays.toString(keys) +
                ", values=" + Arrays.toString(values) +
                ", size=" + size +
                '}';
    }
}

class MapMain {
    public static void main(String[] args) {
//        Map map = new Map(10);
//        map.add(1123, "Java");
//        map.add(1124, "C++");
//        map.add(1125, "Python");
//        System.out.println(map);
//        map.delete(1124);
//        System.out.println(map);
//        map.add(1123, "C++");
//        System.out.println(map);
//        System.out.println(map.search(1123));
//        map.add(1125, null);
//        System.out.println(map.search(1125));
//        System.out.println(map.contains(1125));
//        System.out.println(map.contains(1124));
//        System.out.println(map.search(1124));
        java.util.Map<Integer, String> stringMap = new HashMap<>();
        stringMap.put(1123, "Java");
        stringMap.remove(1123);
        stringMap.get(1123);
        stringMap.containsKey(1123);
    }
}