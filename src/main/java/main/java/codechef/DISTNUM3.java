package main.java.codechef;

import main.java.InputReader;

import java.util.*;
import java.io.*;

/**
 * Reference: https://www.codechef.com/viewsolution/12879967
 */
public class DISTNUM3 {
    static List<Integer>[] adj;
    static int clock = 0, start[], end[], V;
    static int DP[][];
    static int level[];
    static int parent[];
    static int distinctCount;
    static boolean marked[];
    static int vertices[];

    static int log(int N) {
        return 31 - Integer.numberOfLeadingZeros(N);
    }

    static void findAncestorsAtEachLevel() {
        System.arraycopy(parent, 1, DP[0], 1, V);
        for (int i = 1; i < DP.length; i++) {
            for (int j = 1; j <= V; j++) {
                DP[i][j] = DP[i - 1][DP[i - 1][j]];
            }
        }
    }

    static int LCA(int u, int v) {
        if (level[v] < level[u]) {
            final int temp = u;
            u = v;
            v = temp;
        }
        int diff = level[v] - level[u];
        while (diff > 0) {
            final int log = log(diff);
            v = DP[log][v];
            diff -= (1 << log);
        }
        while (u != v) {
            int i = log(level[u]);
            while (i > 0 && DP[i][u] == DP[i][v]) {
                i--;
            }
            u = DP[i][u];
            v = DP[i][v];
        }
        return u;
    }

    static void dfs(final int u, final int par, final int lev, final int[] eulerTour) {
        eulerTour[clock] = u;
        start[u] = clock++;
        parent[u] = par;
        level[u] = lev;
        for (final int v : adj[u]) {
            if (v != par) {
                dfs(v, u, lev + 1, eulerTour);
            }
        }
        eulerTour[clock] = u;
        end[u] = clock++;
    }

    static void visit(final int idx, final int[] frequency) {
        if (marked[idx]) {
            frequency[vertices[idx]]--;
            if (frequency[vertices[idx]] == 0) {
                distinctCount--;
            }
        } else {
            frequency[vertices[idx]]++;
            if (frequency[vertices[idx]] == 1) {
                distinctCount++;
            }
        }
        marked[idx] = !marked[idx];
    }

    static void update(final int idx, final int newVal, final int[] frequency) {
        if (marked[idx]) {
            visit(idx, frequency);
            vertices[idx] = newVal;
            visit(idx, frequency);
        } else {
            vertices[idx] = newVal;
        }
    }

    public static void main(String[] args) throws IOException {
        final InputReader in = new InputReader(System.in);
        V = in.readInt();
        final int Q = in.readInt();
        adj = new ArrayList[V + 1];
        for (int i = 1; i <= V; i++) {
            adj[i] = new ArrayList<>();
        }
        vertices = new int[V + 1];
        for (int i = 1; i <= V; i++) {
            vertices[i] = in.readInt();
        }
        final Map<Integer, Integer> map = new HashMap<>();
        for (int i = 1; i <= V; i++) {
            map.putIfAbsent(vertices[i], map.size());
            vertices[i] = map.get(vertices[i]);
        }
        final int verticesCopy[] = new int[V + 1];
        System.arraycopy(vertices, 0, verticesCopy, 0, V + 1);
        final int edges = V - 1;
        for (int i = 0; i < edges; i++) {
            final int u = in.readInt();
            final int v = in.readInt();
            adj[u].add(v);
            adj[v].add(u);
        }
        start = new int[V + 1];
        end = new int[V + 1];
        final int[] eulerTour = new int[2 * (V + 1)];
        level = new int[V + 1];
        marked = new boolean[V + 1];
        DP = new int[log(V) + 1][V + 1];
        parent = new int[V + 1];
        dfs(1, 0, 0, eulerTour);
        findAncestorsAtEachLevel();
        int numberOfQueries = 0, numberOfUpdates = 0;
        final Query queries[] = new Query[Q];
        final Update updates[] = new Update[Q];
        for (int i = 0; i < Q; i++) {
            if (in.readInt() == 1) {
                final int u = in.readInt();
                final int v = in.readInt();
                final Query q;
                if (start[v] > end[u]) {
                    q = new Query(end[u], start[v], numberOfUpdates, LCA(u, v), numberOfQueries);
                } else if (start[u] > end[v]) {
                    q = new Query(end[v], start[u], numberOfUpdates, LCA(u, v), numberOfQueries);
                } else {
                    q = new Query(Math.min(start[u], start[v]),
                                  Math.max(start[u], start[v]),
                                  numberOfUpdates,
                                  -1,
                                  numberOfQueries);
                }
                queries[numberOfQueries++] = q;
            } else {
                final int idx = in.readInt();
                int newVal = in.readInt();
                map.putIfAbsent(newVal, map.size());
                newVal = map.get(newVal);
                updates[numberOfUpdates++] = new Update(idx, newVal, verticesCopy[idx]);
                verticesCopy[idx] = newVal;
            }
        }
        final int BLOCK_SIZE = (int) (Math.pow(2 * V, 2.0 / 3.0) + 1);
        Arrays.sort(queries, 0, numberOfQueries, (first, second) -> {
            if (first.L / BLOCK_SIZE != second.L / BLOCK_SIZE) {
                return first.L / BLOCK_SIZE - second.L / BLOCK_SIZE;
            } else if (first.R / BLOCK_SIZE != second.R / BLOCK_SIZE) {
                return first.R / BLOCK_SIZE - second.R / BLOCK_SIZE;
            } else {
                return first.updatesTillNow - second.updatesTillNow;
            }
        });
        final int ans[] = new int[numberOfQueries];
        int moLeft = -1, moRight = -1;
        int currentUpdateCount = 0;
        final int[] frequency = new int[map.size()];
        for (int i = 0; i < numberOfQueries; i++) {
            final Query query = queries[i];
            while (currentUpdateCount < query.updatesTillNow) {
                final Update update = updates[currentUpdateCount++];
                update(update.idx, update.newValue, frequency);
            }
            while (currentUpdateCount > query.updatesTillNow) {
                final Update update = updates[--currentUpdateCount];
                update(update.idx, update.previousValue, frequency);
            }
            while (moLeft < query.L - 1) {
                moLeft++;
                visit(eulerTour[moLeft], frequency);
            }
            while (moLeft >= query.L) {
                visit(eulerTour[moLeft], frequency);
                moLeft--;
            }
            while (moRight < query.R) {
                moRight++;
                visit(eulerTour[moRight], frequency);
            }
            while (moRight > query.R) {
                visit(eulerTour[moRight], frequency);
                moRight--;
            }
            if (query.LCA != -1) {
                visit(query.LCA, frequency);
            }
            ans[query.id] = distinctCount;
            if (query.LCA != -1) {
                visit(query.LCA, frequency);
            }
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for (final int a : ans) {
            stringBuilder.append(a).append('\n');
        }
        System.out.println(stringBuilder);
    }
}

class Query {
    final int L, R, updatesTillNow, LCA, id;

    public Query(final int l, final int r, final int updatesTillNow, final int lCA, final int id) {
        L = l;
        R = r;
        this.updatesTillNow = updatesTillNow;
        LCA = lCA;
        this.id = id;
    }
}

class Update {
    final int idx, previousValue, newValue;

    public Update(final int idx, final int newValue, final int previousValue) {
        this.idx = idx;
        this.newValue = newValue;
        this.previousValue = previousValue;
    }
}