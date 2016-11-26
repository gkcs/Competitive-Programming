package main.java;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SqrtDecomposition {
    private final int a[];
    private final long blocks[];
    private static final int mod = 1000000007;
    private static final BigInteger modo = BigInteger.valueOf(mod);
    private final int frequencies[][];
    private long powers[] = new long[202500];
    private long invPowers[] = new long[202500];
    private final int sqrt;

    public SqrtDecomposition(int n) {
        n = (int) Math.ceil(Math.sqrt(n));
        n = n * n;
        this.a = new int[n];
        this.sqrt = (int) Math.ceil(Math.sqrt(a.length));
        this.blocks = new long[sqrt];
        populatePowers();
        Arrays.fill(blocks, 1);
        this.frequencies = new int[sqrt][202500];
        for (int i = 0; i < sqrt; i++) {
            final int index = i * sqrt;
            frequencies[i][0] = index + sqrt;
            blocks[i] = powers[index + sqrt];
        }
    }

    public void update(int index, int value) {
        index--;
        if (a[index] != value) {
            int blockIndex = index / sqrt;
            while (blockIndex < blocks.length) {
                blocks[blockIndex] = ((((((((blocks[blockIndex]
                        * invPowers[frequencies[blockIndex][a[index]]])
                        % mod)
                        * powers[frequencies[blockIndex][a[index]] - 1])
                        % mod)
                        * invPowers[frequencies[blockIndex][value]])
                        % mod)
                        * powers[frequencies[blockIndex][value] + 1])
                        % mod);
                frequencies[blockIndex][a[index]]--;
                frequencies[blockIndex][value]++;
                blockIndex++;
            }
            a[index] = value;
        }
    }

    private final int freq[] = new int[202500];

    public long query(int end) {
        end--;
        final int blockIndex = end / sqrt;
        final int set[] = new int[sqrt];
        int count = 0;
        final int endIndex = end % sqrt;
        for (int i = 0; i <= endIndex; i++) {
            freq[a[blockIndex * sqrt + i]] = 0;
        }
        for (int i = 0; i <= endIndex; i++) {
            if (freq[a[blockIndex * sqrt + i]] == 0) {
                set[count++] = a[blockIndex * sqrt + i];
            }
            freq[a[blockIndex * sqrt + i]]++;
        }
        long result = blockIndex > 0 ? blocks[blockIndex - 1] : 1;
        for (int i = 0; i < count; i++) {
            final int previous = blockIndex > 0 ? frequencies[blockIndex - 1][set[i]] : 0;
            result = ((((result
                    * invPowers[previous])
                    % mod)
                    * powers[previous + freq[set[i]]])
                    % mod);
        }
        return result;
    }

    private void populatePowers() {
        for (int i = 0; i < powers.length; i++) {
            powers[i] = pow(i + 1);
            invPowers[i] = BigInteger.valueOf(powers[i]).modInverse(modo).longValue();
        }
    }

    public static long pow(int mult) {
        long result = 1;
        long exponent = mult;
        if ((mult & 1) != 0) {
            result = mult;
        }
        for (int bit = 1; bit < 32; bit++) {
            exponent = (exponent * exponent) % mod;
            if ((mult & (1 << bit)) != 0) {
                result = (result * exponent) % mod;
            }
        }
        return result;
    }
}

class Main {
    public static void main(String[] args) {
        final InputReader inputReader = new InputReader(System.in);
        final SqrtDecomposition decomposition = new SqrtDecomposition(inputReader.readInt());
        final int q = inputReader.readInt();
        final StringBuilder stringBuilder = new StringBuilder();
        final long qs[][] = new long[q][3];
        for (int i = 0; i < q; i++) {
            qs[i][0] = inputReader.readInt();
            if (qs[i][0] == 1) {
                qs[i][1] = inputReader.readInt();
                qs[i][2] = inputReader.readInt();
            } else {
                qs[i][1] = inputReader.readLong();
            }
        }
        final Map<Long, Integer> mapping = new HashMap<>();
        mapping.put(0L, 0);
        int count = 1;
        for (int i = 0; i < q; i++) {
            if (qs[i][0] == 1 && !mapping.containsKey(qs[i][2])) {
                mapping.put(qs[i][2], count);
                count++;
            }
        }
        for (int i = 0; i < q; i++) {
            if (qs[i][0] == 1) {
                decomposition.update((int) qs[i][1], mapping.get(qs[i][2]));
            } else {
                stringBuilder.append(decomposition.query((int) qs[i][1])).append('\n');
            }
        }
        System.out.println(stringBuilder);
    }
}
