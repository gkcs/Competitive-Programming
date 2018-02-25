package main.java.course;

import java.util.ArrayList;
import java.util.List;

public class HashMap {
    private final List<List<Pair>> buckets;

    public HashMap() {
        buckets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            buckets.add(new ArrayList<>());
        }
    }

    public void add(String key, Object value) {
        final List<Pair> pairs = getBucket(key);
        for (final Pair pair : pairs) {
            if (pair.key.equals(key)) {
                pairs.remove(pair);
                break;
            }
        }
        pairs.add(new Pair(key, value));
    }

    public Object delete(String key) {
        final List<Pair> pairs = getBucket(key);
        Object removedValue = null;
        for (final Pair pair : pairs) {
            if (pair.key.equals(key)) {
                pairs.remove(pair);
                removedValue = pair.value;
                break;
            }
        }
        return removedValue;
    }

    public Object search(String key) {
        final List<Pair> pairs = getBucket(key);
        Object value = null;
        for (final Pair pair : pairs) {
            if (pair.key.equals(key)) {
                value = pair.value;
            }
        }
        return value;
    }

    private List<Pair> getBucket(final String key) {
        return buckets.get(key.hashCode() % 10);
    }

    @Override
    public String toString() {
        return "HashMap{" +
                "buckets=" + buckets +
                '}';
    }
}

class Pair {
    final String key;
    final Object value;

    public Pair(final String key, final Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        return key.equals(((Pair) o).key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "Pair{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}

class HashMapMain {
    public static void main(String[] args) {
//        HashMap hashMap = new HashMap();
//        hashMap.add("java", "course");
//        hashMap.add("C++", "code");
//        hashMap.add("C#", "Microsoft");
//        System.out.println(hashMap);
//        System.out.println(hashMap.search("java"));
//        System.out.println(hashMap);
        java.util.HashMap<String, Object> hashMap = new java.util.HashMap<>();
        hashMap.put("java", "course");
        hashMap.put("C++", "code");
        hashMap.put("C#", "Microsoft");
        System.out.println(hashMap);
        System.out.println(hashMap.remove("java"));
        System.out.println(hashMap);

    }
}
