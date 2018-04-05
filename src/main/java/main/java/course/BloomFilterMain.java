package main.java.course;

public class BloomFilterMain {
    public static void main(String[] args) {
        BloomFilter bloomFilter = new BloomFilter(1000);
        bloomFilter.mark(141432);
        bloomFilter.mark(2354364);
        System.out.println(bloomFilter.mayExist(141432));
        System.out.println(bloomFilter.mayExist(723));
        //This should be false, but bloom filters provide no guarantee on definite existence
        System.out.println(bloomFilter.mayExist(1432));
    }
}

class BloomFilter {
    final long[] points;
    private final int size;

    BloomFilter(final int size) {
        this.size = size;
        points = new long[(size + 63) >> 6];
    }

    public void mark(int point) {
        point %= size;
        points[point >> 6] |= (1 << (point & 63));
    }

    public boolean mayExist(int point) {
        point %= size;
        return (points[point >> 6] & (1 << (point & 63))) != 0;
    }
}