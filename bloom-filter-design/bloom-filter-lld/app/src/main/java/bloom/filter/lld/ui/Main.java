package bloom.filter.lld.ui;

import bloom.filter.lld.core.BloomFilter;
import bloom.filter.lld.core.BloomFilterBuilder;
import bloom.filter.lld.core.DefaultBloomFilter;
import bloom.filter.lld.hashing.StringFunnelStrategy;

public class Main {
    public static void main(String[] args) {
        // Initialize the Bloom Filter with a generic String type
        BloomFilter<String> filter = BloomFilterBuilder.<String>create(new StringFunnelStrategy())
                .withExpectedElements(50)  // Kept small (50 elements) so the visualizer array fits in terminal
                .withTargetFpr(0.05)       // 5% False Positive Rate
                .build();

        // Cast to DefaultBloomFilter so UI can access internal state (bitSet, keysAdded) for visualization
        if (filter instanceof DefaultBloomFilter) {
            ConsoleUI ui = new ConsoleUI((DefaultBloomFilter<String>) filter);
            ui.start();
        } else {
            System.err.println("Unsupported BloomFilter implementation.");
        }
    }
}
