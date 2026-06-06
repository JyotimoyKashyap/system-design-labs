package bloom.filter.lld.core;

public interface BloomFilter<T> {
    void addKey(T key);

    boolean mightContain(T key);

    void reset();
}
