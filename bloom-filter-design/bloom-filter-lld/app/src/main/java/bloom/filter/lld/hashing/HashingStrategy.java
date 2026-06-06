package bloom.filter.lld.hashing;

public interface HashingStrategy {
    int[] hash(byte[] data, int kHashFunctions, int bitArraySize);
}
