package main.java.codechef;

import main.java.InputReader;

import java.util.*;
import java.io.*;

public class DISTNUM3 {
    private static final int MAX = 100000;
    static List<Integer>[] adj;
    static int clock = 0, start[], end[], V;
    /* LCA <NlogN , logN> dependency : level , log , V , DP = new int[log(V) + 1][V + 1];, parent (for the first level of DP) */
    static int DP[][];
    static int level[];
    static int parent[];
    static int distinctCount;
    static boolean marked[];
    static int val[];

    static int log(int N) {
        return 31 - Integer.numberOfLeadingZeros(N);
    }

    static void binaryLift() {
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
        while (diff > 0) {        // Bring v to the same level as u
            final int log = log(diff);
            v = DP[log][v];
            diff -= (1 << log);
        }
        while (u != v) {
            int i = log(level[u]);
            for (; i > 0 && DP[i][u] == DP[i][v]; ) {
                i--;
            }
            u = DP[i][u];
            v = DP[i][v];
        }
        return u;
    }

    static void dfs(int u, int par, int lev, final int[] eulerTour) {
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
            frequency[val[idx]]--;
            if (frequency[val[idx]] == 0) {
                distinctCount--;
            }
        } else {
            frequency[val[idx]]++;
            if (frequency[val[idx]] == 1) {
                distinctCount++;
            }
        }
        marked[idx] = !marked[idx];
    }

    static void update(final int idx, final int newVal, final int[] frequency) {
        if (marked[idx]) {
            visit(idx, frequency);
            val[idx] = newVal;
            visit(idx, frequency);
        } else {
            val[idx] = newVal;
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
        val = new int[V + 1];
        for (int i = 1; i <= V; i++) {
            val[i] = in.readInt();
        }
        final Map<Integer, Integer> map = new HashMap<>();
        for (int i = 1; i <= V; i++) {
            if (!map.containsKey(val[i])) {
                map.put(val[i], map.size());
            }
            val[i] = map.get(val[i]);
        }
        final int currVal[] = new int[V + 1];
        System.arraycopy(val, 0, currVal, 0, V + 1);
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
        final int block[] = new int[2 * (V + 1)];
        dfs(1, 0, 0, eulerTour);
        binaryLift();
        int numberOfQueries = 0, numberOfUpdates = 0;
        final Query queries[] = new Query[MAX];
        final Update updates[] = new Update[MAX];
        for (int i = 0; i < Q; i++) {
            if (in.readInt() == 1) { // Query
                final int u = in.readInt();
                final int v = in.readInt();
                final Query q;
                if (end[u] < start[v])    // Cousin Nodes
                {
                    q = new Query(end[u], start[v], numberOfUpdates, LCA(u, v), numberOfQueries);
                } else if (start[u] > end[v]) {
                    q = new Query(end[v], start[u], numberOfUpdates, LCA(u, v), numberOfQueries);
                } else            // Ancestors
                {
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
                if (!map.containsKey(newVal)) {
                    map.put(newVal, map.size());
                }
                newVal = map.get(newVal);
                updates[numberOfUpdates++] = new Update(idx, newVal, currVal[idx]);
                currVal[idx] = newVal;
            }
        }
        final int BLOCK_SIZE = (int) (Math.pow(2 * V, 2.0 / 3.0) + 1);
        for (int i = 0; i < block.length; i++) {
            block[i] = i / BLOCK_SIZE;
        }
        Arrays.sort(queries, 0, numberOfQueries, (o1, o2) -> {
            if (block[o1.L] != block[o2.L]) {
                return block[o1.L] - block[o2.L];
            } else if (block[o1.R] != block[o2.R]) {
                return block[o1.R] - block[o2.R];
            } else {
                return o1.updatesTillNow - o2.updatesTillNow;
            }
        });
        final int ans[] = new int[numberOfQueries];
        int moLeft = -1, moRight = -1;
        int currentUpdateCount = 0;
        final int[] freq = new int[map.size()];
        for (int i = 0; i < numberOfQueries; i++) {
            final Query query = queries[i];
            while (currentUpdateCount < query.updatesTillNow) {
                final Update update = updates[currentUpdateCount];
                update(update.idx, update.newVal, freq);
                currentUpdateCount++;
            }
            while (currentUpdateCount > query.updatesTillNow) {
                currentUpdateCount--;
                final Update update = updates[currentUpdateCount];
                update(update.idx, update.prevVal, freq);
            }
            while (moLeft < query.L - 1) {
                moLeft++;
                visit(eulerTour[moLeft], freq);
            }
            while (moLeft >= query.L) {
                visit(eulerTour[moLeft], freq);
                moLeft--;
            }
            while (moRight < query.R) {
                moRight++;
                visit(eulerTour[moRight], freq);
            }
            while (moRight > query.R) {
                visit(eulerTour[moRight], freq);
                moRight--;
            }
            if (query.LCA != -1) {
                visit(query.LCA, freq);
            }
            ans[query.id] = distinctCount;
            if (query.LCA != -1) {
                visit(query.LCA, freq);
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

    @Override
    public String toString() {
        return String.format("[L = %d R = %d updatesLess = %d LCA = %d id = %d]", L, R, updatesTillNow, LCA, id);
    }
}

class Update {
    final int idx, prevVal, newVal;

    public Update(final int idx, final int newVal, final int prevVal) {
        this.idx = idx;
        this.newVal = newVal;
        this.prevVal = prevVal;
    }

    @Override
    public String toString() {
        return String.format("[idx = %d prevVal = %d newVal = %d", idx, prevVal, newVal);
    }
}