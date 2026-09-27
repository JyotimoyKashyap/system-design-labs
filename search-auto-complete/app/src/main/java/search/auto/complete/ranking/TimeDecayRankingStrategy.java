package search.auto.complete.ranking;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimeDecayRankingStrategy implements RankingStrategy {
    private final double lambda;
    private final Supplier<Long> clock;

    public TimeDecayRankingStrategy(long halfLife, TimeUnit unit) {
        this(halfLife, unit, System::currentTimeMillis);
    }

    public TimeDecayRankingStrategy(long halfLife, TimeUnit unit, Supplier<Long> clock) {
        if (halfLife <= 0) {
            throw new IllegalArgumentException("Half-life must be strictly positive");
        }

        long halfLifeMs = unit.toMillis(halfLife);
        // pre-compute decay constant: λ = ln(2) / halfLifeMs
        this.lambda = Math.log(2.0) / halfLifeMs;
        this.clock = clock;
    }

    @Override
    public double calculateScore(QueryMetadata metadata) {
        if (metadata.frequency() == 0) {
            return 0.0;
        }

        long now = clock.get();
        long elapsedMs = Math.max(0L, now - metadata.lastSearchedTimestampMs());

        // decay multiplier: e^(-λ * elapsedMs)
        double decayFactor = Math.exp(-lambda * elapsedMs);

        return metadata.frequency() * decayFactor;
    }
}
