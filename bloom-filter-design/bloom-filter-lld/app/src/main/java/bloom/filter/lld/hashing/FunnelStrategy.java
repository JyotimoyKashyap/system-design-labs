package bloom.filter.lld.hashing;

public interface FunnelStrategy<T> {
    byte[] funnel(T key);
}
