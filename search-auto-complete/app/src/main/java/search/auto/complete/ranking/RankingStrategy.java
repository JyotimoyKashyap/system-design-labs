package search.auto.complete.ranking;

public interface RankingStrategy {
    /**
     * Calculates a numerical relevance score for the given query metadata.
     * Higher score indicates higher relevance in autocomplete suggestions
     */
    double calculateScore(QueryMetadata metadata);
}
