package search.auto.complete.queryingestionengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import search.auto.complete.autocompleteengine.trieautocomplete.TrieAutoCompleteIndex;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class QueryIngestionBufferTest {

    private TrieAutoCompleteIndex index;

    @BeforeEach
    void setUp() {
        TrieAutoCompleteIndex.resetForTesting();
        index = TrieAutoCompleteIndex.init(5);
    }

    @Test
    @DisplayName("Should aggregate duplicate queries into combined counts on flush")
    void testCombinerAggregation() {
        QueryIngestionBuffer buffer = new QueryIngestionBuffer(index, new FlushPolicy() {
            @Override public void start(Runnable flushAction) {}
            @Override public void stop() {}
        });

        for (int i = 0; i < 50; i++) {
            buffer.queue("apple");
        }
        for (int i = 0; i < 20; i++) {
            buffer.queue("application");
        }

        // Before flush, index should be empty
        assertTrue(index.search("app").isEmpty());

        buffer.flush();

        // After flush, "apple" should rank #1 because it has count 50 vs 20
        List<String> results = index.search("app");
        assertEquals(List.of("apple", "application"), results);
    }

    @Test
    @DisplayName("Should automatically flush via TimeIntervalFlushPolicy without manual trigger")
    void testPeriodicFlushWithTimer() throws InterruptedException {
        FlushPolicy timePolicy = new TimeIntervalFlushPolicy(50, TimeUnit.MILLISECONDS);
        QueryIngestionBuffer buffer = new QueryIngestionBuffer(index, timePolicy);
        buffer.start();

        buffer.queue("iphone");

        // Before interval passes, search should be empty
        assertTrue(index.search("iph").isEmpty());

        // Wait for scheduler to trigger periodic flush
        Thread.sleep(120);

        List<String> results = index.search("iph");
        assertEquals(List.of("iphone"), results);

        buffer.stop();
    }

    @Test
    @DisplayName("Should handle massive concurrent queue writes without dropping counts")
    void testConcurrentQueuingWithoutDataLoss() throws InterruptedException {
        QueryIngestionBuffer buffer = new QueryIngestionBuffer(index, new FlushPolicy() {
            @Override public void start(Runnable flushAction) {}
            @Override public void stop() {}
        });

        int threadCount = 20;
        int incrementsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < incrementsPerThread; i++) {
                        buffer.queue("concurrency");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        buffer.flush();

        List<String> results = index.search("con");
        assertEquals(List.of("concurrency"), results);
    }

    @Test
    @DisplayName("Should flush remaining items and terminate cleanly on stop()")
    void testStopDrainsBuffer() {
        FlushPolicy timePolicy = new TimeIntervalFlushPolicy(10, TimeUnit.MINUTES);
        QueryIngestionBuffer buffer = new QueryIngestionBuffer(index, timePolicy);
        buffer.start();

        buffer.queue("shutdown");
        buffer.stop(); // should cancel policy and execute final flush

        List<String> results = index.search("shut");
        assertEquals(List.of("shutdown"), results);
    }
}
