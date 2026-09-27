package search.auto.complete.ranking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeDecayRankingStrategyTest {

    @Test
    @DisplayName("When elapsed time is 0, score equals frequency")
    void testZeroElapsedTime() {
        AtomicLong clock = new AtomicLong(10_000L);
        TimeDecayRankingStrategy strategy = new TimeDecayRankingStrategy(24, TimeUnit.HOURS, clock::get);

        QueryMetadata metadata = new QueryMetadata("test", 100, 10_000L);
        assertEquals(100.0, strategy.calculateScore(metadata), 1e-6);
    }

    @Test
    @DisplayName("When elapsed time equals half-life, score is exactly half")
    void testOneHalfLifeElapsed() {
        AtomicLong clock = new AtomicLong(10_000L);
        long halfLifeMs = TimeUnit.HOURS.toMillis(24);
        TimeDecayRankingStrategy strategy = new TimeDecayRankingStrategy(24, TimeUnit.HOURS, clock::get);

        QueryMetadata metadata = new QueryMetadata("test", 100, 10_000L);

        // Advance clock by 1 half-life
        clock.addAndGet(halfLifeMs);
        assertEquals(50.0, strategy.calculateScore(metadata), 1e-4);
    }

    @Test
    @DisplayName("When elapsed time equals two half-lives, score is one quarter")
    void testTwoHalfLivesElapsed() {
        AtomicLong clock = new AtomicLong(10_000L);
        long halfLifeMs = TimeUnit.HOURS.toMillis(24);
        TimeDecayRankingStrategy strategy = new TimeDecayRankingStrategy(24, TimeUnit.HOURS, clock::get);

        QueryMetadata metadata = new QueryMetadata("test", 100, 10_000L);

        // Advance clock by 2 half-lives
        clock.addAndGet(2 * halfLifeMs);
        assertEquals(25.0, strategy.calculateScore(metadata), 1e-4);
    }

    @Test
    @DisplayName("Zero frequency returns 0.0 regardless of time")
    void testZeroFrequency() {
        AtomicLong clock = new AtomicLong(10_000L);
        TimeDecayRankingStrategy strategy = new TimeDecayRankingStrategy(24, TimeUnit.HOURS, clock::get);

        QueryMetadata metadata = new QueryMetadata("test", 0, 10_000L);
        assertEquals(0.0, strategy.calculateScore(metadata));
    }

    @Test
    @DisplayName("Future timestamp (clock skew) clamped to elapsed=0, no amplification")
    void testFutureTimestampClamped() {
        AtomicLong clock = new AtomicLong(10_000L);
        TimeDecayRankingStrategy strategy = new TimeDecayRankingStrategy(24, TimeUnit.HOURS, clock::get);

        // Event timestamp is 5,000ms in future
        QueryMetadata metadata = new QueryMetadata("test", 100, 15_000L);
        assertEquals(100.0, strategy.calculateScore(metadata), 1e-6);
    }

    @Test
    @DisplayName("Invalid half-life throws IllegalArgumentException")
    void testInvalidHalfLife() {
        assertThrows(IllegalArgumentException.class, () -> new TimeDecayRankingStrategy(0, TimeUnit.HOURS));
        assertThrows(IllegalArgumentException.class, () -> new TimeDecayRankingStrategy(-5, TimeUnit.SECONDS));
    }
}
