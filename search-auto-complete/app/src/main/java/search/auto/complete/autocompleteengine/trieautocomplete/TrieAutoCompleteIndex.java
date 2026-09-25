package search.auto.complete.autocompleteengine.trieautocomplete;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import search.auto.complete.autocompleteengine.AutoCompleteIndex;

public class TrieAutoCompleteIndex implements AutoCompleteIndex {
    
    private TrieNode root;
    private int K;

    private static volatile TrieAutoCompleteIndex instance;

    private TrieAutoCompleteIndex(int K) {
        root = new TrieNode();
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

    @Override
    public void insert(String query) {
        validateQuery(query);

        TrieNode node = root;
        for (char k : query.toCharArray()) {
            if (!node.contains(k)) {
                node.put(k);
            }
            node = node.get(k);
        }

        // at the end of the loop, I'll be at the end
        node.updateRankBy(1);
        node.setEnd();
    }

    @Override
    public List<String> search(String prefix) {
        validateQuery(prefix);

        List<String> suggestions = new ArrayList<>();
        TrieNode node = root;
        for (char k : prefix.toCharArray()) {
            if (!node.contains(k)) {
                return suggestions;
            }
            node = node.get(k);
        }

        // now at the end I'll have links nodes with refs and I'll have to do DFS on all those valid refs
        // after getting the suggestions part, I'll append it to the prefix to complete it
        PriorityQueue<Suggestion> minHeap = new PriorityQueue<>();

        dfs(node, prefix, minHeap);

        LinkedList<String> res = new LinkedList<>();
        while (!minHeap.isEmpty()) {
            res.addFirst(minHeap.poll().getAutoSuggest());
        }

        return res;
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
