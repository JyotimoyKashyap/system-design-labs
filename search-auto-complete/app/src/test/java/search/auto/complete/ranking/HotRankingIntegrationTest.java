package search.auto.complete.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import search.auto.complete.autocompleteengine.trieautocomplete.TrieAutoCompleteIndex;
import search.auto.complete.queryingestionengine.FlushPolicy;
import search.auto.complete.queryingestionengine.QueryIngestionBuffer;
import search.auto.complete.service.SearchAutoCompleteService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotRankingIntegrationTest {

    @Test
    @DisplayName("Hot ranking: Fresh query with fewer searches outranks older query with more searches")
    void testHotRankingPromotion() {
        AtomicLong simulatedClock = new AtomicLong(1_000_000_000L); // Day 1 baseline

        // 24-hour time window (each day grants +1.0 score boost, equal to a 10x search multiplier)
        HotRankingStrategy strategy = new HotRankingStrategy(24, TimeUnit.HOURS, 1_000_000_000L);

        TrieAutoCompleteIndex.resetForTesting();
        TrieAutoCompleteIndex index = TrieAutoCompleteIndex.init(5, strategy, simulatedClock::get);
        QueryIngestionBuffer buffer = new QueryIngestionBuffer(index, new FlushPolicy() {
            @Override public void start(Runnable flushAction) {}
            @Override public void stop() {}
        });
        SearchAutoCompleteService service = new SearchAutoCompleteService(index, buffer);

        // 1. Day 1: Ingest "olympics twenty" 10 times (Score: log10(10) + 0 = 1.0)
        for (int i = 0; i < 10; i++) {
            service.recordQuery("olympics twenty");
        }
        buffer.flush();

        // Verify Day 1 suggestion
        assertEquals(List.of("olympics twenty"), service.getSuggestions("olympics"));

        // 2. Fast-forward clock by 3 days
        simulatedClock.addAndGet(TimeUnit.DAYS.toMillis(3));

        // 3. Day 3: Ingest "olympics twenty four" only 2 times (Score: log10(2) + 3.0 = 3.301)
        for (int i = 0; i < 2; i++) {
            service.recordQuery("olympics twenty four");
        }
        buffer.flush();

        // 4. Verify ranking: "olympics twenty four" (3.301) outranks older "olympics twenty" (1.0)
        List<String> suggestions = service.getSuggestions("olympics");
        assertEquals(List.of("olympics twenty four", "olympics twenty"), suggestions);
    }
}
