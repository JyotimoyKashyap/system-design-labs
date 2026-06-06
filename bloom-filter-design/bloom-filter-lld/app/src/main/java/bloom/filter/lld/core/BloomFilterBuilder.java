package bloom.filter.lld.core;

import bloom.filter.lld.config.BloomFilterConfig;
import bloom.filter.lld.hashing.FunnelStrategy;
import bloom.filter.lld.hashing.HashingStrategy;
import bloom.filter.lld.hashing.MurmurHashingStrategy;

public class BloomFilterBuilder<T> {
    private double targetFpr;
    private int expectedElements;
    private HashingStrategy hashingStrategy = new MurmurHashingStrategy();
    private FunnelStrategy<T> funnelStrategy;
    private BloomFilterConfig bloomFilterConfig;

    public static <T> BloomFilterBuilder<T> create(FunnelStrategy<T> funnelStrategy) {
        BloomFilterBuilder<T> builder = new BloomFilterBuilder<>();
        builder.funnelStrategy = funnelStrategy;
        return builder;
    }

    public BloomFilterBuilder<T> withExpectedElements(int expectedElements) {
        this.expectedElements = expectedElements;
        return this;
    }

    public BloomFilterBuilder<T> withTargetFpr(double targetFpr) {
        this.targetFpr = targetFpr;
        return this;
    }

    public BloomFilter<T> build() {
        bloomFilterConfig = new BloomFilterConfig(expectedElements, targetFpr);
        int bitArraySize = bloomFilterConfig.getBitArraySize();
        int kHashFunctions = bloomFilterConfig.getKHashFunctions();

        return new DefaultBloomFilter<>(bitArraySize, kHashFunctions, hashingStrategy, funnelStrategy);
    }
}
