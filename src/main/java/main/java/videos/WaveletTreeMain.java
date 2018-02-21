package main.java.videos;

import java.util.Arrays;
import java.util.Random;

class WaveletTree {
    private final Node root;

    public WaveletTree(final int a[]) {
        root = new Node(a, a.length);
    }

    /**
     * Finds the number of elements in the given range having value = element
     *
     * @param index
     * @param element
     * @return
     */
    public int rank(int index, final int element) {
        Node node = this.root;
        boolean exists = true;
        while (!node.isLeafNode()) {
            final double pivot = node.getPivot();
            if (element <= pivot) {
                exists = index < node.getSize() && !(pivot >= node.get(index));
                index = node.getMappingOnLeft(index);
                node = node.getLeftSubTree();
                //go to left
            } else {
                exists = index < node.getSize() && !(pivot < node.get(index));
                index = node.getMappingOnRight(index);
                node = node.getRightSubTree();
                //go to right
            }
        }
        if (element != node.getPivot()) {
            return 0;
        } else {
            return exists ? index : index + 1;
        }
    }

    public int quantile(final int leftIndex,
                        final int rightIndex,
                        final int index) {
        return 0;
    }

    public int rangeCounting(final int leftIndex,
                             final int rightIndex,
                             final int leftElement,
                             final int rightElement) {
        return 0;
    }

    @Override
    public String toString() {
        return "WaveletTree{" +
                "root=" + root +
                '}';
    }

    public int rank(final int left, final int right, final int element) {
        final int leftElements;
        if (left == 0) {
            leftElements = 0;
        } else {
            leftElements = rank(left - 1, element);
        }
        final int rightElements = rank(right, element);
        System.out.println(leftElements);
        System.out.println(rightElements);
        return rightElements - leftElements;
    }
}

class Node {
    private final int array[];
    private final int leftCount[];
    private final int min, max;
    private final Node left, right;

    public Node(final int[] array, int n) {
        this.array = Arrays.copyOf(array, n);
        int max, min;
        max = min = array[0];
        leftCount = new int[n];
        for (int i = 0; i < n; i++) {
            if (array[i] > max) {
                max = array[i];
            } else if (min > array[i]) {
                min = array[i];
            }
        }
        this.min = min;
        this.max = max;
        if (isLeafNode()) {
            left = right = null;
        } else {
            final double pivot = (min + max) / 2.0;
            final int[] leftArray = new int[n], rightArray = new int[n];
            if (array[0] > pivot) {
                rightArray[0] = array[0];
            } else {
                leftArray[0] = array[0];
                leftCount[0] = 1;
            }
            for (int i = 1; i < n; i++) {
                if (array[i] > pivot) {
                    leftCount[i] = leftCount[i - 1];
                    rightArray[i - leftCount[i]] = array[i];
                } else {
                    leftArray[leftCount[i - 1]] = array[i];
                    leftCount[i] = leftCount[i - 1] + 1;
                }
            }
            left = new Node(leftArray, leftCount[n - 1]);
            right = new Node(rightArray, n - leftCount[n - 1]);
        }
    }

    public boolean isLeafNode() {
        return min == max;
    }

    public int get(int i) {
        return array[i];
    }

    public Node getLeftSubTree() {
        return left;
    }

    public Node getRightSubTree() {
        return right;
    }

    public int getMappingOnLeft(int i) {
        return i == 0 ? 0 : leftCount[i - 1];
    }

    public int getMappingOnRight(int i) {
        return i == 0 ? 0 : i - leftCount[i - 1];
    }

    public int getElementOnLeft(int i) {
        return left.get(getMappingOnLeft(i));
    }

    public int getElementOnRight(int i) {
        return right.get(getMappingOnRight(i));
    }

    @Override
    public String toString() {
        return "Node{" +
                "array=" + Arrays.toString(array) +
                ", leftCount=" + Arrays.toString(leftCount) +
                ", min=" + min +
                ", max=" + max +
                ", left=" + left +
                ", right=" + right +
                '}';
    }

    public double getPivot() {
        return (min + max) / 2.0;
    }

    public int getSize() {
        return leftCount.length;
    }
}

public class WaveletTreeMain {
    public static void main(String[] args) {
        //final int[] a = {10, 10, 1, 17, 4, 6};
        final int[] a = new int[10];
        final Random random = new Random();
        int bound = 10;
        for (int i = 0; i < a.length; i++) {
            a[i] = random.nextInt(bound);
        }
        final WaveletTree tree = new WaveletTree(a);
        for (int test = 0; test < 1000000; test++) {
            final int element = random.nextInt(bound);
            int count = 0;
            int left = random.nextInt(a.length);
            int right = random.nextInt(a.length - left) + left;
            for (int i = left; i <= right; i++) {
                if (a[i] == element) {
                    count++;
                }
            }
            int rank = tree.rank(left, right, element);
            if (count != rank) {
                System.out.println(tree);
                System.out.println(count);
                System.out.println(rank);
                throw new RuntimeException("left: " + left + " right: " + right + " element: " + element);
            }
        }
    }
}
