package search.auto.complete.autocompleteengine.trieautocomplete;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import search.auto.complete.autocompleteengine.AutoCompleteIndex;
import search.auto.complete.queryingestionengine.QueryIngestionBuffer;

public class TrieAutoCompleteIndex implements AutoCompleteIndex {
    
    private final AtomicReference<TrieNode> rootRef;
    private int K;

    private static volatile TrieAutoCompleteIndex instance;

    private TrieAutoCompleteIndex(int K) {
        rootRef = new AtomicReference<>(new TrieNode());
        this.K = K;
    }

    public  static TrieAutoCompleteIndex init(int K) {
        if (instance == null) {
            synchronized(TrieAutoCompleteIndex.class) {
                if (instance == null) {
                    instance = new TrieAutoCompleteIndex(K);
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

    private void insert(TrieNode targetRoot, String query, int count) {
        validateQuery(query);

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
        node.setEnd();

        Suggestion updatedSuggestion = new Suggestion(node.getRank(), query);
        for (TrieNode ancestor : path) {
            ancestor.updateTopK(updatedSuggestion, K);
        }
    }

    @Override
    public List<String> search(String prefix) {
        validateQuery(prefix);

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
    public synchronized void insertBatch(Map<String, ? extends Number> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }

        TrieNode newRoot = rootRef.get().deepCopy();

        for (Map.Entry<String, ? extends Number> entry : batch.entrySet()) {
            insert(newRoot, entry.getKey(), entry.getValue().intValue());
        }

        rootRef.set(newRoot);
    }

    private void dfs(TrieNode node, String autoSuggest, PriorityQueue<Suggestion> minHeap) {
        if (node.isEnd()) {
            minHeap.offer(new Suggestion(node.getRank(), autoSuggest));
            if (minHeap.size() > K) {
                minHeap.poll();
            }
        }

        TrieNode[] links = node.getLinks();
        for (int i=0; i<links.length; i++) {
            char key = i == 26 ? ' ' : (char) (i + 'a');
            // if links[i] != null then add that character to the copy of the String and then do a dfs
            if (links[i] != null) {
                dfs(node.get(key), autoSuggest + key, minHeap);
            }
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.isBlank()) throw new IllegalArgumentException("Query cannot be Blank or empty");
        if (query.length() > 20) throw new IllegalArgumentException("Suggestions unavailable for query length > 20 characters");

        for (char k : query.toCharArray()) {
            if (k != ' ' && (k < 'a' || k > 'z')) {
                throw new IllegalArgumentException("Query contains invalid characters");
            }
        }
    }

}
