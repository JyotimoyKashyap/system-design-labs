package bloom.filter.lld.hashing;

import java.nio.ByteBuffer;
import com.google.common.hash.Hashing;

public class MurmurHashingStrategy implements HashingStrategy {

    @Override
    public int[] hash(byte[] data, int kHashFunctions, int bitArraySize) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        int[] indices = new int[kHashFunctions];

        byte[] hashBytes = Hashing.murmur3_128().hashBytes(data).asBytes();

        ByteBuffer buffer = ByteBuffer.wrap(hashBytes);
        long hash1 = buffer.getLong();
        long hash2 = buffer.getLong();

        // 3. Simulate 'k' independent hash functions using double hashing
        for (int i = 0; i < kHashFunctions; i++) {
            // Formula: h_i = hash1 + (i * hash2)
            long combinedHash = hash1 + ((long) i * hash2);
            
            // Bitwise AND with Long.MAX_VALUE (0x7FFFFFFFFFFFFFFFL) strips the sign bit.
            // Modulo by 'm' keeps the index inside the bounds of the bit array.
            indices[i] = (int) ((combinedHash & Long.MAX_VALUE) % bitArraySize);
        }
        return indices;
    }

    

}
