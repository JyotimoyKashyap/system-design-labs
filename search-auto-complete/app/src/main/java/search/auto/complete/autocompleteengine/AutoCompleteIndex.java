package search.auto.complete.autocompleteengine;

import java.util.List;

public interface AutoCompleteIndex {
    void insert(String query, int count);
    default void insert(String query) {
        insert(query, 1);
    }
    List<String> search(String prefix);
}
