package search.auto.complete.autocompleteengine;

import java.util.List;
import java.util.Map;

public interface AutoCompleteIndex {
    List<String> search(String prefix);
    void insertBatch(Map<String, ? extends Number> batch);
}
