package main.java;

public class MaxSumArray {
    public static void main(String[] args) {
        int n = 10;
        int a[] = new int[]{-1, -3, -7, -1, -10, -2, -3, -2, -1, -3};
        int max_sum = Integer.MIN_VALUE, current_sum = 0;
        for (int i = 0; i < n; i++) {
            current_sum = current_sum + a[i];
            if (current_sum < 0) {
                current_sum = a[i];
            }
            max_sum = Math.max(current_sum, max_sum);
            System.out.print(max_sum + " ");
        }
        System.out.println("\n" + max_sum);
    }
}
