package main.java.videos;

import main.java.InputReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MosUpdate {
    public static int BLOCK_SIZE = 0;

    public static void main(String[] args) {
        final InputReader in = new InputReader(System.in);
        final int n = in.readInt();
        final int a[] = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = in.readInt();
        }
        final int q = in.readInt();
        final List<RangeQuery> queries = new ArrayList<>();
        final List<Update> updates = new ArrayList<>();
        final int[] tempValueHolder = new int[a.length];
        System.arraycopy(a, 0, tempValueHolder, 0, a.length);
        for (int i = 0; i < q; i++) {
            if (in.readInt() == 1) {
                queries.add(new RangeQuery(queries.size(), in.readInt(), in.readInt(), updates.size()));
            } else {
                final int index = in.readInt();
                final int newValue = in.readInt();
                updates.add(new Update(index, newValue, tempValueHolder[i]));
                tempValueHolder[index] = newValue;
            }
        }
        BLOCK_SIZE = (int) Math.pow(a.length, 0.67);
        System.out.println(Arrays.stream(solve(a, queries, updates))
                                   .mapToObj(String::valueOf)
                                   .collect(Collectors.joining("\n")));
    }

    public static int[] solve(final int[] a, final List<RangeQuery> queries, final List<Update> updates) {
        Collections.sort(queries);
        int start = 0, end = -1;
        final int frequencies[] = new int[10];
        final int answers[] = new int[queries.size()];
        int count = 0;
        int updatesDoneTillNow = 0;
        for (final RangeQuery query : queries) {
            while (updatesDoneTillNow < query.updatesTillNow) {
                final Update update = updates.get(updatesDoneTillNow);
                if (update.index >= start && update.index <= end) {
                    frequencies[a[update.index]]--;
                }
                a[update.index] = update.newValue;
                if (update.index >= start && update.index <= end) {
                    frequencies[a[update.index]]++;
                }
                updatesDoneTillNow++;
            }
            while (updatesDoneTillNow > query.updatesTillNow) {
                updatesDoneTillNow--;
                final Update update = updates.get(updatesDoneTillNow);
                if (update.index >= start && update.index <= end) {
                    frequencies[a[update.index]]--;
                }
                a[update.index] = update.previousValue;
                if (update.index >= start && update.index <= end) {
                    frequencies[a[update.index]]++;
                }
            }
            while (start < query.left) {
                frequencies[a[start]]--;
                if (frequencies[a[start]] == 2) {
                    count--;
                }
                start++;
            }
            while (start > query.left) {
                start--;
                frequencies[a[start]]++;
                if (frequencies[a[start]] == 3) {
                    count++;
                }
            }
            while (end < query.right) {
                end++;
                frequencies[a[end]]++;
                if (frequencies[a[end]] == 3) {
                    count++;
                }
            }
            while (end > query.right) {
                frequencies[a[end]]--;
                if (frequencies[a[end]] == 2) {
                    count--;
                }
                end--;
            }
            answers[query.index] = count;
        }
        return answers;
    }
}

class RangeQuery implements Comparable<RangeQuery> {
    final int index, left, right, updatesTillNow;

    public RangeQuery(final int index, final int left, final int right, final int updatesTillNow) {
        this.index = index;
        this.left = left;
        this.right = right;
        this.updatesTillNow = updatesTillNow;
    }

    @Override
    public int compareTo(final RangeQuery other) {
        final int leftBlockIndex = this.left / MosUpdate.BLOCK_SIZE;
        final int otherLeftBlockIndex = other.left / MosUpdate.BLOCK_SIZE;
        if (leftBlockIndex != otherLeftBlockIndex) {
            return leftBlockIndex - otherLeftBlockIndex;
        } else {
            final int rightBlockIndex = this.right / MosUpdate.BLOCK_SIZE;
            final int otherRightBlockIndex = other.right / MosUpdate.BLOCK_SIZE;
            if (rightBlockIndex != otherRightBlockIndex) {
                return rightBlockIndex - otherLeftBlockIndex;
            } else {
                return this.updatesTillNow - other.updatesTillNow;
            }
        }
    }

    @Override
    public String toString() {
        return "RangeQuery{" +
                "index=" + index +
                ", left=" + left +
                ", right=" + right +
                ", updatesTillNow=" + updatesTillNow +
                '}';
    }
}

class Update {
    final int index, previousValue, newValue;

    public Update(final int index, final int newValue, final int previousValue) {
        this.index = index;
        this.newValue = newValue;
        this.previousValue = previousValue;
    }
}