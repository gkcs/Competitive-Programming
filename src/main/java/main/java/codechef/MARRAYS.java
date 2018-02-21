package main.java.codechef;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.TreeMap;

import static java.lang.Math.max;

class MARRAYS {

    private static int bsl(int o[], int k) {
        int l = 0;
        int r = o.length - 1;
        int res = -1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (k - o[mid] >= 0) {
                res = mid;
                l = mid + 1;
            } else
                r = mid - 1;
        }
        return res;
    }

    private static int bsr(int o[], int k) {
        int l = 0;
        int r = o.length - 1;
        int res = -1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (o[mid] - k >= 0) {
                res = mid;
                r = mid - 1;
            } else
                l = mid + 1;
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        for (int t = Integer.parseInt(bufferedReader.readLine()); t > 0; t--) {
            int n = Integer.parseInt(bufferedReader.readLine());
            int a[][] = new int[n][];
            long dp[] = new long[10];
            int o[] = new int[10];
            long ans = 0;
            long f = 0;
            for (int i = 0; i < n; i++) {
                int m = Integer.parseInt(bufferedReader.readLine());
                a[i] = new int[m];
                int ps = o.length;
                long l[] = new long[ps];
                long r[] = new long[ps];
                int il[] = new int[ps];
                int ir[] = new int[ps];
                l[0] = dp[0] - o[0] * f;
                il[0] = 0;
                r[ps - 1] = dp[ps - 1] + o[ps - 1] * f;
                ir[ps - 1] = ps - 1;
                for (int j = 1; j < ps; j++) {
                    if (dp[j] - o[j] * f > l[j - 1]) {
                        l[j] = dp[j] - o[j] * f;
                        il[j] = j;
                    } else {
                        l[j] = l[j - 1];
                        il[j] = il[j - 1];
                    }
                }
                for (int j = ps - 2; j >= 0; j--) {
                    if (dp[j] + o[j] * f > r[j + 1]) {
                        r[j] = dp[j] + o[j] * f;
                        ir[j] = j;
                    } else {
                        r[j] = r[j + 1];
                        ir[j] = ir[j + 1];
                    }
                }

                TreeMap<Integer, Integer> tm = new TreeMap<>();
                int cnt = 0;
                for (int j = 0; j < m; j++) {
                    a[i][j] = Integer.parseInt(bufferedReader.readLine());
                    tm.put(a[i][j], 0);
                }
                for (int j : tm.keySet())
                    tm.put(j, cnt++);
                int co[] = new int[cnt];
                long cdp[] = new long[cnt];
                for (int j = 0; j < m; j++) {
                    int r1 = bsl(o, a[i][j]);//left
                    int r2 = bsr(o, a[i][j]);//right
                    int x = j > 0 ? j - 1 : m - 1;
                    int v = a[i][x];
                    int id = tm.get(v);
                    co[id] = v;
                    long a1 = r1 != -1 ? l[r1] + a[i][j] * f : 0;
                    long a2 = r2 != -1 ? r[r2] - a[i][j] * f : 0;
                    cdp[id] = max(cdp[id], max(a1, a2));
                }
                dp = cdp;
                o = co;
                f++;
            }
            for (int i = 0; i < o.length; i++) {
                ans = max(ans, dp[i]);
            }
            System.out.println(ans);
        }
    }
}