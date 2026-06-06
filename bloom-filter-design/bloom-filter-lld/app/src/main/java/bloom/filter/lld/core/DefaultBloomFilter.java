package bloom.filter.lld.core;

import java.util.BitSet;

import bloom.filter.lld.hashing.FunnelStrategy;
import bloom.filter.lld.hashing.HashingStrategy;

public class DefaultBloomFilter<T> implements BloomFilter<T>{

    private final BitSet bitSet;
    private final HashingStrategy hashingStrategy;
    private final FunnelStrategy<T> funnelStrategy;

    int bitArraySize;
    int kHashFunctions;
    int keysAdded = 0;

    DefaultBloomFilter(int bitArraySize, int kHashFunctions, 
        HashingStrategy hashingStrategy, FunnelStrategy<T> funnelStrategy) {
            this.bitSet = new BitSet(bitArraySize);
            this.hashingStrategy = hashingStrategy;
            this.funnelStrategy = funnelStrategy;

            this.bitArraySize = bitArraySize;
            this.kHashFunctions = kHashFunctions;
    }

    @Override
    public void addKey(T key) {
        keysAdded++;
        byte[] bytes = funnelStrategy.funnel(key);
        int[] indices = hashingStrategy.hash(bytes, kHashFunctions, bitArraySize);

        // set the indices
        for (int index : indices) {
            bitSet.set(index);
        }
    }

    @Override
    public boolean mightContain(T key) {
        byte[] bytes = funnelStrategy.funnel(key);
        int[] indices = hashingStrategy.hash(bytes, kHashFunctions, bitArraySize);
        // check if the index is already set
        for (int index : indices) {
            if (!bitSet.get(index)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void reset() {
        bitSet.clear();
        keysAdded = 0;
    }

    public BitSet getBitSet() { return bitSet; }
    public int getBitArraySize() { return bitArraySize; }
    public int getKHashFunctions() { return kHashFunctions; }
    public int getKeysAdded() { return keysAdded; }

}
