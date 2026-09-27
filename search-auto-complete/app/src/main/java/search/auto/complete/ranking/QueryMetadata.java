package search.auto.complete.ranking;

public record QueryMetadata(
        String query,
        long frequency,
        long lastSearchedTimestampMs
) {
    public QueryMetadata {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
        if (frequency < 0) {
            throw new IllegalArgumentException("Frequency cannot be negative");
        }
    }
}
