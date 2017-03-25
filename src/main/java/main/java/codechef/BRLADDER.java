package main.java.codechef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BRLADDER {

    public static final int mod = 1000000007;

    public static void main(String[] args) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        final StringBuilder sb = new StringBuilder();
        for (int t = Integer.parseInt(br.readLine()); t > 0; t--) {
            final int n = Integer.parseInt(br.readLine());
            final char a[][] = new char[n][];
            boolean[][] visited = new boolean[a.length][a.length];
            for (int i = 0; i < n; i++) {
                a[i] = br.readLine().toCharArray();
            }
            boolean possible = true;
            for (int i = 0; i < n && possible; i++) {
                for (int j = 0; j < n; j++) {
                    if (a[i][j] != '?' && a[i][j] != '.') {
                        if (a[i][j] == 'G') {
                            if (i > 0 && a[i - 1][j] != '.') {
                                possible = false;
                            }
                            if (j > 0 && a[i][j - 1] != '.') {
                                possible = false;
                            }
                            if (i < a.length - 1 && a[i + 1][j] != '.') {
                                possible = false;
                            }
                            if (j < a.length - 1 && a[i][j + 1] != '.') {
                                possible = false;
                            }
                        } else if (a[i][j] == 'P') {
                            if (i > 0 && (a[i - 1][j] == 'G' || a[i - 1][j] == 'B')) {
                                possible = false;
                            }
                            if (j > 0 && (a[i][j - 1] == 'G' || a[i][j - 1] == 'B')) {
                                possible = false;
                            }
                            if (i < a.length - 1 && (a[i + 1][j] == 'G' || a[i + 1][j] == 'B')) {
                                possible = false;
                            }
                            if (j < a.length - 1 && (a[i][j + 1] == 'G' || a[i][j + 1] == 'B')) {
                                possible = false;
                            }
                        } else {
                            if (i > 0 && (a[i - 1][j] == 'G' || a[i - 1][j] == 'P')) {
                                possible = false;
                            }
                            if (j > 0 && (a[i][j - 1] == 'G' || a[i][j - 1] == 'P')) {
                                possible = false;
                            }
                            if (i < a.length - 1 && (a[i + 1][j] == 'G' || a[i + 1][j] == 'P')) {
                                possible = false;
                            }
                            if (j < a.length - 1 && (a[i][j + 1] == 'G' || a[i][j + 1] == 'P')) {
                                possible = false;
                            }
                        }
                    }
                }
            }
            long count;
            if (!possible) {
                count = 0;
            } else {
                count = 1;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if (a[i][j] == '?' && !visited[i][j]) {
                            count = (count * dfs(a, i, j, visited)) % mod;
                        }
                    }
                }
            }
            sb.append(count).append('\n');
        }
        System.out.println(sb);
    }

    private static int dfs(final char[][] a, final int i, final int j, final boolean[][] visited) {
        final int stack[][] = new int[a.length * a.length][2];
        stack[0][0] = i;
        stack[0][1] = j;
        char currentType = '?';
        int top = 1;
        boolean island = true;
        if (i > 0 && a[i - 1][j] != '.') {
            island = false;
        }
        if (j > 0 && a[i][j - 1] != '.') {
            island = false;
        }
        if (i < a.length - 1 && a[i + 1][j] != '.') {
            island = false;
        }
        if (j < a.length - 1 && a[i][j + 1] != '.') {
            island = false;
        }
        int possibleTypes = visited[i][j] ? 1 : 2;
        if (island) {
            possibleTypes = 3;
        }
        while (top > 0) {
            final int currentX = stack[top - 1][0];
            final int currentY = stack[top - 1][1];
            top--;
            if (!visited[currentX][currentY]) {
                visited[currentX][currentY] = true;
                if (currentX > 0) {
                    int row = currentX - 1;
                    if (!visited[row][currentY]) {
                        if (a[row][currentY] == '?') {
                            stack[top][0] = row;
                            stack[top][1] = currentY;
                            top++;
                        } else if (a[row][currentY] == 'G') {
                            possibleTypes = 0;
                            break;
                        } else if (a[row][currentY] != '.') {
                            if (possibleTypes > 1 || currentType == a[row][currentY]) {
                                possibleTypes = 1;
                                stack[top][0] = row;
                                stack[top][1] = currentY;
                                currentType = a[row][currentY];
                                top++;
                            } else {
                                possibleTypes = 0;
                                break;
                            }
                        }
                    }
                }
                if (currentY > 0) {
                    int col = currentY - 1;
                    if (!visited[currentX][col]) {
                        if (a[currentX][col] == '?') {
                            stack[top][0] = currentX;
                            stack[top][1] = col;
                            top++;
                        } else if (a[currentX][col] == 'G') {
                            possibleTypes = 0;
                            break;
                        } else if (a[currentX][col] != '.') {
                            if (possibleTypes != 1 || currentType == a[currentX][col]) {
                                possibleTypes = 1;
                                currentType = a[currentX][col];
                                stack[top][0] = currentX;
                                stack[top][1] = col;
                                top++;
                            } else {
                                possibleTypes = 0;
                                break;
                            }
                        }
                    }
                }
                if (currentX < a.length - 1) {
                    int row = currentX + 1;
                    if (!visited[row][currentY]) {
                        if (a[row][currentY] == '?') {
                            stack[top][0] = row;
                            stack[top][1] = currentY;
                            top++;
                        } else if (a[row][currentY] == 'G') {
                            possibleTypes = 0;
                            break;
                        } else if (a[row][currentY] != '.') {
                            if (possibleTypes != 1 || currentType == a[row][currentY]) {
                                possibleTypes = 1;
                                currentType = a[row][currentY];
                                stack[top][0] = row;
                                stack[top][1] = currentY;
                                top++;
                            } else {
                                possibleTypes = 0;
                                break;
                            }
                        }
                    }
                }
                if (currentY < a.length - 1) {
                    int col = currentY + 1;
                    if (!visited[currentX][col]) {
                        if (a[currentX][col] == '?') {
                            stack[top][0] = currentX;
                            stack[top][1] = col;
                            top++;
                        } else if (a[currentX][col] == 'G') {
                            possibleTypes = 0;
                            break;
                        } else if (a[currentX][col] != '.') {
                            if (possibleTypes != 1 || currentType == a[currentX][col]) {
                                possibleTypes = 1;
                                currentType = a[currentX][col];
                                stack[top][0] = currentX;
                                stack[top][1] = col;
                                top++;
                            } else {
                                possibleTypes = 0;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return possibleTypes;
    }
}