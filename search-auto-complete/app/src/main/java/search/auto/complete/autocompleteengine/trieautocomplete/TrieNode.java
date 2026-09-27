package search.auto.complete.autocompleteengine.trieautocomplete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrieNode {
    private TrieNode[] links;
    private int rank;
    private boolean isEnd;
    private static final char EMPTY = ' '; // baked into memory address space immutable
    private List<Suggestion> topK;

    public TrieNode() {
        links = new TrieNode[27]; // a-z and " " (whitespace)
        rank = 0;
        isEnd = false;
        topK = new ArrayList<>();
    }

    public TrieNode get(char key) {
        if (key == EMPTY) return links[26];
        else return links[key - 'a'];
    }

    public void put(char key) {
        if (key == EMPTY) links[26] = new TrieNode();
        else links[key - 'a'] = new TrieNode();
    }

    public boolean contains(char key) {
        if (key == EMPTY) return links[26] != null;
        else return links[key - 'a'] != null;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd() {
        isEnd = true;
    }

    public void updateRankBy(int rank) {
        this.rank += rank;
    }

    public int getRank() {
        return rank;
    }

    public TrieNode[] getLinks() {
    return links;
    }

    public TrieNode deepCopy() {
        TrieNode copy = new TrieNode();
        copy.rank = this.rank;
        copy.isEnd = this.isEnd;
        copy.topK = new ArrayList<>(this.topK);
        for (int i=0; i<27; i++) {
            if (this.links[i] != null) {
                copy.links[i] = this.links[i].deepCopy();
            }
        }
        return copy;
    }

    public void updateTopK(Suggestion candidate, int K) {
        topK.removeIf(s -> s.getAutoSuggest().equals(candidate.getAutoSuggest()));
        topK.add(candidate);
        topK.sort(Collections.reverseOrder());
        if (topK.size() > K) {
            topK.removeLast();
        }
    }

    public List<String> getTopK() {
        return topK.stream().map(Suggestion::getAutoSuggest).toList();
    }
}
