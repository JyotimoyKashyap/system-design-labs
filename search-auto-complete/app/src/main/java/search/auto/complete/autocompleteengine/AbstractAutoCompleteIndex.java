package search.auto.complete.autocompleteengine;

import search.auto.complete.autocompleteengine.trieautocomplete.Suggestion;
import search.auto.complete.ranking.QueryMetadata;
import search.auto.complete.ranking.RankingStrategy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractAutoCompleteIndex implements AutoCompleteIndex {
    protected final int K;
    protected final RankingStrategy rankingStrategy;
    protected final Supplier<Long> clock;

    protected AbstractAutoCompleteIndex(int K, RankingStrategy rankingStrategy, Supplier<Long> clock) {
        if (K <= 0) {
            throw new IllegalArgumentException("K must be strictly positive");
        }
        this.K = K;
        this.rankingStrategy = Objects.requireNonNull(rankingStrategy, "Ranking strategy cannot be null");
        this.clock = (clock != null) ? clock : System::currentTimeMillis;
    }

    /**
     * Template Method : Enforces input validation before delegating
     * to the concrete search algorithm (Trie, Radix, BK-tree)
     */
    @Override
    public List<String> search(String prefix) {
        validateQuery(prefix);
        return doSearch(prefix);
    }

    /**
     * Subclasses implement their specific search algorithm (e.g. Trie Traversal
     */
    protected abstract List<String> doSearch(String prefix);

    @Override
    public void insertBatch(Map<String, ? extends Number> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }
        doInsertBatch(batch);
    }

    protected abstract void doInsertBatch(Map<String, ? extends Number> batch);

    protected Suggestion createScoredSuggestion(String query, long frequency, long timestampMs) {
        QueryMetadata metadata = new QueryMetadata(query, frequency, timestampMs);
        double score = rankingStrategy.calculateScore(metadata);
        return new Suggestion(score, query);
    }

    protected void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be Blank or empty");
        }
        if (query.length() > 20) {
            throw new IllegalArgumentException("Suggestions unavailable for query length > 20 characters");
        }

        for (char k : query.toCharArray()) {
            if (k != ' ' && (k < 'a' || k > 'z')) {
                throw new IllegalArgumentException("Query contains invalid characters");
            }
        }
    }

    public int getK() {
        return K;
    }

    public RankingStrategy getRankingStrategy() {
        return rankingStrategy;
    }
}
