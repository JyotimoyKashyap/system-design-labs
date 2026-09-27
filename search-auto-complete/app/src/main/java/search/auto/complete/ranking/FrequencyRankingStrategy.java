package search.auto.complete.ranking;

public class FrequencyRankingStrategy implements RankingStrategy {

    @Override
    public double calculateScore(QueryMetadata metadata) {
        return metadata.frequency();
    }
}
