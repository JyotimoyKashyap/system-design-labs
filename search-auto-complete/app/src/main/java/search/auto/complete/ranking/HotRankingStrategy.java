package search.auto.complete.ranking;

import java.util.concurrent.TimeUnit;

public class HotRankingStrategy implements RankingStrategy {
    private final double timeWindowMs;
    private final long epochOffsetMs;

    public HotRankingStrategy(long timeWindow, TimeUnit unit) {
        this(timeWindow, unit, 0L);
    }

    public HotRankingStrategy(long timeWindow, TimeUnit unit, long epochOffsetMs) {
        if (timeWindow <= 0) {
            throw new IllegalArgumentException("Time window must be strictly positive");
        }
        this.timeWindowMs = (double) unit.toMillis(timeWindow);
        this.epochOffsetMs = epochOffsetMs;
    }

    @Override
    public double calculateScore(QueryMetadata metadata) {
        if (metadata.frequency() <= 0) {
            return 0.0;
        }

        // 1. Logarithmic scale for frequency (10 searches = 1.0, 100 searches = 2.0)
        double order = Math.log10(metadata.frequency());

        // 2. Linear boost based on search recency
        double timeBoost = (metadata.lastSearchedTimestampMs() - epochOffsetMs) / timeWindowMs;

        return order + timeBoost;
    }
}
