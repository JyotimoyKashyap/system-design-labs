package search.auto.complete.autocompleteengine.trieautocomplete;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import search.auto.complete.autocompleteengine.AbstractAutoCompleteIndex;
import search.auto.complete.ranking.FrequencyRankingStrategy;
import search.auto.complete.ranking.RankingStrategy;

public class TrieAutoCompleteIndex extends AbstractAutoCompleteIndex {
    
    private final AtomicReference<TrieNode> rootRef;
    private static volatile TrieAutoCompleteIndex instance;

    private TrieAutoCompleteIndex(int K, RankingStrategy rankingStrategy, Supplier<Long> clock) {
        super(K, rankingStrategy, clock);
        this.rootRef = new AtomicReference<>(new TrieNode());
    }

    public static TrieAutoCompleteIndex init(int K) {
        return init(K, new FrequencyRankingStrategy(), System::currentTimeMillis);
    }

    public static TrieAutoCompleteIndex init(int K, RankingStrategy rankingStrategy) {
        return init(K, rankingStrategy, System::currentTimeMillis);
    }

    public static TrieAutoCompleteIndex init(int K, RankingStrategy rankingStrategy, Supplier<Long> clock) {
        if (instance == null) {
            synchronized(TrieAutoCompleteIndex.class) {
                if (instance == null) {
                    instance = new TrieAutoCompleteIndex(K, rankingStrategy, clock);
                }
            }
        } else {
            throw new IllegalStateException("Already initialized!");
        }
        return instance;
    }

    public static TrieAutoCompleteIndex getInstance() {
        if (instance == null) throw new IllegalArgumentException("TrieAutoCompleteIndex is not initialized. Call init(K) at startup");
        return instance;
    }

    public static void resetForTesting() {
        synchronized (TrieAutoCompleteIndex.class) {
            instance = null;
        }
    }

    private void insert(TrieNode targetRoot, String query, int count, long timestampMs) {
        List<TrieNode> path = new ArrayList<>();
        TrieNode node = targetRoot;
        path.add(node);
        for (char k : query.toCharArray()) {
            if (!node.contains(k)) {
                node.put(k);
            }
            node = node.get(k);
            path.add(node);
        }

        // at the end of the loop, I'll be at the end
        node.updateRankBy(count);
        node.setLastSearchedTimestampMs(timestampMs);
        node.setEnd();

        Suggestion updatedSuggestion = createScoredSuggestion(
                query,
                node.getRank(),
                node.getLastSearchedTimestampMs()
        );

        for (TrieNode ancestor : path) {
            ancestor.updateTopK(updatedSuggestion, K);
        }
    }

    @Override
    protected List<String> doSearch(String prefix) {
        TrieNode node = rootRef.get();
        for (char k : prefix.toCharArray()) {
            if (!node.contains(k)) {
                return Collections.emptyList();
            }
            node = node.get(k);
        }

        return node.getTopK();
    }

    @Override
    protected synchronized void doInsertBatch(Map<String, ? extends Number> batch) {
        long batchTimeStamp = clock.get();
        TrieNode newRoot = rootRef.get().deepCopy();

        for (Map.Entry<String, ? extends Number> entry : batch.entrySet()) {
            insert(newRoot, entry.getKey(), entry.getValue().intValue(), batchTimeStamp);
        }

        rootRef.set(newRoot);
    }

}
