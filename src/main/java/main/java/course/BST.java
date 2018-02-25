package main.java.course;

import java.util.TreeSet;

public class BST {
    private BST left, right;
    private final int nodeValue;

    public BST(final int nodeValue) {
        this.nodeValue = nodeValue;
    }

    public void add(int element) {
        if (nodeValue > element) {
            if (left == null) {
                left = new BST(element);
            } else {
                left.add(element);
            }
        } else {
            if (right == null) {
                right = new BST(element);
            } else {
                right.add(element);
            }
        }
    }

    public void delete(int element) {
        if (nodeValue > element) {
            if (left == null) {
                throw new RuntimeException("Element does not exist!");
            } else {
                left.delete(element);
            }
        } else {
            if (right == null) {
                throw new RuntimeException("Element does not exist!");
            } else {
                right.delete(element);
            }
        }
    }

    public boolean search(int element) {
        if (nodeValue == element) {
            return true;
        }
        if (nodeValue > element) {
            return left != null && left.search(element);
        } else {
            return right != null && right.search(element);
        }
    }

    @Override
    public String toString() {
        return "BST{" +
                "left=" + left +
                ", right=" + right +
                ", nodeValue=" + nodeValue +
                '}';
    }
}

class BSTMain {
    public static void main(String[] args) {
//        BST bst = new BST(0);
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.add(-1);
        treeSet.add(12);
        treeSet.add(432);
        treeSet.add(34);
        treeSet.add(-46);
        treeSet.add(-232);
        treeSet.add(-45);
        System.out.println(treeSet.contains(11));
        System.out.println(treeSet);
    }
}