package main.java;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SqrtDecomposition {
    private final int a[];
    private final BigInteger blocks[];
    private static final int mod = 1000000007;
    private static final BigInteger modo = BigInteger.valueOf(mod);
    private final int frequencies[][];
    private BigInteger powers[] = new BigInteger[202500];
    private BigInteger invPowers[] = new BigInteger[202500];
    private final int sqrt;

    public SqrtDecomposition(int n) {
        n = (int) Math.ceil(Math.sqrt(n));
        n = n * n;
        this.a = new int[n];
        this.sqrt = (int) Math.ceil(Math.sqrt(a.length));
        this.blocks = new BigInteger[sqrt];
        populatePowers();
        Arrays.fill(blocks, BigInteger.ONE);
        this.frequencies = new int[sqrt][powers.length];
        for (int i = 0; i < sqrt; i++) {
            final int index = i * sqrt;
            if (i > 0) {
                blocks[i] = blocks[i - 1];
                frequencies[i][0] = frequencies[i - 1][0];
            } else {
                blocks[i] = BigInteger.ONE;
            }
            for (int j = 0; j < sqrt; j++) {
                blocks[i] = blocks[i]
                        .multiply(invPowers[frequencies[i][a[index + j]]])
                        .mod(modo)
                        .multiply(powers[frequencies[i][a[index + j]] + 1])
                        .mod(modo);
                ++frequencies[i][a[index + j]];
            }
        }
    }

    public void update(int index, int value) {
        index--;
        int blockIndex = index / sqrt;
        while (blockIndex < blocks.length) {
            blocks[blockIndex] = blocks[blockIndex]
                    .multiply(invPowers[frequencies[blockIndex][a[index]]])
                    .mod(modo)
                    .multiply(powers[frequencies[blockIndex][a[index]] - 1])
                    .mod(modo)
                    .multiply(invPowers[frequencies[blockIndex][value]])
                    .mod(modo)
                    .multiply(powers[frequencies[blockIndex][value] + 1])
                    .mod(modo);
            frequencies[blockIndex][a[index]]--;
            frequencies[blockIndex][value]++;
            blockIndex++;
        }
        a[index] = value;
    }

    public long query(int end) {
        end--;
        final int blockIndex = end / sqrt;
        final Map<Integer, Integer> map = new HashMap<>();
        final int[][] elements = new int[sqrt][2];
        int count = 0;
        final int endIndex = end % sqrt;
        for (int i = 0; i <= endIndex; i++) {
            if (!map.containsKey(a[blockIndex * sqrt + i])) {
                map.put(a[blockIndex * sqrt + i], count);
                elements[count][0] = a[blockIndex * sqrt + i];
                count++;
            }
            elements[map.get(a[blockIndex * sqrt + i])][1]++;
        }
        BigInteger result = blockIndex > 0 ? blocks[blockIndex - 1] : BigInteger.ONE;
        for (final Map.Entry<Integer, Integer> entry : map.entrySet()) {
            final int previous = blockIndex > 0 ? frequencies[blockIndex - 1][entry.getKey()] : 0;
            result = result
                    .multiply(invPowers[previous])
                    .mod(modo)
                    .multiply(powers[previous + elements[entry.getValue()][1]])
                    .mod(modo);
        }
        return result.longValue();
    }

    private void populatePowers() {
        for (int i = 0; i < powers.length; i++) {
            powers[i] = BigInteger.valueOf(pow(i + 1));
            invPowers[i] = powers[i].modInverse(modo);
        }
    }

    private long pow(int mult) {
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
        final int qs[][] = new int[q][3];
        for (int i = 0; i < q; i++) {
            qs[i][0] = inputReader.readInt();
            if (qs[i][0] == 1) {
                qs[i][1] = inputReader.readInt();
                qs[i][2] = inputReader.readInt();
            } else {
                qs[i][1] = inputReader.readInt();
            }
        }
        final Map<Integer, Integer> mapping = new HashMap<>();
        mapping.put(0, 0);
        int count = 1;
        for (int i = 0; i < q; i++) {
            if (qs[i][0] == 1 && !mapping.containsKey(qs[i][2])) {
                mapping.put(qs[i][2], count);
                count++;
            }
        }
        for (int i = 0; i < q; i++) {
            if (qs[i][0] == 1) {
                decomposition.update(qs[i][1], mapping.get(qs[i][2]));
            } else {
                stringBuilder.append(decomposition.query(qs[i][1])).append('\n');
            }
        }
        System.out.println(stringBuilder);
    }
}
