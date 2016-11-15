package main.java;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private final Map<Board, Long> map;

    public Cache() {
        map = new ConcurrentHashMap<>();
        new Thread(this::loadCache).start();
    }

    private void loadCache() {

    }

    public Long get(Board board) {
        return map.get(board);
    }
}
