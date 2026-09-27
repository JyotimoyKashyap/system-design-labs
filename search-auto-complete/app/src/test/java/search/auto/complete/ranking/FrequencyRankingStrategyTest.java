package search.auto.complete.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrequencyRankingStrategyTest {

    private final FrequencyRankingStrategy strategy = new FrequencyRankingStrategy();

    @Test
    @DisplayName("Should return raw frequency as score")
    void testCalculatesScoreFromFrequency() {
        QueryMetadata metadata = new QueryMetadata("apple", 42, 1000L);
        assertEquals(42.0, strategy.calculateScore(metadata));
    }

    @Test
    @DisplayName("Should return 0.0 for zero frequency")
    void testZeroFrequency() {
        QueryMetadata metadata = new QueryMetadata("banana", 0, 1000L);
        assertEquals(0.0, strategy.calculateScore(metadata));
    }

    @Test
    @DisplayName("QueryMetadata validation checks")
    void testQueryMetadataValidation() {
        assertThrows(IllegalArgumentException.class, () -> new QueryMetadata(null, 1, 1000L));
        assertThrows(IllegalArgumentException.class, () -> new QueryMetadata("  ", 1, 1000L));
        assertThrows(IllegalArgumentException.class, () -> new QueryMetadata("apple", -1, 1000L));
    }
}
