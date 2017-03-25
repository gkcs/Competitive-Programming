package main.java.codechef;

import main.java.InputReader;

import java.util.*;
import java.io.*;

public class DISTNUM3 {
    private static final int MAX = 100000;
    static ArrayList<Integer>[] adj;
    static int clock = 0, eulerTour[], start[], end[], V;
    static int BLOCK_SIZE;

    /* LCA <NlogN , logN> dependency : level , log , V , DP = new int[log(V) + 1][V + 1];, parent (for the first level of DP) */
    static int DP[][];
    static int level[];
    static int parent[];

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
            int temp = u;
            u = v;
            v = temp;
        }
        int diff = level[v] - level[u];
        while (diff > 0) {        // Bring v to the same level as u
            int log = log(diff);
            v = DP[log][v];
            diff -= (1 << log);
        }
        while (u != v) {
            int i = log(level[u]);
            for (; i > 0 && DP[i][u] == DP[i][v]; )
                i--;

            u = DP[i][u];
            v = DP[i][v];
        }

        return u;
    }

    static void dfs(int u, int par, int lev) {
        eulerTour[clock] = u;
        start[u] = clock++;
        parent[u] = par;
        level[u] = lev;
        for (int v : adj[u])
            if (v != par)
                dfs(v, u, lev + 1);

        eulerTour[clock] = u;
        end[u] = clock++;
    }


    static class Query {
        int L, R, numUpdatesLess, LCA, id;

        public Query(int l, int r, int numUpdatesLess, int lCA, int id) {
            L = l;
            R = r;
            this.numUpdatesLess = numUpdatesLess;
            LCA = lCA;
            this.id = id;
        }

        @Override
        public String toString() {
            return String.format("[L = %d R = %d updatesLess = %d LCA = %d id = %d]", L, R, numUpdatesLess, LCA, id);
        }
    }

    static class Update {
        int idx, prevVal, newVal;

        public Update(int idx, int newVal, int prevVal) {
            this.idx = idx;
            this.newVal = newVal;
            this.prevVal = prevVal;
        }

        @Override
        public String toString() {
            return String.format("[idx = %d prevVal = %d newVal = %d", idx, prevVal, newVal);
        }
    }

    static class MoComparator implements Comparator<Query> {
        @Override
        public int compare(Query o1, Query o2) {
            if (blockCache[o1.L] != blockCache[o2.L])
                return blockCache[o1.L] - blockCache[o2.L];
            else if (blockCache[o1.R] != blockCache[o2.R])
                return blockCache[o1.R] - blockCache[o2.R];
            else
                return o1.numUpdatesLess - o2.numUpdatesLess;
        }
    }

    static int freq[];
    static int distinctCount;
    static boolean marked[];
    static int blockCache[];
    static int val[];
    static HashMap<Integer, Integer> map;

    static void visit(int idx) {
        if (marked[idx]) {
            freq[val[idx]]--;
            if (freq[val[idx]] == 0)
                distinctCount--;
        } else {
            freq[val[idx]]++;
            if (freq[val[idx]] == 1)
                distinctCount++;
        }

        marked[idx] = !marked[idx];
    }

    static void update(int idx, int newVal) {
        if (marked[idx]) {
            visit(idx);
            val[idx] = newVal;
            visit(idx);
        } else
            val[idx] = newVal;
    }

    static int countDistinct(int u, int v) {
        int lca = LCA(u, v);
        BitSet bitSet = new BitSet(map.size());
        bitSet.set(val[lca]);
        while (u != lca) {
            bitSet.set(val[u]);
            u = parent[u];
        }
        while (v != lca) {
            bitSet.set(val[v]);
            v = parent[v];
        }

        return bitSet.cardinality();
    }

    public static void main(String[] args) throws IOException {
        final InputReader in = new InputReader(System.in);
        int qSZ = 0, uSZ = 0;
        V = in.readInt();
        int Q = in.readInt();
        int E = V - 1;
        Query queries[] = new Query[MAX];
        Update updates[] = new Update[MAX];
        map = new HashMap<>();    // Used to compress the keys

        adj = new ArrayList[V + 1];
        for (int i = 1; i <= V; i++) {
            adj[i] = new ArrayList<>();
        }
        val = new int[V + 1];
        for (int i = 1; i <= V; i++) {
            val[i] = in.readInt();
        }
        for (int i = 1; i <= V; i++) {
            if (!map.containsKey(val[i])) {
                map.put(val[i], map.size());
            }
            val[i] = map.get(val[i]);
        }
        int currVal[] = new int[V + 1];
        System.arraycopy(val, 0, currVal, 0, V + 1);
        while (E-- > 0) {
            final int u = in.readInt();
            final int v = in.readInt();
            adj[u].add(v);
            adj[v].add(u);
        }
        start = new int[V + 1];
        end = new int[V + 1];
        eulerTour = new int[2 * (V + 1)];
        level = new int[V + 1];
        marked = new boolean[V + 1];
        DP = new int[log(V) + 1][V + 1];
        parent = new int[V + 1];
        blockCache = new int[2 * (V + 1)];
        dfs(1, 0, 0);
        binaryLift();
        while (Q-- > 0) {
            if (in.readInt() == 1) { // Query
                final int u = in.readInt();
                final int v = in.readInt();
                final Query q;
                if (end[u] < start[v])    // Cousin Nodes
                {
                    q = new Query(end[u], start[v], uSZ, LCA(u, v), qSZ);
                } else if (start[u] > end[v]) {
                    q = new Query(end[v], start[u], uSZ, LCA(u, v), qSZ);
                } else            // Ancestors
                {
                    q = new Query(Math.min(start[u], start[v]), Math.max(start[u], start[v]), uSZ, -1, qSZ);
                }
                queries[qSZ++] = q;
            } else {
                final int idx = in.readInt();
                int newVal = in.readInt();
                if (!map.containsKey(newVal)) {
                    map.put(newVal, map.size());
                }
                newVal = map.get(newVal);
                updates[uSZ++] = new Update(idx, newVal, currVal[idx]);
                currVal[idx] = newVal;
            }
        }
        freq = new int[map.size()];
        BLOCK_SIZE = (int) (Math.pow(2 * V, 2.0 / 3.0) + 1);
        for (int i = 0; i < blockCache.length; i++) {
            blockCache[i] = i / BLOCK_SIZE;
        }
        Arrays.sort(queries, 0, qSZ, new MoComparator());
        final int ans[] = new int[qSZ];
        int moLeft = -1, moRight = -1;
        int currUpd = 0;
        for (int i = 0; i < qSZ; i++) {
            final Query q = queries[i];
            while (currUpd < q.numUpdatesLess) {
                final Update u = updates[currUpd];
                update(u.idx, u.newVal);
                currUpd++;
            }
            while (currUpd > q.numUpdatesLess) {
                final Update u = updates[currUpd - 1];
                update(u.idx, u.prevVal);
                currUpd--;
            }
            while (moLeft < q.L - 1) {
                moLeft++;
                visit(eulerTour[moLeft]);
            }
            while (moLeft >= q.L) {
                visit(eulerTour[moLeft]);
                moLeft--;
            }
            while (moRight < q.R) {
                moRight++;
                visit(eulerTour[moRight]);
            }
            while (moRight > q.R) {
                visit(eulerTour[moRight]);
                moRight--;
            }
            if (q.LCA != -1) {
                visit(q.LCA);
            }
            ans[q.id] = distinctCount;
            if (q.LCA != -1) {
                visit(q.LCA);
            }
        }
        for (final int a : ans) {
            System.out.println(a);
        }
    }
} 