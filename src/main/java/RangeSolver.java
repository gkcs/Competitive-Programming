import java.util.TreeSet;

public class RangeSolver {
    public int range(int lists[][]) {
        if (lists.length == 0 || lists[0].length == 0) {
            return 0;
        }

        final TreeSet<Node> values = new TreeSet<>();
        for (int i = 0; i < lists.length; i++) {
            values.add(new Node(i, 0, lists[i][0]));
        }
        int largestRange = Integer.MAX_VALUE;
        while (true) {
            Node firstNode = values.first();
            Node first = firstNode;
            Node last = values.last();
            largestRange = last.value - first.value;
            if (firstNode.listIndex + 1 < lists[0].length) {
                values.remove(values.first());
            } else {
                break;
            }
            firstNode = values.first();
            last = values.last();
            values.add(new Node(firstNode.listNumber, firstNode.listIndex + 1,
                                lists[firstNode.listNumber][firstNode.listIndex + 1]));
            if (first.value - last.value < largestRange) {
                largestRange = first.value - last.value;
            }
        }
        return largestRange;
    }
}

class Node implements Comparable<Node> {
    final int listNumber;
    final int listIndex;
    final int value;

    Node(final int listNumber, final int listIndex, final int value) {
        this.listNumber = listNumber;
        this.listIndex = listIndex;
        this.value = value;
    }

    @Override
    public int compareTo(final Node o) {
        return this.value - o.value;
    }
}
