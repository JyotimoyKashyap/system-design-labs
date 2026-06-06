package bloom.filter.lld.config;

public class BloomFilterConfig {
    private int expectedElements;
    private double targetFpr; 

    private int bitArraySize;
    private int kHashFunctions;

    public BloomFilterConfig (int expectedElements, double targetFpr) {
        if (expectedElements <= 0) {
            throw new IllegalArgumentException("Expected elements must be > 0");
        }

        if (targetFpr <= 0.0 || targetFpr >= 1.0) {
            throw new IllegalArgumentException("False positive rate (x) must be 0.0 < x < 1.0");
        }

        this.expectedElements = expectedElements;
        this.targetFpr = targetFpr;

        // calculate the bit array size 
        this.bitArraySize = calculateBitArraySize();
        this.kHashFunctions = calculateKHashFunctions();
    }

    private int calculateBitArraySize() {
        return (int) Math.ceil(-1 * (expectedElements * Math.log(targetFpr)) / Math.pow(Math.log(2), 2));
    }

    private int calculateKHashFunctions() {
        return (int) Math.max(1, Math.round(((double) bitArraySize / expectedElements) * Math.log(2)));
    }

    /**
     * Calculate the optimal bit array size for the bloom filter
     * @return Optimal Bit Array Size 
     */
    public int getBitArraySize() {
        return bitArraySize;
    }

    /**
     * Calculates the optimal K Hash functions that is required for the bloom filter
     * @return No of hashing functions 
     */
    public int getKHashFunctions() {
        return kHashFunctions;
    }
    
}
