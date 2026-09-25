package search.auto.complete.autocompleteengine;

import java.util.List;

public interface AutoCompleteIndex {
    void insert(String query);
    List<String> search(String prefix);
}
